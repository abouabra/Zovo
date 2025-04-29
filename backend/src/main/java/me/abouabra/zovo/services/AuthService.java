package me.abouabra.zovo.services;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.abouabra.zovo.config.SessionProperties;
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
import me.abouabra.zovo.models.VerificationToken;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * The {@code AuthService} class provides authentication and user account management
 * functionalities. It includes methods for user registration, login, logout,
 * email verification, and password reset processes.
 *
 * <p>Key Responsibilities:
 * <ul>
 *   <li>User registration with role assignment and email verification</li>
 *   <li>User login and session management</li>
 *   <li>Email verification and password reset token handling</li>
 *   <li>Password reset and security context updates</li>
 * </ul>
 */
@Service
@AllArgsConstructor
@Getter
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionProperties sessionProperties;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;

    /**
     * Activates a user by enabling their account and marking them as active.
     * The user data is saved to the repository after activation.
     *
     * @param user the user to be activated, containing their account details.
     */
    private void activateUser(User user) {
        user.setEnabled(true);
        user.setActive(true);
        userRepo.save(user);
        log.info("User '{}' has been activated", user.getEmail());
    }


    /**
     * Registers a new user in the system and sends a verification email.
     *
     * <p>Validates the uniqueness of the username and email before proceeding.
     * Assigns a default user role and saves the user with an encoded password.
     * Generates a verification token and sends a verification email to the user.
     *
     * @param requestDTO the DTO containing user registration details, such as username and email.
     * @return a ResponseEntity containing a success status and a message.
     * @throws UserAlreadyExistsException if the username or email is already taken.
     * @throws RoleNotFoundException if the default user role cannot be found.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> register(@Valid UserRegisterDTO requestDTO) {
        if (userRepo.findUserByUsername(requestDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '%s' is already taken".formatted(requestDTO.getUsername()));
        }
        if (userRepo.findUserByEmail(requestDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email '%s' is already taken".formatted(requestDTO.getEmail()));
        }

        User user = userMapper.toUser(requestDTO);

        Role userRole = roleRepo.findByName("ROLE_USER").orElseThrow(() -> new RoleNotFoundException("Role with the specified name was not found"));

        user.setRoles(Set.of(userRole));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);

        String UUIDToken = verificationTokenService.generateVerificationToken(user, VerificationTokenType.CONFIRM_EMAIL);
        emailService.sendMail(user.getEmail(), VerificationTokenType.CONFIRM_EMAIL, UUIDToken);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User has been registered successfully. Please check your email for verification link");

        return ResponseEntity.ok(response);
    }


    /**
     * Authenticates the user and establishes a session along with security context.
     *
     * @param loginDTO The data transfer object containing user login credentials.
     * @param request The HTTP servlet request triggered during login.
     * @param response The HTTP servlet response to send cookies for the session.
     * @return UserResponseDTO containing user information post successful login.
     */
    @Transactional
    public UserResponseDTO login(UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        User user = userMapper.toUser(loginDTO);

        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        Cookie cookie = sessionProperties.createSessionCookie(newSession);
        response.addCookie(cookie);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User authenticatedUser = userPrincipal.getUser();
        return userMapper.toDTO(authenticatedUser);
    }


    /**
     * Logs the user out by invalidating their session, clearing security context,
     * and adding an expired session cookie to the response.
     *
     * @param request  the HTTP request containing session details
     * @param response the HTTP response to set the expired session cookie
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Cookie cookie = sessionProperties.createExpiredSessionCookie(session.getId());
            response.addCookie(cookie);
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        log.info("User has been logged out");
    }

    /**
     * Confirms the email verification process using the provided token.
     * Checks the token's validity, activates the user, and deletes the token.
     *
     * @param token the email verification token to validate.
     * @return a ResponseEntity containing a map with the status and message of the operation.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> confirmEmail(String token) {
        Map<String, String> response = new HashMap<>();

        Optional<VerificationToken> tokenOpt = verificationTokenService.validateToken(
                token, VerificationTokenType.CONFIRM_EMAIL);

        if (tokenOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Invalid or expired verification token");
            return ResponseEntity.badRequest().body(response);
        }

        User user = tokenOpt.get().getUser();
        activateUser(user);

        verificationTokenService.deleteTokenByUser(user);

        response.put("status", "success");
        response.put("message", "Your email has been successfully verified");
        return ResponseEntity.ok(response);
    }


    /**
     * Sends a password reset verification token to a user's email if they exist and their account is active.
     *
     * @param email the email address of the user requesting the password reset.
     * @return a {@link ResponseEntity} containing a map with the status and message of the operation.
     */
    public ResponseEntity<Map<String, String>> sendVerifyPasswordResetToken(String email) {
        Map<String, String> response = new HashMap<>();

        Optional<User> userOpt = userRepo.findUserByEmail(email);
        if (userOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User with the specified email does not exist");
            return ResponseEntity.badRequest().body(response);
        }
        User user = userOpt.get();
        if (!user.isEnabled()) {
            response.put("status", "error");
            response.put("message", "User with the specified email is not active");
            return ResponseEntity.badRequest().body(response);
        }

        String UUIDToken = verificationTokenService.generateVerificationToken(user, VerificationTokenType.PASSWORD_RESET);
        emailService.sendMail(user.getEmail(), VerificationTokenType.PASSWORD_RESET, UUIDToken);

        response.put("status", "success");
        response.put("message", "Password Reset Token has been successfully sent to your email");

        return ResponseEntity.ok(response);
    }

    /**
     * Verifies the provided password reset token to determine its validity.
     *
     * @param token the password reset token to be verified.
     * @return a {@link ResponseEntity} containing a map with the verification status and associated message.
     */
    public ResponseEntity<Map<String, String>> verifyPasswordResetToken(String token) {
        Map<String, String> response = new HashMap<>();

        boolean isValid = verificationTokenService.validateToken(token, VerificationTokenType.PASSWORD_RESET).isPresent();

        if (isValid) {
            response.put("status", "success");
            response.put("message", "Your Password Reset Token has been successfully verified");
            return ResponseEntity.ok(response);
        }

        response.put("status", "error");
        response.put("message", "Invalid or expired verification token");
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Changes the password of a user after validating the provided password reset token.
     * The token is verified, and upon success, the user's password is updated and saved.
     *
     * @param passwordResetDTO a data transfer object containing the reset token and new password.
     * @return a ResponseEntity containing a response map with the status and message of the operation.
     */
    @Transactional
    public ResponseEntity<Map<String, String>> changePassword(@Valid PasswordResetDTO passwordResetDTO) {
        Map<String, String> response = new HashMap<>();
        String uuidToken = passwordResetDTO.getToken();

        Optional<VerificationToken> tokenOpt = verificationTokenService.validateToken(uuidToken, VerificationTokenType.PASSWORD_RESET);

        if (tokenOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Invalid or expired verification token");
            return ResponseEntity.badRequest().body(response);
        }

        User user = tokenOpt.get().getUser();
        user.setPassword(passwordEncoder.encode(passwordResetDTO.getPassword()));
        userRepo.save(user);

        verificationTokenService.deleteTokenByUser(user);

        response.put("status", "success");
        response.put("message", "Your password has been successfully reset");
        return ResponseEntity.ok(response);
    }

}
