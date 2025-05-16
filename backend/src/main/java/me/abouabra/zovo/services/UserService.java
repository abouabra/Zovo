package me.abouabra.zovo.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.abouabra.zovo.dtos.UpdateProfileDTO;
import me.abouabra.zovo.dtos.UserDTO;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.exceptions.UserNotFoundException;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import me.abouabra.zovo.utils.ApiResponse;
import me.abouabra.zovo.utils.ContentTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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

    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> updateUserAvatar(MultipartFile file, User user) {
        try {
            avatarStorageService.deleteAvatar(user.getAvatarKey());
            InputStream avatarStream = file.getInputStream();
            String completeKey = user.getId() + ContentTypeUtils.convertContentTypeToExtension(file.getContentType());
            avatarStorageService.uploadAvatar(completeKey, avatarStream, avatarStream.available(), file.getContentType());
            user.setAvatarKey(completeKey);
            userRepo.save(user);
            updateUserSession(user);
            return ApiResponse.success("Avatar updated");
        } catch (Exception e) {
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Failed to read avatar file");
        }
    }

    public ResponseEntity<? extends ApiResponse<?>> updateProfile(UpdateProfileDTO updateProfileDTO, User user) {
        boolean usernameChanged = !Objects.equals(user.getUsername(), updateProfileDTO.getUsername());
        boolean emailChanged = !Objects.equals(user.getEmail(), updateProfileDTO.getEmail());

        if (usernameChanged && userRepo.findUserByUsername(updateProfileDTO.getUsername()).isPresent()) {
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Username '%s' is already taken".formatted(updateProfileDTO.getUsername()));
        }

        if (emailChanged && userRepo.findUserByEmail(updateProfileDTO.getEmail()).isPresent()) {
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Email '%s' is already taken".formatted(updateProfileDTO.getEmail()));
        }

        if ((updateProfileDTO.getPassword().isBlank() && !updateProfileDTO.getPasswordConfirmation().isBlank()) ||
                (!updateProfileDTO.getPassword().isBlank() && updateProfileDTO.getPasswordConfirmation().isBlank())) {
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Passwords must be provided together");
        }


        if (!updateProfileDTO.getPassword().isBlank()) {
            if (!updateProfileDTO.getPassword().equals(updateProfileDTO.getPasswordConfirmation())) {
                return ApiResponse.failure(ApiCode.BAD_REQUEST, "Passwords do not match");
            }
            Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
            if (!pattern.matcher(updateProfileDTO.getPassword()).matches()) {
                return ApiResponse.failure(ApiCode.BAD_REQUEST, "Password must be at least 8 characters long and contain at least one uppercase, lowercase letter, number and special character");
            }
            user.setPassword(passwordEncoder.encode(updateProfileDTO.getPassword()));
        }

        user.setUsername(updateProfileDTO.getUsername());
        user.setEmail(updateProfileDTO.getEmail());
        userRepo.save(user);

        updateUserSession(user);

        return ApiResponse.success("Profile updated");
    }

    public void updateUserSession(User user) {
        UserPrincipal updatedPrincipal = new UserPrincipal(user);
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        if (currentAuth == null) {
            return;
        }

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedPrincipal,
                currentAuth.getCredentials(),
                updatedPrincipal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        RequestContextHolder.currentRequestAttributes().setAttribute(
                "SPRING_SECURITY_CONTEXT",
                SecurityContextHolder.getContext(),
                RequestAttributes.SCOPE_SESSION
        );
    }

}
