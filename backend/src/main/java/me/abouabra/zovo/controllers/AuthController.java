package me.abouabra.zovo.controllers;

import jakarta.servlet.http.HttpServletRequest;
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
 * The {@code AuthController} class provides REST API endpoints for
 * user registration and authentication processes.
 * <p>
 * This controller handles interactions related to user authentication,
 * including registering new users and logging in existing users. It communicates
 * with the {@code AuthService} to perform these operations.
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
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody UserLoginDTO loginDTO, HttpServletRequest request) {
        UserResponseDTO responseDTO = authService.login(loginDTO, request);
        return ResponseEntity.ok(responseDTO);
    }
}
