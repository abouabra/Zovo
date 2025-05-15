package me.abouabra.zovo.services;


import com.bastiaanjansen.otp.HMACAlgorithm;
import com.bastiaanjansen.otp.SecretGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.configs.SessionProperties;
import me.abouabra.zovo.dtos.*;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.enums.VerificationTokenType;
import me.abouabra.zovo.exceptions.*;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.Role;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.redis.RedisStorageService;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import me.abouabra.zovo.utils.ApiResponse;
import me.abouabra.zovo.utils.AvatarGenerator;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;



/**
 * The {@code AuthService} class handles user authentication, registration,
 * two-factor authentication (2FA), OAuth2 logins, password management, and account activation.
 * <p>
 * It integrates with various services including email, token management,
 * encryption, and session management to provide a comprehensive authentication
 * solution.
 * </p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionProperties sessionProperties;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    private final RedisStorageService redisStorageService;
    private final SecretEncryptionService encryptionService;
    private final AvatarStorageService avatarStorageService;
    private final AvatarGenerator avatarGenerator;
    private final UserService userService;

    /**
     * Registers a new user and sends a verification email.
     * <p>
     * Validates if the username or email already exists, throws an exception if either is taken.
     * Saves the new user and assigns the default "ROLE_USER".
     * Sends an email with a verification token asynchronously.
     * </p>
     *
     * @param requestDTO the user registration details including username, email, and password.
     * @return a {@link ResponseEntity} containing a success message upon successful registration.
     * @throws UserAlreadyExistsException if the username or email is already in use.
     * @throws RoleNotFoundException      if the default role is not found in the database.
     */
    public ResponseEntity<? extends ApiResponse<?>> register(@Valid UserRegisterDTO requestDTO) {

        if (userRepo.existsByUsernameOrEmail(requestDTO.getUsername(), requestDTO.getEmail())) {
            if (userRepo.findUserByUsername(requestDTO.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username '%s' is already taken".formatted(requestDTO.getUsername()));
            } else {
                throw new UserAlreadyExistsException("Email '%s' is already taken".formatted(requestDTO.getEmail()));
            }
        }

        User user = saveNewUser(requestDTO);

        CompletableFuture.runAsync(() -> {
            String UUIDToken = verificationTokenService.generateVerificationToken(user, VerificationTokenType.CONFIRM_EMAIL);
            emailService.sendMailAsync(user.getEmail(), VerificationTokenType.CONFIRM_EMAIL, UUIDToken);

            String avatarKey = avatarGenerator.createAvatar(user.getId().toString(), false);
            user.setAvatarKey(avatarKey);
            userRepo.save(user);
        });

        return ApiResponse.success("User has been registered successfully. Please check your email for verification link");
    }

    /**
     * Handles user login by authenticating the provided credentials.
     * Initiates two-factor authentication if enabled for the user.
     *
     * @param loginDTO the login details containing email and password.
     * @param request the HTTP request used for handling session management.
     * @param response the HTTP response to modify cookies or headers.
     * @return a ResponseEntity containing the login status or 2FA requirement.
     */
    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> login(UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        ResponseEntity<? extends ApiResponse<?>> twoFactorAuthChallengeIfEnabled = generateTwoFactorAuthChallengeIfEnabled(user, "EmailPassword", null);
        if (twoFactorAuthChallengeIfEnabled != null) return twoFactorAuthChallengeIfEnabled;

        SecurityContext context = createAndSetSecurityContext(authentication);
        HttpSession newSession = createNewSession(request, context);
        response.addCookie(sessionProperties.createSessionCookie(newSession));

        UserDTO userDTO = userMapper.toDTO(userPrincipal.getUser(), avatarStorageService);
        return ApiResponse.success("Logged in successfully", userDTO);
    }

    /**
     * Handles login with Two-Factor Authentication (2FA) by verifying the provided credentials
     * and token, then establishes a session upon successful validation.
     *
     * @param tokenAndCodeDTO Object containing the temporary token and the 2FA code submitted by the user.
     * @param request The HTTP servlet request used to manage the user session.
     * @param response The HTTP servlet response used to update cookies and session details upon login.
     * @return A {@code ResponseEntity} containing the success message and the user's response data.
     */
    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> loginWith2FA(@RequestBody TwoFaTokenAndCodeDTO tokenAndCodeDTO, HttpServletRequest request, HttpServletResponse response) {
        String tempToken = tokenAndCodeDTO.getToken();
        String email = redisStorageService.get2FASession(tempToken);

        if (email == null)
            throw new InvalidTwoFactorCodeException("Invalid 2FA code");

        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isValid = verify2FACode(user, tokenAndCodeDTO.getCode());

        if (!isValid)
            throw new InvalidTwoFactorCodeException("Invalid 2FA code");

        redisStorageService.delete2FASession(tempToken);


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(user),
                null,
                new UserPrincipal(user).getAuthorities()
        );

        SecurityContext context = createAndSetSecurityContext(authentication);
        HttpSession newSession = createNewSession(request, context);
        response.addCookie(sessionProperties.createSessionCookie(newSession));
        UserDTO userDTO = userMapper.toDTO(user, avatarStorageService);

        return ApiResponse.success("Logged in successfully", userDTO);
    }

    /**
     * Logs out the user by invalidating their session and clearing security context.
     * <p>Ensures the session cookie is expired and the user is securely logged out.</p>
     *
     * @param request  the HTTP request containing the user's session
     * @param response the HTTP response to modify with an expired session cookie
     */
    public ResponseEntity<? extends ApiResponse<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session == null)
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "User is not logged in");

        response.addCookie(sessionProperties.createExpiredSessionCookie(session.getId()));
        session.invalidate();

        SecurityContextHolder.clearContext();
        log.info("User has been logged out");
        return ApiResponse.success("User logged out successfully");
    }

    /**
     * Confirms the email address associated with a verification token.
     * Validates the token and activates the user if valid.
     *
     * @param token the verification token to validate for email confirmation
     * @return a ResponseEntity containing the result of the email confirmation
     * as a success or failure response
     */
    @Transactional()
    public ResponseEntity<? extends ApiResponse<?>> confirmEmail(String token) {
        return verificationTokenService.validateToken(token, VerificationTokenType.CONFIRM_EMAIL)
                .map(verificationToken -> {
                    User user = verificationToken.getUser();
                    activateUser(user);
                    verificationTokenService.deleteTokenByUser(user);
                    return ApiResponse.success("Your email has been successfully verified");
                })
                .orElse(ApiResponse.failure(ApiCode.INVALID_VERIFICATION_TOKEN, "Invalid or expired verification token"));
    }


    /**
     * Sends a password reset verification token to the user's email if the email belongs to an active user.
     *
     * @param email The email address of the user requesting a password reset.
     * @return A ResponseEntity containing an ApiResponse indicating the success or failure of the operation.
     */
    public ResponseEntity<? extends ApiResponse<?>> sendVerifyPasswordResetToken(String email) {
        return userRepo.findActiveUserByEmail(email)
                .map(user -> {
                    CompletableFuture.runAsync(() -> {
                        String UUIDToken = verificationTokenService.generateVerificationToken(
                                user, VerificationTokenType.PASSWORD_RESET);
                        emailService.sendMailAsync(user.getEmail(), VerificationTokenType.PASSWORD_RESET, UUIDToken);
                    });
                    return ApiResponse.success("Password Reset Token has been successfully sent to your email");
                })
                .orElse(ApiResponse.failure(ApiCode.INVALID_VERIFICATION_TOKEN, "User with the specified email does not exist or is not active"));
    }

    /**
     * Verifies the validity of a password reset token.
     *
     * @param token the password reset token to be validated.
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} indicating
     * whether the token is valid or invalid/expired.
     */
    public ResponseEntity<? extends ApiResponse<?>> verifyPasswordResetToken(String token) {
        boolean isValid = verificationTokenService.validateToken(token, VerificationTokenType.PASSWORD_RESET).isPresent();

        return isValid
                ? ApiResponse.success("Your Password Reset Token has been successfully verified")
                : ApiResponse.failure(ApiCode.INVALID_VERIFICATION_TOKEN, "Invalid or expired verification token");
    }


    /**
     * Handles the process of changing a user's password using a verification token.
     *
     * @param passwordResetDTO the data transfer object containing the token and new password.
     * @return a ResponseEntity with an ApiResponse indicating success or failure of the operation.
     */
    @Transactional()
    public ResponseEntity<? extends ApiResponse<?>> changePassword(@Valid PasswordResetDTO passwordResetDTO) {
        return verificationTokenService.validateToken(passwordResetDTO.getToken(), VerificationTokenType.PASSWORD_RESET)
                .map(token -> {
                    User user = token.getUser();
                    user.setPassword(passwordEncoder.encode(passwordResetDTO.getPassword()));
                    userRepo.save(user);
                    verificationTokenService.deleteTokenByUser(user);
                    userService.updateUserSession(user);
                    return ApiResponse.success("Your password has been successfully reset");
                })
                .orElse(ApiResponse.failure(ApiCode.INVALID_VERIFICATION_TOKEN, "Invalid or expired verification token"));
    }


    /**
     * Generates a 2-Factor Authentication (2FA) setup for the logged-in user.
     * <p>
     * The method creates a TOTP secret, encrypts it, and generates recovery codes
     * for the user. It then stores the encrypted secret and recovery codes in the
     * user's record and constructs a URI for the 2FA setup.
     *
     * @param loggedInUser The currently authenticated user for whom 2FA is being generated.
     * @return A {@code ResponseEntity} containing a map with the generated 2FA URI and recovery codes
     * on success, or an error message on failure.
     */
    public ResponseEntity<? extends ApiResponse<?>> generate2FA(UserPrincipal loggedInUser) {
        try {
            User user = loggedInUser.getUser();

            if (user.isTwoFactorEnabled())
                throw new TwoFactorAuthAlreadyEnabledException("2FA is already enabled for this account");

            byte[] secretBytes = SecretGenerator.generate();
            String base32Secret = new Base32().encodeAsString(secretBytes);

            String encryptedSecret = encryptionService.encrypt(base32Secret);

            List<String> recoveryCodes = generateRecoveryCodes();
            String encodedRecoveryCodes = encodeRecoveryCodes(recoveryCodes);

            user.setTwoFactorSecret(encryptedSecret);
            user.setTwoFactorRecoveryCodes(encodedRecoveryCodes);
            userRepo.save(user);

            TOTPGenerator generator = getTOTPGenerator(base32Secret);

            URI URI = generator.getURI("Zovo", user.getEmail());
            userService.updateUserSession(user);

            return ApiResponse.success("2FA QR code and recovery codes generated successfully",
                    Map.of("uri", URI.toString(),
                            "recoveryCodes", recoveryCodes));
        } catch (URISyntaxException e) {
            return ApiResponse.failure(ApiCode.INTERNAL_SERVER_ERROR, "Error occurred while generating 2FA URI");
        }
    }

    public ResponseEntity<? extends ApiResponse<?>> verify2FA(User user, TwoFaCodeDTO twoFaCodeDTO) {
        String encryptedSecret = user.getTwoFactorSecret();

        if (encryptedSecret == null || encryptedSecret.isEmpty()) {
            return ApiResponse.failure(ApiCode.TWO_FACTOR_AUTH_NOT_ENABLED, "2FA is not enabled for this account.");
        }
        String base32Secret = encryptionService.decrypt(encryptedSecret);
        TOTPGenerator generator = getTOTPGenerator(base32Secret);

        boolean isValid = generator.verify(twoFaCodeDTO.getCode());
        if (isValid) {
            return ApiResponse.success("2FA verification successful");
        } else {
            return ApiResponse.failure(ApiCode.INVALID_TWO_FACTOR_CODE, "Invalid 2FA code");
        }
    }

    /**
     * Enables Two-Factor Authentication (2FA) for the currently logged-in user.
     * <p>
     * Validates the provided 2FA code and updates the user's account settings accordingly.
     * </p>
     *
     * @param loggedInUser The {@code UserPrincipal} representing the authenticated user.
     * @param twoFaCodeDTO The {@code TwoFaCodeDTO} containing the 2FA code to verify.
     * @return A {@code ResponseEntity} with an {@code ApiResponse} indicating the success or failure of 2FA enablement.
     */
    public ResponseEntity<? extends ApiResponse<?>> enable2FA(UserPrincipal loggedInUser, TwoFaCodeDTO twoFaCodeDTO) {
        User user = loggedInUser.getUser();

        if (user.isTwoFactorEnabled())
            throw new TwoFactorAuthAlreadyEnabledException("2FA is already enabled for this account");

        String encryptedSecret = user.getTwoFactorSecret();

        if (encryptedSecret == null || encryptedSecret.isEmpty()) {
            return ApiResponse.failure(ApiCode.TWO_FACTOR_AUTH_NOT_ENABLED, "2FA is not enabled for this account.");
        }
        String base32Secret = encryptionService.decrypt(encryptedSecret);
        TOTPGenerator generator = getTOTPGenerator(base32Secret);

        boolean isValid = generator.verify(twoFaCodeDTO.getCode());

        if (isValid) {
            user.setTwoFactorEnabled(true);
            userRepo.save(user);
            userService.updateUserSession(user);

            CompletableFuture.runAsync(() ->
                    emailService.sendMailAsync(user.getEmail(), VerificationTokenType.TWO_FACTOR_AUTH_ENABLED, null)
            );

            log.info("2FA successfully enabled for user: {}", user.getEmail());
            return ApiResponse.success("2FA has been enabled successfully");
        } else {
            log.warn("Failed 2FA activation attempt for user: {}", user.getEmail());
            return ApiResponse.failure(ApiCode.INVALID_TWO_FACTOR_CODE, "Invalid 2FA code");
        }
    }


    /**
     * Disables Two-Factor Authentication (2FA) for the logged-in user.
     *
     * @param loggedInUser the currently authenticated user's details.
     * @return a ResponseEntity containing an ApiResponse indicating the success or failure of the operation.
     */
    public ResponseEntity<? extends ApiResponse<?>> disable2FA(UserPrincipal loggedInUser) {
        User user = loggedInUser.getUser();

        if (!user.isTwoFactorEnabled()) {
            return ApiResponse.failure(ApiCode.TWO_FACTOR_AUTH_NOT_ENABLED, "2FA is not currently enabled for this account");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorRecoveryCodes(null);

        userRepo.save(user);
        userService.updateUserSession(user);


        CompletableFuture.runAsync(() ->
                emailService.sendMailAsync(user.getEmail(), VerificationTokenType.TWO_FACTOR_AUTH_DISABLED, null)
        );

        log.info("2FA disabled for user: {}", user.getEmail());
        return ApiResponse.success("2FA has been disabled successfully");
    }









    /**
     * Generates a two-factor authentication challenge if it is enabled for the user.
     *
     * @param user      The user attempting to authenticate.
     * @param provider  The 2FA provider being used (e.g., authenticator app, email).
     * @param redirectURI An optional redirect URI for web-based 2FA flow.
     * @return A {@code ResponseEntity} with the appropriate response, either a 2FA challenge or null if 2FA is not enabled.
     */
    public ResponseEntity<? extends ApiResponse<?>> generateTwoFactorAuthChallengeIfEnabled(User user, String provider, String redirectURI) {
        if (user.isTwoFactorEnabled()) {
            String token = UUID.randomUUID().toString();

            redisStorageService.set2FASession(token, user.getEmail());
            log.info("2FA is enabled for user: {}", user.getEmail());

            TwoFaChallengeEnabledDTO twoFaChallengeEnabledDTO = new TwoFaChallengeEnabledDTO(token, provider);
            Map<String, Object> responseData = getStringObjectMap(twoFaChallengeEnabledDTO);

            if(redirectURI != null && !redirectURI.isBlank())
                return ApiResponse.redirect(redirectURI, responseData);

            return ApiResponse.success("Please enter your 2FA code to complete login", ApiCode.LOGIN_NEEDS_2FA, responseData);
        }
        return null;
    }


    /**
     * Converts the fields of the given object into a map with field names as keys
     * and their values as map values.
     *
     * @param <T> the type of the input object
     * @param object the object to be converted into a map
     * @return a map containing field names as keys and corresponding field values
     */
    public <T> Map<String, Object> getStringObjectMap(T object) {
        Map<String, Object> response = new HashMap<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                response.put(field.getName(), field.get(object));
            } catch (IllegalAccessException e) {
                log.error("Error accessing field: {}", field.getName(), e);
            }
        }
        return response;
    }


    /**
     * Saves a new user based on the provided registration details, assigns a default role,
     * and persists it in the database.
     *
     * @param requestDTO The data transfer object containing the user's registration information.
     * @return The saved {@link User} entity after persisting it in the database.
     */
    private User saveNewUser(UserRegisterDTO requestDTO) {
        String defaultRole = "ROLE_USER";
        Role userRole = redisStorageService.getRole(defaultRole);
        if(userRole == null) {
            userRole = roleRepo.findByName(defaultRole).orElseThrow(() ->
                    new RoleNotFoundException("Role with the specified name was not found"));
            redisStorageService.setRole(defaultRole, userRole);
        }

        User user = userMapper.toUser(requestDTO);
        user.setStatus("offline");
        user.setRoles(Set.of(userRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepo.save(user);
        userService.updateUserSession(user);
        return user;
    }


    /**
     * Activates the given user by enabling their account and marking it as active.
     * <p>
     * The user entity is updated and persisted to the repository.
     * A log entry is created to indicate the activation.
     *
     * @param user the user to be activated, must not be null
     */
    private void activateUser(User user) {
        user.setEnabled(true);
        user.setActive(true);
        userRepo.save(user);
        userService.updateUserSession(user);

        log.info("User '{}' has been activated", user.getEmail());
    }


    /**
     * Creates a new {@link SecurityContext}, sets the provided {@link Authentication}
     * into it, and updates the {@link SecurityContextHolder} with this context.
     *
     * @param authentication the {@link Authentication} object to set in the security context
     * @return the created {@link SecurityContext} with the provided authentication
     */
    public SecurityContext createAndSetSecurityContext(Authentication authentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return context;
    }


    /**
     * Creates a new HTTP session, invalidating the existing one if present,
     * and associates it with the provided {@link SecurityContext}.
     *
     * @param request the {@link HttpServletRequest} containing the current session
     * @param context the {@link SecurityContext} to associate with the new session
     * @return the newly created {@link HttpSession} instance
     */
    public HttpSession createNewSession(HttpServletRequest request, SecurityContext context) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return newSession;
    }


    public ResponseEntity<? extends ApiResponse<?>> getTwoFaStatus(User user) {
        return ApiResponse.success(user.isTwoFactorEnabled() ? "Enabled" : "Disabled");
    }

    /**
     * Verifies the provided 2FA code for the given user. It checks if the code is a valid recovery
     * code or a time-based TOTP code.
     *
     * <p>This method consumes a recovery code if it is valid.
     *
     * @param user The user for whom the 2FA code is being verified.
     * @param code The 2FA code to validate.
     * @return <code>true</code> if the code is valid or matches a recovery code; <code>false</code> otherwise.
     */
    private boolean verify2FACode(User user, String code) {
        if (isValidRecoveryCode(user, code)) {
            consumeRecoveryCode(user, code);
            return true;
        }

        String encryptedSecret = user.getTwoFactorSecret();
        if (encryptedSecret == null || encryptedSecret.isEmpty()) {
            return false;
        }

        String base32Secret = encryptionService.decrypt(encryptedSecret);
        TOTPGenerator generator = getTOTPGenerator(base32Secret);

        return generator.verify(code);
    }


    /**
     * Generates a TOTPGenerator configured with the provided secret key.
     *
     * @param secret The base32-encoded secret key used for TOTP generation.
     * @return A configured instance of TOTPGenerator.
     */
    private TOTPGenerator getTOTPGenerator(String secret) {
        byte[] secretBytes = new Base32().decode(secret);
        return new TOTPGenerator.Builder(secretBytes)
                .withHOTPGenerator(builder -> {
                    builder.withAlgorithm(HMACAlgorithm.SHA256);
                    builder.withPasswordLength(6);
                })
                .withPeriod(Duration.ofSeconds(30))
                .build();
    }


    /**
     * Generates a list of secure recovery codes, each consisting of 8 alphanumeric characters.
     * <p>
     * The method generates 10 unique recovery codes using random bytes, encodes them,
     * and ensures their length is exactly 8 characters, appending digits if needed.
     * <p>
     * Useful for cases requiring secure, user-specific recovery tokens.
     *
     * @return a {@link List} of 10 unique recovery codes as {@link String}.
     */
    private List<String> generateRecoveryCodes() {
        List<String> recoveryCodes = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 10; i++) {
            byte[] bytes = new byte[6];
            random.nextBytes(bytes);

            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            String code;
            if (encoded.length() >= 8) {
                code = encoded.substring(0, 8).toUpperCase();
            } else {
                StringBuilder sb = new StringBuilder(encoded.toUpperCase());
                while (sb.length() < 8) {
                    sb.append(random.nextInt(10));
                }
                code = sb.toString();
            }
            recoveryCodes.add(code);
        }

        return recoveryCodes;
    }


    /**
     * Encodes a list of recovery codes using BCrypt hashing.
     *
     * @param recoveryCodes the list of plain recovery codes to be hashed
     * @return a single comma-separated string of hashed recovery codes
     */
    private String encodeRecoveryCodes(List<String> recoveryCodes) {
        return recoveryCodes.stream()
                .map(code -> BCrypt.hashpw(code, BCrypt.gensalt()))
                .collect(Collectors.joining(","));
    }


    /**
     * Validates if the given recovery code matches any stored hashed recovery codes for the user.
     *
     * @param user The user whose recovery codes are to be checked.
     * @param code The recovery code to validate.
     * @return <code>true</code> if the recovery code is valid; <code>false</code> otherwise.
     */
    private boolean isValidRecoveryCode(User user, String code) {
        if (user.getTwoFactorRecoveryCodes() == null) {
            return false;
        }

        return Arrays.stream(user.getTwoFactorRecoveryCodes().split(","))
                .anyMatch(storedHash -> BCrypt.checkpw(code, storedHash));
    }


    /**
     * Consumes a provided recovery code by removing it from the user's stored recovery codes.
     * <p>
     * If the provided code matches a stored hash, it is removed from the list.
     * Remaining recovery codes are saved back to the user entity.
     *
     * @param user The user whose recovery codes are to be processed.
     * @param code The recovery code to be consumed.
     */
    private void consumeRecoveryCode(User user, String code) {
        List<String> remainingCodes = Arrays.stream(user.getTwoFactorRecoveryCodes().split(","))
                .filter(storedHash -> !BCrypt.checkpw(code, storedHash))
                .collect(Collectors.toList());

        user.setTwoFactorRecoveryCodes(String.join(",", remainingCodes));
        userRepo.save(user);
        userService.updateUserSession(user);
    }

    /**
     * Retrieves available OAuth providers and their authorization URLs.
     *
     * @return a ResponseEntity containing an ApiResponse with a map of providers and their URLs.
     */
    public ResponseEntity<? extends ApiResponse<?>> getOAuthProviders() {
        Map<String, String> providers = new HashMap<>();
        providers.put("google", "/api/v1/auth/oauth2/authorize/google");
        providers.put("github", "/api/v1/auth/oauth2/authorize/github");

        return ApiResponse.success(providers);
    }

}
