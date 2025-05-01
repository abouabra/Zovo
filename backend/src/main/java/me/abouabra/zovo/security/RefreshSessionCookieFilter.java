package me.abouabra.zovo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.configs.SessionProperties;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@AllArgsConstructor
public class RefreshSessionCookieFilter extends OncePerRequestFilter {
    private final SessionProperties sessionProperties;

    /**
     * <p>Processes the HTTP request and updates the session cookie if a user is authenticated.</p>
     * <p>If an authenticated session exists, checks for the matching session cookie, updates it,
     * and adds it back to the HTTP response.</p>
     *
     * @param request     the HTTP request to process
     * @param response    the HTTP response to update
     * @param filterChain the filter chain to pass the request and response to the next filter
     * @throws ServletException in case of servlet errors
     * @throws IOException      in case of input/output errors
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false);

        if (session != null && auth != null && auth.isAuthenticated()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (c.getName().equals(sessionProperties.getName())) {
                        Cookie refreshed = sessionProperties.updateSessionCookie(c.getName(), c.getValue());
                        response.addCookie(refreshed);
                        break;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

}