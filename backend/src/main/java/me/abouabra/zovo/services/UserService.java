package me.abouabra.zovo.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.dtos.UserDTO;
import me.abouabra.zovo.exceptions.UserNotFoundException;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import me.abouabra.zovo.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Data
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final CacheManager cacheManager;
    private final AvatarStorageService avatarStorageService;

    @Transactional
    @Cacheable(value = "usersList", key = "'default'")
    public List<UserDTO> getAllUsers() {
        return userRepo
                .findAll()
                .stream()
                .map(user -> userMapper.toDTO(user, avatarStorageService))
                .toList();
    }

    @Transactional
    @Cacheable(value = "usersList", key = "#userId")
    public UserDTO getUserById(int userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '%d' was not found".formatted(userId)));
        return userMapper.toDTO(user, avatarStorageService);
    }

    public ResponseEntity<? extends ApiResponse<?>> getLoggedInUserData(UserPrincipal loggedInUser) {
        return ApiResponse.success(userMapper.toDTO(loggedInUser.getUser(), avatarStorageService));
    }

    public ResponseEntity<? extends ApiResponse<?>> testAdminEligibility(UserPrincipal loggedInUser) {
        return ApiResponse.success(userMapper.toDTO(loggedInUser.getUser(), avatarStorageService));
    }
}
