
package me.abouabra.zovo.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    // Google
    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.provider.google.authorization-uri:}")
    private String googleAuthorizationUri;
    @Value("${spring.security.oauth2.client.provider.google.token-uri:}")
    private String googleTokenUri;
    @Value("${spring.security.oauth2.client.provider.google.user-info-uri:}")
    private String googleUserInfoUri;

    // Github
    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String githubClientId;
    @Value("${spring.security.oauth2.client.registration.github.client-secret:}")
    private String githubClientSecret;
    @Value("${spring.security.oauth2.client.provider.github.authorization-uri:}")
    private String githubAuthorizationUri;
    @Value("${spring.security.oauth2.client.provider.github.token-uri:}")
    private String githubTokenUri;
    @Value("${spring.security.oauth2.client.provider.github.user-info-uri:}")
    private String githubUserInfoUri;

    public String getAuthorizationUrl(String provider) {
        String redirectUri = baseUrl + "/api/v1/auth/oauth2/callback/" + provider;
        String state = UUID.randomUUID().toString();

        if ("google".equals(provider)) {
            return UriComponentsBuilder.fromHttpUrl(googleAuthorizationUri)
                    .queryParam("client_id", googleClientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("scope", "openid email profile")
                    .queryParam("state", state)
                    .build().toString();
        } else if ("github".equals(provider)) {
            return UriComponentsBuilder.fromHttpUrl(githubAuthorizationUri)
                    .queryParam("client_id", githubClientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("scope", "user:email read:user")
                    .queryParam("state", state)
                    .build().toString();
        }
        throw new IllegalArgumentException("Unknown provider: " + provider);
    }

    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> handleCallback(String provider, String code) {
        String redirectUri = baseUrl + "/api/v1/auth/oauth2/callback/" + provider;

        try {
            if ("google".equals(provider)) {
                // 1. Exchange code for token using MultiValueMap (form data)
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("code", code);
                params.add("client_id", googleClientId);
                params.add("client_secret", googleClientSecret);
                params.add("redirect_uri", redirectUri);
                params.add("grant_type", "authorization_code");

                // Get token
                Map<String, Object> tokenResponse = exchangeForToken(googleTokenUri, params);
                String accessToken = (String) tokenResponse.get("access_token");

                // 2. Fetch user info
                Map<String, Object> userInfo = getUserInfo(googleUserInfoUri, accessToken);

                // 3. Get email and username
                String email = (String) userInfo.get("email");
                String username = (String) userInfo.getOrDefault("name", email.split("@")[0]);

                User user = linkOrCreateUser(email, username);

                // Return a simplified response instead of the entire User entity
                Map<String, Object> userResponse = new HashMap<>();
                userResponse.put("id", user.getId());
                userResponse.put("username", user.getUsername());
                userResponse.put("email", user.getEmail());
                userResponse.put("isActive", user.isActive());
                userResponse.put("isEnabled", user.isEnabled());

                return ApiResponse.success("Authentication successful", userResponse);
            } else if ("github".equals(provider)) {
                // 1. Exchange code for token
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("code", code);
                params.add("client_id", githubClientId);
                params.add("client_secret", githubClientSecret);
                params.add("redirect_uri", redirectUri);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                // Get token
                Map<String, Object> tokenResponse = exchangeForToken(githubTokenUri, params, headers);
                String accessToken = (String) tokenResponse.get("access_token");

                // 2. Fetch user info
                Map<String, Object> userInfo = getUserInfo(githubUserInfoUri, accessToken);

                // Use GitHub login and email
                String email = (String) userInfo.get("email");
                if (email == null) {
                    email = userInfo.get("login") + "@github.com";
                }
                String username = (String) userInfo.get("login");

                User user = linkOrCreateUser(email, username);

                // Return a simplified response instead of the entire User entity
                Map<String, Object> userResponse = new HashMap<>();
                userResponse.put("id", user.getId());
                userResponse.put("username", user.getUsername());
                userResponse.put("email", user.getEmail());
                userResponse.put("isActive", user.isActive());
                userResponse.put("isEnabled", user.isEnabled());

                return ApiResponse.success("Authentication successful", userResponse);
            }
            throw new IllegalArgumentException("Unknown provider: " + provider);
        } catch (Exception e) {
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Authentication failed: " + e.getMessage());
        }
    }

    private Map<String, Object> exchangeForToken(String url, MultiValueMap<String, String> params) {
        return exchangeForToken(url, params, new HttpHeaders());
    }

    private Map<String, Object> exchangeForToken(String url, MultiValueMap<String, String> params, HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        return response.getBody();
    }

    private Map<String, Object> getUserInfo(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }

    private User linkOrCreateUser(String email, String username) {
        return userRepository.findUserByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setUsername(username);
            u.setPassword(UUID.randomUUID().toString());
            u.setEnabled(true);
            u.setActive(true);
            return userRepository.save(u);
        });
    }
}