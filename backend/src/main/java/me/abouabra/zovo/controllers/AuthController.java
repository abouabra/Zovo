package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.dtos.*;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.enums.RedisGroupAction;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.AuthService;
import me.abouabra.zovo.services.redis.RedisRateLimitingService;
import me.abouabra.zovo.services.oauth2.OAuth2Service;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * The {@code AuthController} handles authentication and authorization operations.
 * <p>
 * This includes user registration, login, logout, email confirmation, password resets,
 * Two-Factor Authentication (2FA), and OAuth2 social login flows. The controller
 * applies rate limiting to prevent abuse.
 * <p>
 * Endpoints are secured and return {@code ResponseEntity} objects containing
 * appropriate {@code ApiResponse} details in the response.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private AuthService authService;
    private RedisRateLimitingService redisRateLimitingService;
    private OAuth2Service oAuth2Service;

    @GetMapping("/is-authenticated")
    public ResponseEntity<? extends ApiResponse<?>> isAuthenticated(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        return loggedInUser == null
                ? ApiResponse.failure(ApiCode.UNAUTHORIZED, "User not authenticated")
                : ApiResponse.success("User is authenticated");
    }

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
     * Confirms the user's email address using the provided token.
     *
     * <p>This endpoint is protected against rate limiting and ensures the token
     * is valid and processed securely.</p>
     *
     * @param body A map containing the "token" parameter.
     * @param request The HTTP servlet request, used for accessing client details.
     * @return A {@link ResponseEntity} wrapping an {@link ApiResponse}, indicating success or failure.
     */
    @PostMapping("/confirm-email")
    public ResponseEntity<? extends ApiResponse<?>> confirmEmail(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String token = body.get("token");
        if (token == null || token.isEmpty())
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Missing token parameter");
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.confirmEmail(token)
        );
    }


    /**
     * Handles the request to send a password reset verification token to the user's email.
     *
     * @param body A map containing the "email" key with the user's email address as its value.
     * @param request The HTTP servlet request object, used for retrieving client details.
     * @return A {@link ResponseEntity} containing an {@link ApiResponse} indicating success or failure of the operation.
     */
    @PostMapping("/send-password-reset")
    public ResponseEntity<? extends ApiResponse<?>> sendVerifyPasswordResetToken(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String email = body.get("email");
        if (email == null || email.isEmpty())
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Missing email parameter");
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> authService.sendVerifyPasswordResetToken(email)
        );
    }

    /**
     * Verifies the validity of a password reset token.
     *
     * @param body A map containing the "token" key with the password reset token as its value.
     * @param request The HTTP servlet request object containing client request details.
     * @return A {@link ResponseEntity} containing the API response with the verification result.
     */
    @PostMapping("/verify-password-reset-token")
    public ResponseEntity<? extends ApiResponse<?>> verifyPasswordResetToken(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String token = body.get("token");
        if (token == null || token.isEmpty())
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Missing token parameter");
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


    @GetMapping("/2fa/status")
    public ResponseEntity<? extends ApiResponse<?>> getTwoFaStatus(@AuthenticationPrincipal UserPrincipal loggedInUser, HttpServletRequest request) {
        return authService.getTwoFaStatus(loggedInUser.getUser());
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

    @PostMapping("/2fa/verify")
    public ResponseEntity<? extends ApiResponse<?>> verify2FA(@AuthenticationPrincipal UserPrincipal loggedInUser, @Valid @RequestBody TwoFaCodeDTO twoFaCodeDTO) {
        return authService.verify2FA(loggedInUser.getUser(), twoFaCodeDTO);
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
    public ResponseEntity<? extends ApiResponse<?>> getOAuthProviders() {
        return authService.getOAuthProviders();
    }

    @GetMapping("/oauth2/authorize/{provider}")
    public void oauth2Authorize(@PathVariable("provider") String provider, HttpServletResponse response) throws IOException {
        String authorizationUrl = oAuth2Service.getAuthorizationUrl(provider);
        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/oauth2/callback/{provider}")
    public ResponseEntity<?> oauth2Callback(@PathVariable("provider") String provider, @RequestParam Map<String, String> params, HttpServletRequest request, HttpServletResponse response) {
        return redisRateLimitingService.wrap(
                RedisGroupAction.AUTH,
                request.getRemoteAddr(),
                () -> {
                    String code = params.get("code");
                    if (code == null || code.isEmpty())
                        return ApiResponse.failure(ApiCode.BAD_REQUEST, "Missing code parameter");
                    return oAuth2Service.handleCallback(provider, code, request, response);
                }
        );
    }


}

