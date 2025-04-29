package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.PasswordResetDTO;
import me.abouabra.zovo.dtos.UserLoginDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * The AuthController class handles authentication-related operations such as user registration, login, logout,
 * email confirmation, and password reset. It exposes RESTful endpoints for clients to interact with the authentication services.
 * <p>
 * Endpoints:
 * <ul>
 *     <li>/api/v1/auth/register - User registration</li>
 *     <li>/api/v1/auth/login - User login</li>
 *     <li>/api/v1/auth/logout - User logout</li>
 *     <li>/api/v1/auth/confirm-email - Email confirmation</li>
 *     <li>/api/v1/auth/send-password-reset - Password reset token request</li>
 *     <li>/api/v1/auth/password-reset - Password reset and token verification</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;

    /**
     * Handles user registration by accepting user details, validating them, and registering the user.
     *
     * @param registerDTO a {@code UserRegisterDTO} object containing the user registration details such as username, email, password, and password confirmation.
     * @return a {@code ResponseEntity} containing a {@code UserResponseDTO} with the registered user's details such as ID, username, and email.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return authService.register(registerDTO);
    }

    /**
     * Authenticates a user by validating the provided login credentials.
     * If successful, the user's authentication context is established.
     *
     * @param loginDTO an object of {@code UserLoginDTO} containing the username and password for login.
     * @param request the {@code HttpServletRequest} to manage session attributes for the authenticated user.
     * @return a {@code ResponseEntity} containing a {@code UserResponseDTO} with the authenticated user's details such as ID, username, and email.
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        UserResponseDTO responseDTO = authService.login(loginDTO, request, response);
        return ResponseEntity.ok(responseDTO);
    }


    /**
     * Logs out the currently authenticated user by invalidating their session
     * and clearing associated authentication details.
     * <p>
     * This endpoint performs the following actions:
     * <ul>
     *   <li>Invalidates the user's HTTP session if it exists.</li>
     *   <li>Clears the authentication context to remove authentication details.</li>
     *   <li>Removes any session-related cookies such as {@code JSESSIONID}.</li>
     *   <li>Ensures the user's session is securely terminated.</li>
     * </ul>
     *
     * @param request  the {@code HttpServletRequest} object, providing access to session data for the current user.
     * @param response the {@code HttpServletResponse} object, used to modify response headers or cookies as needed.
     */
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
    }

    /**
     * Verifies the user's email using the provided token and activates the account if valid.
     *
     * @param token the verification token sent to the user's email.
     * @return a {@code ResponseEntity} containing a map with a status and message
     * indicating the success or failure of the email confirmation.
     */
    @GetMapping("/confirm-email")
    public ResponseEntity<Map<String, String>> confirmEmail(@RequestParam("token") String token) {
        return authService.confirmEmail(token);
    }

    /**
     * Sends a password reset token to the specified email address
     * if the user exists and is active.
     *
     * @param email the email address of the user requesting a password reset.
     * @return a {@code ResponseEntity} containing a map with the status and message
     * indicating the outcome of the operation.
     */
    @GetMapping("/send-password-reset")
    public ResponseEntity<Map<String, String>> sendVerifyPasswordResetToken(@RequestParam("email") String email) {
        return authService.sendVerifyPasswordResetToken(email);
    }

    /**
     * Verifies the provided password reset token and returns a corresponding response.
     *
     * @param token the password reset token to validate.
     * @return a {@code ResponseEntity} containing a {@code Map<String, String>} with the status and message
     * indicating whether the token is valid or expired.
     */
    @GetMapping("/password-reset")
    public ResponseEntity<Map<String, String>> verifyPasswordResetToken(@RequestParam("token") String token) {
        return authService.verifyPasswordResetToken(token);
    }

    /**
     * Resets the user's password using the provided token and new password details.
     *
     * @param passwordResetDTO a {@code PasswordResetDTO} object containing the password reset token, new password,
     *                         and password confirmation.
     * @return a {@code ResponseEntity} containing a {@code Map<String, String>} with the status and message
     *         indicating the result of the password reset operation.
     */
    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
       return authService.changePassword(passwordResetDTO);
    }

}
