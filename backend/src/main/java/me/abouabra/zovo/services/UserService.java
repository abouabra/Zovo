package me.abouabra.zovo.services;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserRequestDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.exceptions.UserAlreadyExistsException;
import me.abouabra.zovo.exceptions.UserNotFoundException;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Data
public class UserService {
    private UserRepository userRepo;
    private UserMapper userMapper;

    public List<UserResponseDTO> getAllUsers() {
        return userRepo
                .findAll()
                .stream()
                .map(
                    user -> userMapper.toDTO(user)
                ).toList();
    }

    public UserResponseDTO register(@Valid UserRegisterDTO requestDTO) {
        if (userRepo.findUserByUsername(requestDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '%s' is already taken".formatted(requestDTO.getUsername()));
        }
        if (userRepo.findUserByEmail(requestDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email '%s' is already taken".formatted(requestDTO.getEmail()));
        }
        User user = userMapper.toUser(requestDTO);
        userRepo.save(user);
        return userMapper.toDTO(user);
    }

    public UserResponseDTO getUserById(int userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '%d' was not found".formatted(userId)));

        return userMapper.toDTO(user);
    }
}
