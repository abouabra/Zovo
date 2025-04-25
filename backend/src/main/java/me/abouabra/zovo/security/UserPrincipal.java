package me.abouabra.zovo.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.abouabra.zovo.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

/**
 * <p>The <code>UserPrincipal</code> class is an implementation of the <code>UserDetails</code> interface,
 * designed to integrate the application's <code>User</code> entity with Spring Security.</p>
 *
 * <p>This class acts as a bridge between the core application user data and the API required by Spring Security
 * for authentication and authorization. It wraps a <code>User</code> instance and delegates its user-related
 * information and functionalities to the <code>UserDetails</code> methods.</p>
 *
 * <ul>
 *   <li><b>User Association:</b> The <code>UserPrincipal</code> class holds a reference to a single <code>User</code>
 *   object, encapsulating its details.</li>
 *   <li><b>Role-Based Authorities:</b> The <code>getAuthorities</code> method fetches roles from the wrapped
 *   <code>User</code> object, interpreting them as granted authorities for Spring Security.</li>
 *   <li><b>ID Access:</b> Provides a method <code>getId</code> to access the ID of the encapsulated <code>User</code>.</li>
 *   <li><b>Custom String Representation:</b> Overrides the <code>toString</code> method to return
 *   the string representation of the encapsulated <code>User</code> instance.</li>
 * </ul>
 *
 * <p><b>Spring Security Integration:</b> The methods provided by the <code>UserDetails</code> interface
 * are mapped to the corresponding fields or logic in the <code>User</code> entity:</p>
 * <ul>
 *   <li><code>getPassword</code>: Retrieves the user's password.</li>
 *   <li><code>getUsername</code>: Retrieves the username of the user.</li>
 *   <li><code>getAuthorities</code>: Retrieves the roles of the user as granted authorities.</li>
 *   <li>Account status checks like <code>isAccountNonExpired</code>, <code>isAccountNonLocked</code>,
 *   <code>isCredentialsNonExpired</code>, and <code>isEnabled</code> use defaults or delegate
 *   directly to the superclass implementation.</li>
 * </ul>
 *
 * <p>This class leverages Lombok annotations for reducing boilerplate code:</p>
 * <ul>
 *   <li><code>@AllArgsConstructor</code>: Generates a constructor with all properties.</li>
 *   <li><code>@Getter</code>: Generates getter methods for the fields.</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
public class UserPrincipal implements UserDetails {

    private final User user;

    public Long getId() {
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return user.toString();
    }

}
