package me.abouabra.zovo.services;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.configs.SessionProperties;
import me.abouabra.zovo.dtos.PasswordResetDTO;
import me.abouabra.zovo.dtos.UserLoginDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.enums.VerificationTokenType;
import me.abouabra.zovo.exceptions.RoleNotFoundException;
import me.abouabra.zovo.exceptions.UserAlreadyExistsException;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.Role;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.utils.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;



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

    private final Map<String, Role> roleCache = new ConcurrentHashMap<>();


    /**
     * Activates a user by enabling their account and setting them to active status.
     *
     * @param user The user to be activated.
     */
    private void activateUser(User user) {
        user.setEnabled(true);
        user.setActive(true);
        userRepo.save(user);
        log.info("User '{}' has been activated", user.getEmail());
    }


    /**
     * Registers a new user with the provided details and sends a verification email.
     * <p>
     * Ensures that the username or email is not already in use. Assigns the default user role
     * and asynchronously sends a verification email upon successful registration.
     *
     * @param requestDTO the data transfer object containing user registration details
     * @return a {@code ResponseEntity} containing a success or error message
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
     * Saves a new user to the repository with the specified role and encoded password.
     *
     * @param requestDTO The data transfer object containing user registration details.
     * @param userRole The role to assign to the new user.
     * @return The saved {@link User} entity.
     */
    private User saveNewUser(UserRegisterDTO requestDTO, Role userRole) {
        User user = userMapper.toUser(requestDTO);
        user.setRoles(Set.of(userRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    /**
     * Authenticates a user and creates a new session after successful login.
     *
     * @param loginDTO Data transfer object containing user login credentials.
     * @param request  HTTP request used to retrieve the current session.
     * @param response HTTP response used to add a session cookie.
     * @return {@link UserResponseDTO} containing the authenticated user's information.
     */
    @Transactional
    public UserResponseDTO login(UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword())
        );

        SecurityContext context = createAndSetSecurityContext(authentication);

        HttpSession newSession = createNewSession(request, context);

        response.addCookie(sessionProperties.createSessionCookie(newSession));

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userMapper.toDTO(userPrincipal.getUser());
    }

    /**
     * Creates a new {@link SecurityContext}, sets the given {@link Authentication} within it,
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
     * Creates a new HTTP session and invalidates the previous session if it exists.
     * Sets the provided security context in the new session.
     *
     * @param request the {@link HttpServletRequest} to retrieve or create the session.
     * @param context the {@link SecurityContext} to attach to the new session.
     * @return the newly created {@link HttpSession} with the security context set.
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
     * Logs out the user by invalidating the session and clearing the security context.
     *
     * <p>Ensures the user's session is terminated and removes any associated cookies.
     *
     * @param request  the HTTP servlet request containing session details
     * @param response the HTTP servlet response used to modify cookies
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
     * Confirms email verification using the provided token.
     *
     * @param token the email verification token to validate
     * @return a response entity containing a success or error message
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
     * Sends a password reset token to the specified email if the user is active.
     * <p>
     * The method generates a unique token and sends it asynchronously to the user's email.
     * </p>
     *
     * @param email the email address of the user requesting the password reset.
     * @return a ResponseEntity containing a success message if the token is sent, or an error message if the user does not exist or is not active.
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
     * <p>Checks if the provided token is valid and not expired.
     *
     * @param token the password reset token to be verified.
     * @return a {@code ResponseEntity} containing a success message if the token is valid,
     *         or an error message if the token is invalid or expired.
     */
    public ResponseEntity<Map<String, String>> verifyPasswordResetToken(String token) {
        boolean isValid = verificationTokenService.validateToken(token, VerificationTokenType.PASSWORD_RESET).isPresent();

        return isValid
                ? responseBuilder.success("Your Password Reset Token has been successfully verified")
                : responseBuilder.error("Invalid or expired verification token");
    }


    /**
     * Changes the password of a user based on the provided password reset token and new password.
     *
     * <p>Validates the token, updates the user's password, and deletes the token if valid. If the token is invalid or expired, an error is returned.</p>
     *
     * @param passwordResetDTO The data transfer object containing the password reset token and new password.
     * @return A {@code ResponseEntity} containing a map with a success or error message.
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
}
