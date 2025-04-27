package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.UserLoginDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * The {@code AuthController} class provides REST API endpoints for handling user authentication and
 * account registration operations. It acts as the controller layer in the authentication flow, delegating
 * actions to the {@code AuthService}.
 *
 * <p>This controller provides the following endpoints:</p>
 * <ul>
 *     <li><b>POST /auth/register:</b> Handles user account registration, validating input details
 *     and returning a response with the created user's details.</li>
 *     <li><b>POST /auth/login:</b> Authenticates a user and establishes the user's session if the
 *     credentials are valid.</li>
 *     <li><b>POST /auth/logout:</b> Logs out the current user by invalidating the session
 *     and clearing the security context.</li>
 * </ul>
 *
 * <p>All requests and responses are managed using {@code ResponseEntity} to standardize the format and
 * status of API responses.</p>
 *
 * <p>Validation of request payloads is handled using the {@code @Valid} annotation, ensuring that the
 * necessary constraints are checked before processing the request.</p>
 *
 * <p>This controller relies on Spring's {@code @RestController} and {@code @RequestMapping} annotations
 * to handle HTTP requests and responses, aligning with RESTful architecture principles.</p>
 */
@RestController
@RequestMapping("/auth")
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
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        UserResponseDTO responseDTO = authService.register(registerDTO);
        return ResponseEntity.ok(responseDTO);
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
}
