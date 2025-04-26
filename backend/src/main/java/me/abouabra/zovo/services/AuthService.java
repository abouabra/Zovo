package me.abouabra.zovo.services;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.abouabra.zovo.dtos.UserLoginDTO;
import me.abouabra.zovo.dtos.UserRegisterDTO;
import me.abouabra.zovo.dtos.UserResponseDTO;
import me.abouabra.zovo.exceptions.RoleNotFoundException;
import me.abouabra.zovo.exceptions.UserAlreadyExistsException;
import me.abouabra.zovo.mappers.UserMapper;
import me.abouabra.zovo.models.Role;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.RoleRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.AuthenticationException;

/**
 * The {@code AuthService} class provides authentication and registration services
 * for managing users and their roles within an application.
 *
 * <p>This service acts as a central point for handling authentication-related
 * functionalities such as logging in users and registering new accounts. It
 * interacts with repositories to manage user and role data and uses mappings
 * to bridge between DTOs and entity objects. Additionally, password encryption
 * and session management are also implemented as part of this service.</p>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *     <li>User Authentication: Authenticates existing users based on their credentials.</li>
 *     <li>User Registration: Registers new users and automatically assigns them the default user role.</li>
 *     <li>Session Management: Manages user sessions and their associated security contexts.</li>
 *     <li>Password Encoding: Securely encrypts user credentials before saving them to the database.</li>
 * </ul>
 *
 * <p>The class is marked with {@code @Service} to indicate that it is a Spring
 * service component and should be discovered during component scanning. It uses
 * constructor injection via {@code @AllArgsConstructor} to provide dependencies.</p>
 *
 * <p><strong>Dependencies:</strong></p>
 * <ul>
 *     <li>{@code UserRepository}: Handles database operations for {@code User} entities.</li>
 *     <li>{@code RoleRepository}: Manages role data retrieval and storage.</li>
 *     <li>{@code UserMapper}: Maps between user entity objects and DTOs.</li>
 *     <li>{@code PasswordEncoder}: Encrypts user passwords securely.</li>
 *     <li>{@code AuthenticationManager}: Provides functionality to authenticate users.</li>
 * </ul>
 *
 * <p><strong>Thread-Safety:</strong> This class is thread-safe as it maintains
 * no mutable state outside of local method variables and relies on Spring's
 * transactional and dependency management features.</p>
 */
@Service
@AllArgsConstructor
@Getter
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private UserRepository userRepo;
    private RoleRepository roleRepo;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    /**
     * Authenticates a user based on their login credentials and establishes an authenticated session.
     * <p>
     * The method performs the following operations:
     * <ul>
     *   <li>Maps the {@code UserLoginDTO} to a {@code User} entity.</li>
     *   <li>Authenticates the user with {@link AuthenticationManager}.</li>
     *   <li>Creates a {@code SecurityContext} and sets the authentication details.</li>
     *   <li>Associates the security context with the user's HTTP session.</li>
     *   <li>Returns a {@code UserResponseDTO} containing authenticated user details.</li>
     * </ul>
     * <p>
     * This method is transactional, ensuring that all operations are consistently applied or rolled back in case of an error.
     *
     * @param loginDTO The {@code UserLoginDTO} containing the user's email and password. Must be valid.
     * @param request  The {@code HttpServletRequest} for accessing and modifying the user's session details.
     * @return A {@code UserResponseDTO} representing the authenticated user, including their ID, username, and email.
     * @throws AuthenticationException If authentication fails (e.g., invalid credentials).
     */
    @Transactional
    public UserResponseDTO login(UserLoginDTO loginDTO, HttpServletRequest request) {
        User user = userMapper.toUser(loginDTO);

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User authenticatedUser = userPrincipal.getUser();
    
        return userMapper.toDTO(authenticatedUser);
    }
    
    
    /**
     * Registers a new user in the system by validating and processing the provided registration details.
     * <p>
     * The method performs the following operations:
     * <ul>
     *   <li>Validates that the username and email are unique; if not, throws {@code UserAlreadyExistsException}.</li>
     *   <li>Maps the {@code UserRegisterDTO} to a {@code User} entity.</li>
     *   <li>Assigns the default {@code ROLE_USER} to the new user.</li>
     *   <li>Encrypts the user's password before persisting it.</li>
     *   <li>Saves the user to the repository and returns their data encapsulated in a {@code UserResponseDTO}.</li>
     * </ul>
     * <p>
     * This method is transactional, ensuring that all operations succeed or none are applied.
     *
     * @param requestDTO A {@code UserRegisterDTO} object containing the registration details,
     *                   including username, email, password, and password confirmation. Must be valid.
     * @return A {@code UserResponseDTO} object representing the newly registered user,
     *         which includes their username, email, and id.
     * @throws UserAlreadyExistsException If a user with the same username or email already exists.
     * @throws RoleNotFoundException      If the default user role {@code ROLE_USER} is not found.
     */
    @Transactional
    public UserResponseDTO register(@Valid UserRegisterDTO requestDTO) {
        if (userRepo.findUserByUsername(requestDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username '%s' is already taken".formatted(requestDTO.getUsername()));
        }
        if (userRepo.findUserByEmail(requestDTO.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email '%s' is already taken".formatted(requestDTO.getEmail()));
        }

        User user = userMapper.toUser(requestDTO);

        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Role with the specified name was not found"));

        user.addRole(userRole);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);
        return userMapper.toDTO(user);
    }
}
