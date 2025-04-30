package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.*;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.AuthService;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;

    
    /**
     * Handles user registration by processing the provided registration details.
     *
     * @param registerDTO the user registration details, including username, email,
     *                    password, and password confirmation, must be valid.
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} object with
     *         success status and relevant data or error message.
     */
    @PostMapping("/register")
    public ResponseEntity<? extends ApiResponse<?>> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return authService.register(registerDTO);
    }

    
    /**
     * Authenticates a user using the provided login credentials.
     *
     * @param loginDTO the user's login details including email and password.
     * @param request the HTTP request object.
     * @param response the HTTP response object.
     * @return a response entity containing an {@code ApiResponse} indicating the success or failure of the login operation.
     */
    @PostMapping("/login")
    public ResponseEntity<? extends ApiResponse<?>> login(@Valid @RequestBody UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        return authService.login(loginDTO, request, response);
    }

    /**
     * Processes two-factor authentication (2FA) login request.
     *
     * @param tokenAndCodeDTO the DTO containing the temporary token and 2FA code.
     * @param request the HTTP request object.
     * @param response the HTTP response object.
     * @return a {@code ResponseEntity} containing an {@code ApiResponse} with the login result.
     */
    @PostMapping("/login-2fa")
    public ResponseEntity<? extends ApiResponse<?>> loginWith2FA(@RequestBody TwoFaTokenAndCodeDTO tokenAndCodeDTO, HttpServletRequest request, HttpServletResponse response) {
        return authService.loginWith2FA(tokenAndCodeDTO, request, response);
    }

    
    /**
     * Logs out the currently authenticated user by invalidating their session
     * and clearing the security context.
     *
     * @param request  the HTTP request containing user-related session information
     * @param response the HTTP response to send back the logout status or handle cookies
     */
    @PostMapping("/logout")
    public ResponseEntity<? extends ApiResponse<?>> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }

    
    /**
     * Confirms a user's email using a provided verification token.
     *
     * @param token the email verification token
     * @return a ResponseEntity containing an ApiResponse with the email confirmation result
     */
    @GetMapping("/confirm-email")
    public ResponseEntity<? extends ApiResponse<?>> confirmEmail(@RequestParam("token") String token) {
        return authService.confirmEmail(token);
    }

    
    /**
     * Sends a password-reset verification token to the specified email address.
     * <p>
     * This operation is performed only if the email belongs to an active user.
     *
     * @param email The email address of the user requesting a password reset.
     * @return A ResponseEntity containing an ApiResponse indicating the success or failure of the operation.
     */
    @GetMapping("/send-password-reset")
    public ResponseEntity<? extends ApiResponse<?>> sendVerifyPasswordResetToken(@RequestParam("email") String email) {
        return authService.sendVerifyPasswordResetToken(email);
    }

    
    /**
     * Verifies the validity of a password reset token.
     *
     * @param token the password reset token to be validated.
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with
     *         the validation result, indicating success or failure.
     */
    @GetMapping("/password-reset")
    public ResponseEntity<? extends ApiResponse<?>> verifyPasswordResetToken(@RequestParam("token") String token) {
        return authService.verifyPasswordResetToken(token);
    }

    
    /**
     * Processes a password reset request by validating the provided token and updating the password.
     *
     * @param passwordResetDTO the data transfer object containing the reset token, new password,
     *                         and password confirmation.
     * @return a ResponseEntity containing an ApiResponse to indicate the success or failure of the operation.
     */
    @PostMapping("/password-reset")
    public ResponseEntity<? extends ApiResponse<?>> changePassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
       return authService.changePassword(passwordResetDTO);
    }

    
    /**
     * Generates a Two-Factor Authentication (2FA) QR code for the authenticated user.
     *
     * @param loggedInUser the authenticated user requesting 2FA generation.
     * @return a {@link ResponseEntity} containing the {@link ApiResponse} with 2FA QR code details or error message.
     */
    @GetMapping("/2fa/generate")
    public ResponseEntity<? extends ApiResponse<?>> generate2FA(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        return authService.generate2FA(loggedInUser);
    }

    
    /**
     * Enables Two-Factor Authentication (2FA) for the logged-in user.
     *
     * @param loggedInUser The current authenticated user.
     * @param twoFaCodeDTO The DTO containing the 2FA code to verify.
     * @return A {@code ResponseEntity} wrapping {@code ApiResponse} indicating
     *         the success or failure of enabling 2FA.
     */
    @PostMapping("/2fa/enable")
    public ResponseEntity<? extends ApiResponse<?>> enable2FA(@AuthenticationPrincipal UserPrincipal loggedInUser, @Valid @RequestBody TwoFaCodeDTO twoFaCodeDTO) {
        return authService.enable2FA(loggedInUser, twoFaCodeDTO);
    }


    
    /**
     * Disables Two-Factor Authentication (2FA) for the currently authenticated user.
     *
     * @param loggedInUser the details of the authenticated user.
     * @return a ResponseEntity with an ApiResponse indicating the result of the operation.
     */
    @DeleteMapping("/2fa/disable")
    public ResponseEntity<? extends ApiResponse<?>> disable2FA(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        return authService.disable2FA(loggedInUser);
    }
}
