package me.abouabra.zovo.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.exceptions.UserNotFoundException;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    public List<UserResponseDTO> getAllUsers() {
        return userRepo
                .findAll()
                .stream()
                .map(
                        userMapper::toDTO
                ).toList();
    }

    public UserResponseDTO getUserById(int userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '%d' was not found".formatted(userId)));

        return userMapper.toDTO(user);
    }

    public UserResponseDTO getLoggedInUserData(UserPrincipal loggedInUser) {
        return userMapper.toDTO(loggedInUser.getUser());
    }

    public UserResponseDTO testAdminEligibility(UserPrincipal loggedInUser) {
        return userMapper.toDTO(loggedInUser.getUser());
    }
}
