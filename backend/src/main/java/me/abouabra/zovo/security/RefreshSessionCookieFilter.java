package me.abouabra.zovo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import me.abouabra.zovo.configs.SessionProperties;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * <p>The <code>RefreshSessionCookieFilter</code> class is a custom implementation of the <code>OncePerRequestFilter</code>
 * that refreshes the HTTP session cookie for authenticated requests.</p>
 *
 * <p>It checks if an HTTP session exists, the user is authenticated, and not anonymous. If conditions are met, it uses
 * <code>SessionProperties</code> to create and add a session cookie to the response.</p>
 *
 * <p>This filter ensures session cookies are refreshed to maintain active user sessions securely.</p>
 */
@AllArgsConstructor
public class RefreshSessionCookieFilter extends OncePerRequestFilter {
    private final SessionProperties sessionProperties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false);
        if (session != null && authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            Cookie cookie = sessionProperties.createSessionCookie(session);
            response.addCookie(cookie);
        }

        filterChain.doFilter(request, response);
    }
}
