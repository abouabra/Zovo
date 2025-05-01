package me.abouabra.zovo.configs;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.boot.web.server.Cookie.SameSite;

/**
 * The <code>SessionProperties</code> class encapsulates configuration properties for HTTP session cookies.
 * <p>
 * It handles cookie attributes such as name, max age, path, domain, HTTP-only, secure flags,
 * and SameSite policies. It also provides utility methods for creating session cookies and
 * managing cookie expiration.
 * <p>
 * Marked with @Component and @ConfigurationProperties, it integrates seamlessly with Spring's
 * property binding mechanism.
 */
@Component
@Data
@ConfigurationProperties(prefix = "server.servlet.session.cookie")
public class SessionProperties {
    private static final int DEFAULT_MAX_AGE = 7 * 24 * 60 * 60; // 7 days
    private static final int COOKIE_DELETED = 0;

    private String name = "JSESSIONID";
    private int maxAge = DEFAULT_MAX_AGE;
    private String path = "/";
    private boolean httpOnly = true;
    private boolean secure = false; // TODO: enable when HTTPS is enabled
    private String domain;
    private SameSite sameSite = SameSite.STRICT;


    public Cookie createSessionCookie(HttpSession session) {
        return buildBaseCookie(session.getId())
                .maxAge(session.getMaxInactiveInterval())
                .build();
    }

    public Cookie updateSessionCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
    
    /**
     * Creates a cookie that will immediately expire, effectively deleting the session
     *
     * @param sessionId the session identifier
     * @return cookie configured for deletion
     */
    public Cookie createExpiredSessionCookie(final String sessionId) {
        return buildBaseCookie(sessionId)
                .maxAge(COOKIE_DELETED)
                .build();
    }

    private CookieBuilder buildBaseCookie(String sessionId) {
        return new CookieBuilder(this.name, sessionId)
                .httpOnly(this.httpOnly)
                .secure(this.secure)
                .path(this.path)
                .domain(this.domain);
    }

    /**
     * Builder class for creating Cookie instances with a fluent API
     */
    private static class CookieBuilder {
        private final Cookie cookie;

        CookieBuilder(String name, String value) {
            this.cookie = new Cookie(name, value);
        }

        CookieBuilder httpOnly(boolean httpOnly) {
            cookie.setHttpOnly(httpOnly);
            return this;
        }

        CookieBuilder secure(boolean secure) {
            cookie.setSecure(secure);
            return this;
        }

        CookieBuilder path(String path) {
            cookie.setPath(path);
            return this;
        }

        CookieBuilder maxAge(int maxAge) {
            cookie.setMaxAge(maxAge);
            return this;
        }

        CookieBuilder domain(String domain) {
            cookie.setDomain(domain);
            return this;
        }

        Cookie build() {
            return cookie;
        }
    }
}