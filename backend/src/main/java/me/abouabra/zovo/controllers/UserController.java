package me.abouabra.zovo.controllers;

import lombok.AllArgsConstructor;
import me.abouabra.zovo.dtos.UpdateProfileDTO;
import me.abouabra.zovo.dtos.UserDTO;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.UserService;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<? extends ApiResponse<?>> getAllUsers() {
        List<UserDTO> usersListDTO = userService.getAllUsers();
        return ApiResponse.success(usersListDTO);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<? extends ApiResponse<?>> getUserById(@PathVariable int userId) {
        UserDTO userDTO = userService.getUserById(userId);
        return ApiResponse.success(userDTO);
    }

    @GetMapping("/me")
    public ResponseEntity<? extends ApiResponse<?>> getLoggedInUserData(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        return userService.getLoggedInUserData(loggedInUser);
    }

    @PostMapping(path="/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<? extends ApiResponse<?>> updateUserAvatar(@RequestParam("avatar") MultipartFile file, @AuthenticationPrincipal UserPrincipal loggedInUser) {
        return userService.updateUserAvatar(file, loggedInUser.getUser());
    }

    @PutMapping("/update")
    public ResponseEntity<? extends ApiResponse<?>> updateProfile(@RequestBody UpdateProfileDTO updateProfileDTO , @AuthenticationPrincipal UserPrincipal loggedInUser) {
        return userService.updateProfile(updateProfileDTO, loggedInUser.getUser());
    }
}
