package me.abouabra.zovo.controllers;

import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> userDTOList = userService.getAllUsers();
        return ResponseEntity.ok(userDTOList);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable int userId) {
        UserResponseDTO responseDTO = userService.getUserById(userId);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponseDTO> getLoggedInUserData(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        UserResponseDTO userDTO = userService.getLoggedInUserData(loggedInUser);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> testAdminEligibility(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        UserResponseDTO dto = userService.testAdminEligibility(loggedInUser);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
