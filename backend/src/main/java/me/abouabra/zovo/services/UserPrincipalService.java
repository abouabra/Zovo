package me.abouabra.zovo.services;

import lombok.AllArgsConstructor;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.security.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * <p>The <code>UserPrincipalService</code> class is a service implementation for managing user authentication
 * and loading user details required by Spring Security.</p>
 *
 * <p>This class implements the <code>UserDetailsService</code> interface, allowing customization of how user
 * details are retrieved from the underlying data source. It integrates with the <code>UserRepository</code>
 * to fetch user data stored in the database and transforms that data into Spring Security's
 * <code>UserDetails</code> object.</p>
 *
 * <p>Key functionalities:</p>
 * <ul>
 *   <li>Fetches a <code>User</code> entity using the email address provided during authentication.</li>
 *   <li>Wraps the retrieved <code>User</code> entity in a <code>UserPrincipal</code> object to adapt it
 *   for Spring Security's requirements.</li>
 *   <li>Throws a <code>UsernameNotFoundException</code> if no user with the provided email address is found,
 *   ensuring consistent error handling for invalid login attempts.</li>
 * </ul>
 *
 * <p>This service supports Spring's dependency injection mechanism by marking itself as a <code>@Service</code>.
 * The <code>AllArgsConstructor</code> annotation is used to automatically inject dependencies, such as
 * the <code>UserRepository</code>.</p>
 */
@Service
@AllArgsConstructor
public class UserPrincipalService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads a user by their username (in this specific implementation, the email address).
     *
     * <p>This method fetches a <code>User</code> entity from the underlying data store using the provided email address
     * and wraps it in a <code>UserPrincipal</code> object for Spring Security integration.</p>
     *
     * <p>If no user is found with the given email, a <code>UsernameNotFoundException</code> is thrown.</p>
     *
     * @param email The email of the user to be loaded. Must not be <code>null</code>.
     * @return A <code>UserDetails</code> object that contains user-specific data such as username, password, and authorities.
     * @throws UsernameNotFoundException If no user is found with the specified email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new UserPrincipal(user);
    }
}
