package me.abouabra.zovo.services;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * The {@code AuthService} class provides methods for managing user authentication, registration,
 * and session-related operations in the application.
 * <p>
 * This class handles core authentication workflows, including user login, registration, and logout.
 * It integrates with the Spring Security framework to ensure secure and reliable session handling.
 * Transactions are utilized where appropriate to maintain consistency across operations.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li>User authentication and session management.</li>
 *   <li>Registration of new users with validation and encryption.</li>
 *   <li>User logout and security context clearing.</li>
 * </ul>
 * <p>
 * The class uses several dependencies:
 * <ul>
 *   <li>{@code UserRepository} - To interact with the user persistence layer.</li>
 *   <li>{@code RoleRepository} - To access and configure roles for users.</li>
 *   <li>{@code UserMapper} - For mapping data transfer objects (DTOs) to entity objects and vice versa.</li>
 *   <li>{@code PasswordEncoder} - To securely encode and validate user passwords.</li>
 *   <li>{@code AuthenticationManager} - To manage the authentication process within Spring Security.</li>
 * </ul>
 * This class is annotated with {@code @Service} to indicate that it is a Spring service component
 * and {@code @AllArgsConstructor} for automated constructor injection.
 */
@Service
@AllArgsConstructor
@Getter
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authManager;
    private UserRepository userRepo;
    private RoleRepository roleRepo;
    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

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
     * which includes their username, email, and id.
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

        Role userRole = roleRepo.findByName("ROLE_USER").orElseThrow(() -> new RoleNotFoundException("Role with the specified name was not found"));

        user.addRole(userRole);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);
        return userMapper.toDTO(user);
    }


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

        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

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
     * Logs out the currently authenticated user by invalidating their session,
     * clearing the security context, and removing any session-related cookies.
     * <p>
     * This method performs the following actions:
     * <ul>
     *   <li>Invalidates the user's HTTP session if it exists.</li>
     *   <li>Clears the {@code SecurityContextHolder} to remove any authentication details.</li>
     *   <li>Removes the {@code JSESSIONID} cookie from the user's browser by setting its max age to zero.</li>
     *   <li>Logs the logout operation for auditing or debugging purposes.</li>
     * </ul>
     * <p>
     * This ensures secure and complete termination of the user's authenticated session.
     *
     * @param request  The {@code HttpServletRequest} object, used to access the user's current HTTP session.
     * @param response The {@code HttpServletResponse} object, used to send the cookie removal response to the client.
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();


        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        log.info("User has been logged out");
    }
}
