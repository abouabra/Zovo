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
import me.abouabra.zovo.enums.VerificationTokenType;
import me.abouabra.zovo.exceptions.RateLimitedException;
import me.abouabra.zovo.exceptions.RoleNotFoundException;
import me.abouabra.zovo.exceptions.UserAlreadyExistsException;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.Role;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.utils.ResponseBuilder;
import org.apache.commons.codec.binary.Base32;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * The {@code AuthService} class handles authentication and authorization tasks.
 * <p>
 * It includes user registration, login, email verification, password management,
 * and session handling functionalities.
 * </p>
 * <p>
 * Dependencies like repositories, mappers, and utilities are used for seamless
 * communication with underlying services and data persistence layers.
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
    private final ResponseBuilder responseBuilder;
    private final RateLimitingService rateLimitingService;
    private final SecretEncryptionService encryptionService;

    // TODO: Replace those maps with redis later
    private final Map<String, Role> roleCache = new ConcurrentHashMap<>();
    private final Map<String, String> twoFactorSessions = new HashMap<>();

    /**
     * Registers a new user based on the provided user registration details.
     * <p>
     * If the username or email is already in use, an exception will be thrown.
     * Upon successful registration, a verification email is sent asynchronously to the user.
     *
     * @param requestDTO the user registration details containing username, email, and other required fields
     * @return a response entity containing a success message with a map of details
     * @throws UserAlreadyExistsException if the username or email is already in use
     * @throws RoleNotFoundException if the default user role is not found
     */
    public ResponseEntity<Map<String, String>> register(@Valid UserRegisterDTO requestDTO) {
        if (userRepo.existsByUsernameOrEmail(requestDTO.getUsername(), requestDTO.getEmail())) {
            if (userRepo.findUserByUsername(requestDTO.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username '%s' is already taken".formatted(requestDTO.getUsername()));
            } else {
                throw new UserAlreadyExistsException("Email '%s' is already taken".formatted(requestDTO.getEmail()));
            }
        }

        Role userRole = roleCache.computeIfAbsent("ROLE_USER", roleName ->
                roleRepo.findByName(roleName).orElseThrow(() ->
                        new RoleNotFoundException("Role with the specified name was not found")));


        User user = saveNewUser(requestDTO, userRole);

        CompletableFuture.runAsync(() -> {
            String UUIDToken = verificationTokenService.generateVerificationToken(user, VerificationTokenType.CONFIRM_EMAIL);
            emailService.sendMailAsync(user.getEmail(), VerificationTokenType.CONFIRM_EMAIL, UUIDToken);
        });

        return responseBuilder.success("User has been registered successfully. Please check your email for verification link");
    }

    /**
     * Handles user login functionality, including authentication, rate-limiting checks,
     * and two-factor authentication if enabled.
     *
     * @param loginDTO      A DTO containing login credentials such as email and password.
     * @param request       The HttpServletRequest object for the current login attempt.
     * @param response      The HttpServletResponse object for setting cookies or responses.
     * @return A ResponseEntity containing the login result, including user details,
     *                      2FA requirements, or error messages.
     */
    @Transactional
    public ResponseEntity<?> login(UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (rateLimitingService.isRateLimited(loginDTO.getEmail(), "login")) {
                long timeRemaining = rateLimitingService.getLockoutDurationRemaining(
                        loginDTO.getEmail(), "login");

                String message = String.format("Too many failed attempts. Try again in %d minutes.", (timeRemaining / 60));
                throw new RateLimitedException(message);
            }

            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
            );
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            // Check if 2FA is enabled
            if (user.isTwoFactorEnabled()) {
                // Generate a temporary token valid for 5 minutes
                String token = UUID.randomUUID().toString();

                // Store email associated with this temporary token (in-memory or Redis in production)
                twoFactorSessions.put(token, user.getEmail());
                log.info("2FA is enabled for user: {}", user.getEmail());

                // Return response indicating 2FA is required
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("status", "2FA_REQUIRED");
                responseMap.put("token", token);
                responseMap.put("message", "Please enter your 2FA code to complete login");

                return ResponseEntity.ok(responseMap);
            }

            SecurityContext context = createAndSetSecurityContext(authentication);

            HttpSession newSession = createNewSession(request, context);

            rateLimitingService.resetAttempts(loginDTO.getEmail(), "login");

            response.addCookie(sessionProperties.createSessionCookie(newSession));

            UserResponseDTO responseDTO = userMapper.toDTO(userPrincipal.getUser());

            return ResponseEntity.ok(responseDTO);
        } catch (BadCredentialsException e) {
            rateLimitingService.recordFailedAttempt(loginDTO.getEmail(), "login");
            throw new BadCredentialsException(e.getMessage());
        }
    }

    /**
     * Handles user login with Two-Factor Authentication (2FA). Validates the provided 2FA code
     * and token and establishes a new authenticated session on success.
     *
     * @param tokenAndCodeDTO an object containing the temporary token and 2FA code for validation.
     * @param request the HTTP servlet request used for session and security context updates.
     * @param response the HTTP servlet response used to set session cookies.
     * @return a ResponseEntity containing the user information if login is successful,
     *         or an error message if validation fails or an exception occurs.
     */
    @Transactional
    public ResponseEntity<?> loginWith2FA(@RequestBody TwoFaTokenAndCodeDTO tokenAndCodeDTO, HttpServletRequest request, HttpServletResponse response) {
        String tempToken = tokenAndCodeDTO.getToken();
        String email = twoFactorSessions.get(tempToken);

        if (email == null)
            return responseBuilder.error("Invalid or expired token. Please login again.");

        if (rateLimitingService.isRateLimited(email, "2fa_verify")) {
            long timeRemaining = rateLimitingService.getLockoutDurationRemaining(email, "login");

            String message = String.format("Too many failed attempts. Try again in %d minutes.", (timeRemaining / 60));
            throw new RateLimitedException(message);
        }

        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> {
                    rateLimitingService.recordFailedAttempt(email, "2fa_verify");
                    return new RuntimeException("User not found");
                });

        // Verify 2FA code (including support for recovery codes)
        boolean isValid = verify2FACode(user, tokenAndCodeDTO.getCode());

        if (!isValid) {
            rateLimitingService.recordFailedAttempt(email, "2fa_verify");
            return responseBuilder.error("Invalid 2FA code");
        }

        twoFactorSessions.remove(tempToken);

        rateLimitingService.resetAttempts(email, "2fa_verify");
        rateLimitingService.resetAttempts(email, "login");

        // Create an authentication object
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(user),
                null,
                new UserPrincipal(user).getAuthorities()
        );

        SecurityContext context = createAndSetSecurityContext(authentication);

        HttpSession newSession = createNewSession(request, context);

        response.addCookie(sessionProperties.createSessionCookie(newSession));

        UserResponseDTO responseDTO = userMapper.toDTO(user);

        return ResponseEntity.ok(responseDTO);

    }

    /**
     * Logs out the current user by invalidating the session and clearing the security context.
     *
     * <p>This method ensures the session cookie is expired and the user's authentication is cleared.
     *
     * @param request  the HttpServletRequest from the client
     * @param response the HttpServletResponse to send feedback to the client
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            response.addCookie(sessionProperties.createExpiredSessionCookie(session.getId()));
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        log.info("User has been logged out");
    }

    /**
     * Confirms the email address of a user using the provided token.
     *
     * <p>Validates the token, activates the user's account upon success, and deletes
     * the associated verification token.</p>
     *
     * @param token the email verification token to be validated
     * @return a ResponseEntity containing a success or error message
     */
    @Transactional()
    public ResponseEntity<Map<String, String>> confirmEmail(String token) {
        return verificationTokenService.validateToken(token, VerificationTokenType.CONFIRM_EMAIL)
                .map(verificationToken -> {
                    User user = verificationToken.getUser();
                    activateUser(user);
                    verificationTokenService.deleteTokenByUser(user);
                    return responseBuilder.success("Your email has been successfully verified");
                })
                .orElse(responseBuilder.error("Invalid or expired verification token"));
    }

    /**
     * Sends a verification token for password reset to the specified email address if the user exists and is active.
     *
     * @param email the email address of the user to send the password-reset token.
     * @return a {@link ResponseEntity} containing a map with a success or error message.
     */
    public ResponseEntity<Map<String, String>> sendVerifyPasswordResetToken(String email) {
        return userRepo.findActiveUserByEmail(email)
                .map(user -> {
                    CompletableFuture.runAsync(() -> {
                        String UUIDToken = verificationTokenService.generateVerificationToken(
                                user, VerificationTokenType.PASSWORD_RESET);
                        emailService.sendMailAsync(user.getEmail(), VerificationTokenType.PASSWORD_RESET, UUIDToken);
                    });
                    return responseBuilder.success("Password Reset Token has been successfully sent to your email");
                })
                .orElse(responseBuilder.error("User with the specified email does not exist or is not active"));
    }

    /**
     * Verifies the validity of a password reset token.
     *
     * @param token The password reset token to be verified.
     * @return A ResponseEntity containing a success message if the token is valid,
     *         or an error message if the token is invalid or expired.
     */
    public ResponseEntity<Map<String, String>> verifyPasswordResetToken(String token) {
        boolean isValid = verificationTokenService.validateToken(token, VerificationTokenType.PASSWORD_RESET).isPresent();

        return isValid
                ? responseBuilder.success("Your Password Reset Token has been successfully verified")
                : responseBuilder.error("Invalid or expired verification token");
    }

    /**
     * Changes the password of a user based on the provided reset token and new password details.
     *
     * @param passwordResetDTO The data transfer object containing the reset token and the new password.
     * @return A ResponseEntity containing a success or error message as a map.
     */
    @Transactional()
    public ResponseEntity<Map<String, String>> changePassword(@Valid PasswordResetDTO passwordResetDTO) {
        return verificationTokenService.validateToken(passwordResetDTO.getToken(), VerificationTokenType.PASSWORD_RESET)
                .map(token -> {
                    User user = token.getUser();
                    user.setPassword(passwordEncoder.encode(passwordResetDTO.getPassword()));
                    userRepo.save(user);
                    verificationTokenService.deleteTokenByUser(user);
                    return responseBuilder.success("Your password has been successfully reset");
                })
                .orElse(responseBuilder.error("Invalid or expired verification token"));
    }

    /**
     * Generates a two-factor authentication (2FA) setup for the provided user.
     * This includes generating a secret, recovery code, and a URI for QR code generation.
     *
     * @param loggedInUser The currently authenticated user for whom 2FA is being generated.
     * @return ResponseEntity containing the 2FA setup details such as URI and recovery codes, or an error response in case of failure.
     */
    public ResponseEntity<?> generate2FA(UserPrincipal loggedInUser) {
        try {
            byte[] secretBytes = SecretGenerator.generate();
            String base32Secret = new Base32().encodeAsString(secretBytes);

            String encryptedSecret = encryptionService.encrypt(base32Secret);


            // Generate recovery codes
            List<String> recoveryCodes = generateRecoveryCodes();
            String encodedRecoveryCodes = encodeRecoveryCodes(recoveryCodes);


            loggedInUser.getUser().setTwoFactorSecret(encryptedSecret);
            loggedInUser.getUser().setTwoFactorRecoveryCodes(encodedRecoveryCodes);
            userRepo.save(loggedInUser.getUser());

            TOTPGenerator generator = getTOTPGenerator(base32Secret);

            URI URI = generator.getURI("Zovo", loggedInUser.getUser().getEmail());

            return ResponseEntity.ok(Map.of(
                    "uri", URI.toString(),
                    "recoveryCodes", recoveryCodes
            ));
        } catch (URISyntaxException e) {
            return responseBuilder.error("Error occurred while generating 2FA URI");
        }
    }

    /**
     * Enables two-factor authentication (2FA) for the logged-in user if a valid 2FA code is provided.
     *
     * <p>This method verifies the provided 2FA code against the user's secret. If valid,
     * 2FA is activated and persisted for the user.
     *
     * @param loggedInUser The currently authenticated user whose 2FA setting is to be updated.
     * @param twoFaCodeDTO The DTO containing the 2FA code to verify.
     * @return A {@code ResponseEntity<?>} containing a success message if 2FA is enabled,
     *         or an error message if the code is invalid or 2FA cannot be activated.
     */
    public ResponseEntity<?> enable2FA(UserPrincipal loggedInUser, TwoFaCodeDTO twoFaCodeDTO) {
        String encryptedSecret = loggedInUser.getUser().getTwoFactorSecret();

        if (encryptedSecret == null || encryptedSecret.isEmpty()) {
            return responseBuilder.error("2FA is not enabled for this account.");
        }
        String base32Secret = encryptionService.decrypt(encryptedSecret);
        TOTPGenerator generator = getTOTPGenerator(base32Secret);

        boolean isValid = generator.verify(twoFaCodeDTO.getCode());

        if (isValid) {
            loggedInUser.getUser().setTwoFactorEnabled(true);
            userRepo.save(loggedInUser.getUser());
            log.info("2FA successfully enabled for user: {}", loggedInUser.getUser().getEmail());
            return responseBuilder.success("2FA has been enabled successfully");
        } else {
            log.warn("Failed 2FA activation attempt for user: {}", loggedInUser.getUser().getEmail());
            return responseBuilder.error("Invalid 2FA code");
        }
    }

    /**
     * Disables Two-Factor Authentication (2FA) for the given user if it is currently enabled.
     *
     * @param loggedInUser The authenticated user requesting to disable 2FA.
     *                     Must be a valid {@link UserPrincipal} object.
     * <p>
     * @return A {@link ResponseEntity} containing success message if 2FA is disabled,
     *         or an error message if 2FA was not enabled.
     */
    public ResponseEntity<?> disable2FA(UserPrincipal loggedInUser) {
        User user = loggedInUser.getUser();

        if (!user.isTwoFactorEnabled()) {
            return responseBuilder.error("2FA is not currently enabled for this account");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorRecoveryCodes(null);

        userRepo.save(user);

        log.info("2FA disabled for user: {}", user.getEmail());
        return responseBuilder.success("2FA has been disabled successfully");
    }

    /**
     * Saves a new user entity into the database with specified details and role.
     *
     * @param requestDTO the data transfer object containing user registration details.
     * @param userRole the role to be assigned to the new user.
     * @return the newly saved User entity.
     */
    private User saveNewUser(UserRegisterDTO requestDTO, Role userRole) {
        User user = userMapper.toUser(requestDTO);
        user.setRoles(Set.of(userRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    /**
     * Activates the given user by enabling and setting them as active,
     * then persists the changes to the repository.
     *
     * @param user the user to be activated
     */
    private void activateUser(User user) {
        user.setEnabled(true);
        user.setActive(true);
        userRepo.save(user);
        log.info("User '{}' has been activated", user.getEmail());
    }

    /**
     * Creates a new {@link SecurityContext}, sets the given {@link Authentication},
     * and updates the {@link SecurityContextHolder}.
     *
     * @param authentication the {@link Authentication} object to set in the security context
     * @return the newly created and configured {@link SecurityContext}
     */
    private SecurityContext createAndSetSecurityContext(Authentication authentication) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return context;
    }

    /**
     * Creates a new HTTP session, invalidating the old session if it exists, and associates
     * the provided SecurityContext with the new session.
     *
     * @param request the HttpServletRequest to get or create the session.
     * @param context the SecurityContext to be stored in the new session.
     * @return the newly created HttpSession with the SecurityContext attribute set.
     */
    private HttpSession createNewSession(HttpServletRequest request, SecurityContext context) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return newSession;
    }

    /**
     * Verifies the provided two-factor authentication (2FA) code, which can be either
     * a recovery code or a time-based one-time password (TOTP).
     *
     * @param user The user whose 2FA code needs to be verified.
     * @param code The 2FA code provided for authentication.
     * @return {@code true} if the 2FA code is valid; {@code false} otherwise.
     */
    private boolean verify2FACode(User user, String code) {
        if (isValidRecoveryCode(user, code)) {
            // Consume the recovery code
            consumeRecoveryCode(user, code);
            return true;
        }

        // Otherwise check TOTP code
        String encryptedSecret = user.getTwoFactorSecret();
        if (encryptedSecret == null || encryptedSecret.isEmpty()) {
            return false;
        }

        // Decrypt the secret using the injected encryption service
        String base32Secret = encryptionService.decrypt(encryptedSecret);
        TOTPGenerator generator = getTOTPGenerator(base32Secret);

        return generator.verify(code);
    }

    /**
     * Creates and returns a configured TOTPGenerator instance.
     *
     * @param secret The secret key, encoded in Base32, used to generate the TOTP.
     * @return A TOTPGenerator configured with the provided secret and settings.
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
     * Generates a list of unique recovery codes, each consisting of 8 characters.
     * <p>
     * The codes are URL-safe, randomly generated, and encoded in Base64. If the encoded length
     * is less than 8 characters, random digits are appended to meet the required length.
     *
     * @return a list of 10 randomly generated recovery codes as Strings.
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
     * Encodes a list of recovery codes using BCrypt hashing and concatenates them into a single string.
     *
     * @param recoveryCodes the list of recovery codes to encode
     * @return a single string containing the hashed recovery codes, separated by commas
     */
    private String encodeRecoveryCodes(List<String> recoveryCodes) { //TODO: understand this
        return recoveryCodes.stream()
                .map(code -> BCrypt.hashpw(code, BCrypt.gensalt()))
                .collect(Collectors.joining(","));
    }

    /**
     * Validates the provided recovery code against the user's stored recovery codes.
     *
     * @param user The user whose recovery codes are to be checked.
     * @param code The recovery code to be validated.
     * @return {@code true} if the code matches a stored recovery code, {@code false} otherwise.
     */
    private boolean isValidRecoveryCode(User user, String code) { //TODO: understand this
        if (user.getTwoFactorRecoveryCodes() == null) {
            return false;
        }

        return Arrays.stream(user.getTwoFactorRecoveryCodes().split(","))
                .anyMatch(storedHash -> BCrypt.checkpw(code, storedHash));
    }

    /**
     * Consumes a recovery code provided by the user by validating it and removing it
     * from the user's stored two-factor recovery codes.
     *
     * @param user the user attempting to consume a recovery code.
     * @param code the recovery code entered by the user for validation.
     */
    private void consumeRecoveryCode(User user, String code) {
        List<String> remainingCodes = Arrays.stream(user.getTwoFactorRecoveryCodes().split(","))
                .filter(storedHash -> !BCrypt.checkpw(code, storedHash))
                .collect(Collectors.toList());

        user.setTwoFactorRecoveryCodes(String.join(",", remainingCodes));
        userRepo.save(user);

//         If few recovery codes left, alert the user
//        if (remainingCodes.size() <= 3) {
//             Send notification to user about low-recovery codes
//            notificationService.sendLowRecoveryCodesAlert(user); //TODO: implement later
//        }
    }

}
