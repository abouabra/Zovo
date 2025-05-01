package me.abouabra.zovo.controllers;

import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.UserService;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<? extends ApiResponse<?>> getAllUsers() {
        List< UserResponseDTO > dto = userService.getAllUsers();
        return ApiResponse.success(dto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<? extends ApiResponse<?>> getUserById(@PathVariable int userId) {
        UserResponseDTO responseDTO = userService.getUserById(userId);
        return ApiResponse.success(responseDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<? extends ApiResponse<?>> getLoggedInUserData(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        return userService.getLoggedInUserData(loggedInUser);
    }

}
