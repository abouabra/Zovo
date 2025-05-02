package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.*;
import me.abouabra.zovo.enums.RedisGroupAction;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.AuthService;
import me.abouabra.zovo.services.OAuth2Service;
import me.abouabra.zovo.services.RedisRateLimitingService;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;
    private RedisRateLimitingService redisRateLimitingService;
    private OAuth2Service oAuth2Service;

    /**
     * Handles user registration by processing the provided registration details.
     *
     * @param registerDTO the user registration details, including username, email,
     *                    password, and password confirmation, must be valid.
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} object with
     * success status and relevant data or error message.
     */
    @PostMapping("/register")
    public ResponseEntity<? extends ApiResponse<?>> register(@Valid @RequestBody UserRegisterDTO registerDTO, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.register(registerDTO)
        );
    }


    /**
     * Handles user login by validating credentials and managing rate limits.
     *
     * @param loginDTO the login details, including email and password, must be valid.
     * @param request  the HTTP request object.
     * @param response the HTTP response object.
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} indicating
     * the success or failure of the login operation.
     */
    @PostMapping("/login")
    public ResponseEntity<? extends ApiResponse<?>> login(@Valid @RequestBody UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.login(loginDTO, request, response)
        );
    }


    /**
     * Handles the login process using Two-Factor Authentication (2FA).
     *
     * @param tokenAndCodeDTO the DTO containing the 2FA token and verification code.
     * @param request         the HTTP request object.
     * @param response        the HTTP response object.
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the success
     * or failure result of the 2FA login process.
     */
    @PostMapping("/login-2fa")
    public ResponseEntity<? extends ApiResponse<?>> loginWith2FA(@RequestBody TwoFaTokenAndCodeDTO tokenAndCodeDTO, HttpServletRequest request, HttpServletResponse response) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.loginWith2FA(tokenAndCodeDTO, request, response)
        );
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
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.logout(request, response)
        );
    }


    /**
     * Confirms a user's email using a provided verification token.
     *
     * @param token the email verification token
     * @return a ResponseEntity containing an ApiResponse with the email confirmation result
     */
    @GetMapping("/confirm-email")
    public ResponseEntity<? extends ApiResponse<?>> confirmEmail(@RequestParam("token") String token, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.confirmEmail(token)
        );
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
    public ResponseEntity<? extends ApiResponse<?>> sendVerifyPasswordResetToken(@RequestParam("email") String email, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.sendVerifyPasswordResetToken(email)
        );
    }


    /**
     * Verifies the validity of a password reset token.
     *
     * @param token the password reset token to be validated.
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with
     * the validation result, indicating success or failure.
     */
    @GetMapping("/password-reset")
    public ResponseEntity<? extends ApiResponse<?>> verifyPasswordResetToken(@RequestParam("token") String token, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.verifyPasswordResetToken(token)
        );
    }


    /**
     * Processes a password reset request by validating the provided token and updating the password.
     *
     * @param passwordResetDTO the data transfer object containing the reset token, new password,
     *                         and password confirmation.
     * @return a ResponseEntity containing an ApiResponse to indicate the success or failure of the operation.
     */
    @PostMapping("/password-reset")
    public ResponseEntity<? extends ApiResponse<?>> changePassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.changePassword(passwordResetDTO)
        );
    }


    /**
     * Generates a Two-Factor Authentication (2FA) QR code for the authenticated user.
     *
     * @param loggedInUser the authenticated user requesting 2FA generation.
     * @return a {@link ResponseEntity} containing the {@link ApiResponse} with 2FA QR code details or error message.
     */
    @GetMapping("/2fa/generate")
    public ResponseEntity<? extends ApiResponse<?>> generate2FA(@AuthenticationPrincipal UserPrincipal loggedInUser, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.generate2FA(loggedInUser)
        );
    }


    /**
     * Enables Two-Factor Authentication (2FA) for the logged-in user.
     *
     * @param loggedInUser The current authenticated user.
     * @param twoFaCodeDTO The DTO containing the 2FA code to verify.
     * @return A {@code ResponseEntity} wrapping {@code ApiResponse} indicating
     * the success or failure of enabling 2FA.
     */
    @PostMapping("/2fa/enable")
    public ResponseEntity<? extends ApiResponse<?>> enable2FA(@AuthenticationPrincipal UserPrincipal loggedInUser, @Valid @RequestBody TwoFaCodeDTO twoFaCodeDTO, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.enable2FA(loggedInUser, twoFaCodeDTO)
        );
    }


    /**
     * Disables Two-Factor Authentication (2FA) for the currently authenticated user.
     *
     * @param loggedInUser the details of the authenticated user.
     * @return a ResponseEntity with an ApiResponse indicating the result of the operation.
     */
    @DeleteMapping("/2fa/disable")
    public ResponseEntity<? extends ApiResponse<?>> disable2FA(@AuthenticationPrincipal UserPrincipal loggedInUser, HttpServletRequest request) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.disable2FA(loggedInUser)
        );
    }

    @GetMapping("/oauth2/providers")
    public ResponseEntity<?> getOAuthProviders() {
        // Return available OAuth providers
        Map<String, String> providers = new HashMap<>();
        providers.put("google", "/api/v1/auth/oauth2/authorize/google");
        providers.put("github", "/api/v1/auth/oauth2/authorize/github");

        return ResponseEntity.ok(providers);
    }

    @GetMapping("/oauth2/authorize/{provider}")
    public void oauth2Authorize(
            @PathVariable("provider") String provider,
            HttpServletResponse response
    ) throws IOException {
        String authorizationUrl = oAuth2Service.getAuthorizationUrl(provider); // Compose from config
        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/oauth2/callback/{provider}")
    public ResponseEntity<?> oauth2Callback(
            @PathVariable("provider") String provider,
            @RequestParam Map<String, String> params,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing code parameter");
        }
        return oAuth2Service.handleCallback(provider, code);
    }
}
