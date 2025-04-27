package me.abouabra.zovo.services;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.abouabra.zovo.config.SessionProperties;
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

import java.util.Set;


/**
 * AuthService provides authentication and user management functionalities.
 * <p>
 * Key operations include:
 * <ul>
 *   <li>User registration with role assignment and password encryption</li>
 *   <li>User authentication with session and cookie management</li>
 *   <li>User logout by clearing session and security context</li>
 * </ul>
 */
@Service
@AllArgsConstructor
@Getter
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authManager;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SessionProperties sessionProperties;


    /**
     * Registers a new user by validating input, assigning roles, encoding the password,
     * and saving the user to the repository.
     *
     * @param requestDTO the data transfer object containing user registration details (username, email, password).
     * @return a UserResponseDTO containing the details of the newly registered user.
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

        user.setRoles(Set.of(userRole));

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepo.save(user);
        return userMapper.toDTO(user);
    }


    /**
     * Authenticates a user, establishes a security context, and manages session creation and cookies.
     *
     * @param loginDTO the data transfer object containing user login credentials (email and password).
     * @param request  the HTTP request used to manage session data.
     * @param response the HTTP response used to add session cookies.
     * @return a UserResponseDTO containing the authenticated user's information.
     */
    @Transactional
    public UserResponseDTO login(UserLoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        User user = userMapper.toUser(loginDTO);

        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        Cookie cookie = sessionProperties.createSessionCookie(newSession);
        response.addCookie(cookie);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User authenticatedUser = userPrincipal.getUser();
        return userMapper.toDTO(authenticatedUser);
    }


    /**
     * Logs out the authenticated user by invalidating their session, clearing the security context,
     * and adding an expired session cookie to the response.
     *
     * @param request  the HTTP request containing the user's session data.
     * @param response the HTTP response used to add an expired session cookie.
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Cookie cookie = sessionProperties.createExpiredSessionCookie(session.getId());
            response.addCookie(cookie);
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        log.info("User has been logged out");
    }
}
