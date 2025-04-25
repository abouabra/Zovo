package me.abouabra.zovo.controllers;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserRequestDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.services.UserService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegisterDTO requestDTO) {
        UserResponseDTO responseDTO = userService.register(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable int userId) {
        UserResponseDTO responseDTO = userService.getUserById(userId);
        return ResponseEntity.ok(responseDTO);
    }
}
