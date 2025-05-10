package me.abouabra.zovo.services.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.configs.SessionProperties;
import me.abouabra.zovo.dtos.UserDTO;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.OAuthConnection;
import me.abouabra.zovo.models.Role;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.OAuthConnectionRepository;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.AuthService;
import me.abouabra.zovo.services.redis.RedisStorageService;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import me.abouabra.zovo.utils.ApiResponse;
import me.abouabra.zovo.utils.AvatarGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service class for handling OAuth2 functionality.
 * <p>
 * Provides methods for managing OAuth2 providers, generating authorization URLs,
 * handling provider callbacks, and managing user accounts associated with OAuth2 authentication.
 * Integrates with user and role repositories, authentication services,
 * and external OAuth2 providers for seamless user login and account handling.
 */
@Service
@Slf4j
public class OAuth2Service {
    private final UserRepository userRepository;
    private final OAuthConnectionRepository oAuthConnectionRepository;
    private final RoleRepository roleRepository;
    private final RedisStorageService redisStorageService;
    private final SessionProperties sessionProperties;
    private final AuthService authService;
    private final RestTemplate restTemplate;
    private final List<OAuth2Provider> providers;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final String oAuthRedirectPrefix;
    private final AvatarStorageService avatarStorageService;
    private final AvatarGenerator avatarGenerator;
    @Autowired
    public OAuth2Service(
            UserRepository userRepository,
            OAuthConnectionRepository oAuthConnectionRepository,
            RoleRepository roleRepository,
            RedisStorageService redisStorageService,
            SessionProperties sessionProperties, AuthService authService,
            List<OAuth2Provider> providers,
            PasswordEncoder passwordEncoder, UserMapper userMapper,
            @Value("${app.oauth2.redirect-prefix}") String oAuthRedirectPrefix, AvatarStorageService avatarStorageService, AvatarGenerator avatarGenerator) {
        this.userRepository = userRepository;
        this.oAuthConnectionRepository = oAuthConnectionRepository;
        this.roleRepository = roleRepository;
        this.redisStorageService = redisStorageService;
        this.sessionProperties = sessionProperties;
        this.authService = authService;
        this.oAuthRedirectPrefix = oAuthRedirectPrefix;
        this.avatarStorageService = avatarStorageService;
        this.avatarGenerator = avatarGenerator;
        this.restTemplate = new RestTemplate();
        this.providers = providers;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves an {@link OAuth2Provider} by its name.
     *
     * <p>Searches through the available providers and returns the matching one.
     * Throws an exception if no provider matches the given name.
     *
     * @param providerName the name of the OAuth2 provider to retrieve.
     * @return the matching {@link OAuth2Provider} instance.
     * @throws IllegalArgumentException if the provider name does not match any known provider.
     */
    private OAuth2Provider getProvider(String providerName) {
        return providers.stream()
                .filter(p -> p.getName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider: " + providerName));
    }

    /**
     * Generates the authorization URL for the given provider.
     *
     * @param providerName the name of the authentication provider.
     * @return the generated authorization URL as a string.
     */
    public String getAuthorizationUrl(String providerName) {
        String state = UUID.randomUUID().toString();
        return getProvider(providerName).buildAuthorizationUrl(state);
    }


    /**
     * Handles the OAuth2 callback for the specified provider.
     * This processes the authentication flow including token exchange, user information retrieval,
     * and user session handling.
     *
     * @param providerName The name of the OAuth2 provider (e.g., Google, GitHub).
     * @param code         The authorization code received from the provider.
     * @param request      The HTTP servlet request.
     * @param response     The HTTP servlet response.
     * @return {@code ResponseEntity} containing an {@code ApiResponse} with details of the processing result.
     */
    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> handleCallback(String providerName, String code, HttpServletRequest request, HttpServletResponse response) {
        try {
            OAuth2Provider provider = getProvider(providerName);

            MultiValueMap<String, String> params = provider.buildTokenRequestParams(code);

            Map<String, Object> tokenResponse = exchangeForToken(
                    provider.getTokenEndpoint(),
                    params,
                    provider.getAdditionalHeaders());

            String accessToken = (String) tokenResponse.get("access_token");

            Map<String, Object> userInfo = getUserInfo(provider.getUserInfoEndpoint(), accessToken);

            Map<String, String> userDetails = provider.extractUserDetails(userInfo, accessToken);

            User user = findOrCreateUserWithOAuth(
                    providerName,
                    userDetails.get("id"),
                    userDetails.get("email").toLowerCase(Locale.ROOT),
                    userDetails.get("name")
            );

            ResponseEntity<? extends ApiResponse<?>> twoFactorAuthChallengeIfEnabled =
                    authService.generateTwoFactorAuthChallengeIfEnabled(user, providerName, String.format("%s/2fa", oAuthRedirectPrefix));
            if (twoFactorAuthChallengeIfEnabled != null) return twoFactorAuthChallengeIfEnabled;

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    new UserPrincipal(user),
                    null,
                    new UserPrincipal(user).getAuthorities()
            );

            SecurityContext context = authService.createAndSetSecurityContext(authentication);
            HttpSession newSession = authService.createNewSession(request, context);
            response.addCookie(sessionProperties.createSessionCookie(newSession));

            UserDTO userDTO = userMapper.toDTO(user, avatarStorageService);
            Map<String, Object> userResponse = authService.getStringObjectMap(userDTO);

            return ApiResponse.redirect(String.format("%s/success", oAuthRedirectPrefix), userResponse);
        } catch (Exception e) {
            log.error("OAuth login error", e);
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Authentication failed: " + e.getMessage());
        }
    }


    /**
     * Exchanges an authorization code or credentials for an access token by making a POST request.
     *
     * @param url     The URL of the token endpoint.
     * @param params  The request parameters needed for the token exchange.
     * @param headers The HTTP headers to include in the request.
     * @return A map containing the token response, including the access token and other details.
     */
    private Map<String, Object> exchangeForToken(String url, MultiValueMap<String, String> params,
                                                 HttpHeaders headers) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }

    /**
     * Retrieves user information from a specified URL using an access token.
     *
     * @param url         The endpoint URL to fetch user information from.
     * @param accessToken The access token required for authentication.
     * @return A map containing user information retrieved from the endpoint.
     */
    private Map<String, Object> getUserInfo(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }

    /**
     * Finds an existing user associated with a specific OAuth provider
     * and provider ID or creates a new user if no match is found.
     *
     * @param provider   The name of the OAuth provider (e.g., Google, Facebook).
     * @param providerId The unique ID of the user in the OAuth provider's system.
     * @param email      The email address of the user.
     * @param name       The name of the user.
     * @return The existing or newly created User object.
     */
    @Transactional
    protected User findOrCreateUserWithOAuth(String provider, String providerId, String email, String name) {
        Optional<OAuthConnection> existingConnection = oAuthConnectionRepository
                .findByProviderAndProviderId(provider, providerId);

        if (existingConnection.isPresent()) {
            return existingConnection.get().getUser();
        }

        User user = userRepository.findUserByEmail(email).orElseGet(() -> {
            String username = email.split("@")[0];
            while (userRepository.existsByUsername(username)) {
                username = username + new Random().nextInt(1000);
            }

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setEnabled(true);
            newUser.setActive(true);

            String defaultRole = "ROLE_USER";
            var userRole = redisStorageService.getRole(defaultRole);
            if (userRole == null) {
                userRole = roleRepository.findByName(defaultRole).orElseThrow(() ->
                        new RuntimeException("Role with the specified name was not found"));
                redisStorageService.setRole(defaultRole, userRole);
            }
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            newUser.setRoles(roles);

            newUser = userRepository.save(newUser);
            String avatarKey = avatarGenerator.createAvatar(newUser.getId().toString(), false);
            newUser.setAvatarKey(avatarKey);

            return userRepository.save(newUser);
        });

        OAuthConnection connection = new OAuthConnection();
        connection.setUser(user);
        connection.setProvider(provider);
        connection.setProviderId(providerId);
        connection.setEmail(email);
        connection.setName(name);
        oAuthConnectionRepository.save(connection);

        return user;
    }
}