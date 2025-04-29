package me.abouabra.zovo.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.abouabra.zovo.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;


/**
 * <p>The <code>UserPrincipal</code> class is an implementation of <code>UserDetails</code>
 * used for Spring Security integration. It wraps the <code>User</code> entity
 * and provides credential and authority information for authentication and authorization.</p>
 *
 * <ul>
 *   <li>Delegates access to user properties, roles, and status.</li>
 *   <li>Ensures account and credential status validity.</li>
 *   <li>Primarily used in security contexts.</li>
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
        return user.isEnabled() && isActive();
    }

    public boolean isActive() {
        return user.isActive();
    }

    @Override
    public String toString() {
        return user.toString();
    }
}
