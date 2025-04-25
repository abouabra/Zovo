package me.abouabra.zovo.security;

import me.abouabra.zovo.services.UserPrincipalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Configures the security framework for the application using Spring Security.
 *
 * <p>
 * This configuration contains definitions for authentication, password encoding, and key security filters.
 * It customizes the behavior of Spring Security to match application requirements, such as public endpoint
 * access, role-based access controls, session management policies, and exception handling.
 * </p>
 *
 * <p><b>Security Features:</b></p>
 * <ul>
 *     <li><strong>AuthenticationProvider Setup:</strong>
 *         <ul>
 *             <li>Uses a {@code DaoAuthenticationProvider} to handle user authentication.</li>
 *             <li>Configures a {@code PasswordEncoder} using bcrypt hashing for secure password storage.</li>
 *             <li>Integrates a {@link UserDetailsService} for loading user details during authentication.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Authorization Rules:</strong>
 *         <ul>
 *             <li>Grants public access to specified endpoints.</li>
 *             <li>Restricts access to admin endpoints based on the user role <code>ADMIN</code>.</li>
 *             <li>Requires authentication for all other endpoints.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Session Management:</strong>
 *         <ul>
 *             <li>Configures session creation policies to <code>IF_REQUIRED</code>, enabling stateful session handling when needed.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Exception Handling:</strong>
 *         <ul>
 *             <li>Uses a custom implementation of {@link CustomAuthenticationEntryPoint} to return structured JSON responses for authentication errors.</li>
 *             <li>Defines a custom {@link CustomAccessDeniedHandler} for handling authorization errors.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p><b>Bean Definitions:</b></p>
 * <ul>
 *     <li><strong>SecurityFilterChain:</strong> Configures the security filter chain with rules and settings for authentication and authorization.</li>
 *     <li><strong>AuthenticationProvider:</strong> Sets up authentication processing with password encoding and user details retrieval.</li>
 *     <li><strong>PasswordEncoder:</strong> Utilizes bcrypt password hashing with configurable strength for secure encoding.</li>
 *     <li><strong>UserDetailsService:</strong> Implements user retrieval logic via {@link UserPrincipalService}.</li>
 *     <li><strong>AuthenticationManager:</strong> Resolves and manages authentication requests based on the configuration.</li>
 * </ul>
 *
 * <p>
 * The {@code SecurityConfig} class ensures the application's security by clearly defining authentication
 * and authorization rules, as well as controlling sensitive operations such as password encoding.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINTS = {"/auth/login", "/auth/register"};
    private static final String ADMIN_ENDPOINT_PREFIX = "/admin/**";

    private final UserPrincipalService userPrincipalService;
    private final int bcryptStrength;

    public SecurityConfig(UserPrincipalService userPrincipalService,
                          @Value("${security.bcrypt-strength}") int bcryptStrength) {
        this.userPrincipalService = userPrincipalService;
        this.bcryptStrength = bcryptStrength;
    }

    /**
     * Configures the security filter chain with authentication and authorization rules.
     *
     * @param httpSecurity        Security configuration object
     * @param authEntryPoint      Custom authentication entry point for handling auth errors
     * @param accessDeniedHandler Custom handler for access denied scenarios
     * @return Configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           CustomAuthenticationEntryPoint authEntryPoint,
                                           CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        configureSecurityFilters(httpSecurity, authEntryPoint, accessDeniedHandler);
        return httpSecurity.build();
    }

    /**
     * Configures the security filters for the application by defining key settings such as
     * session management, authorization rules, and exception handling.
     *
     * <p>
     * This method sets up the following:
     * </p>
     * <ul>
     *     <li>Disables CSRF protection.</li>
     *     <li>Configures the session management policy to <code>IF_REQUIRED</code>.</li>
     *     <li>Defines authorization rules:
     *         <ul>
     *             <li>Allows public access to URIs matching <code>PUBLIC_ENDPOINTS</code>.</li>
     *             <li>Restricts access to URIs with <code>ADMIN_ENDPOINT_PREFIX</code> to users with the role <code>ADMIN</code>.</li>
     *             <li>Requires authentication for all other requests.</li>
     *         </ul>
     *     </li>
     *     <li>Registers a custom authentication provider for processing user authentication.</li>
     *     <li>Configures exception handling for authentication and access-related errors:
     *         <ul>
     *             <li>Uses the provided {@code authEntryPoint} for handling authentication entry point exceptions.</li>
     *             <li>Uses the provided {@code accessDeniedHandler} for handling access denial situations.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param httpSecurity        The {@code HttpSecurity} object used to configure security settings.
     * @param authEntryPoint      A custom implementation of {@code AuthenticationEntryPoint} for handling
     *                            unauthenticated access attempts.
     * @param accessDeniedHandler A custom implementation of {@code AccessDeniedHandler} for handling
     *                            access denial cases.
     * @throws Exception If an error occurs while configuring the security filters.
     */
    private void configureSecurityFilters(HttpSecurity httpSecurity,
                                          CustomAuthenticationEntryPoint authEntryPoint,
                                          CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(ADMIN_ENDPOINT_PREFIX).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(createAuthenticationProvider())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );
    }

    /**
     * Creates and configures an {@link AuthenticationProvider} for authentication operations.
     *
     * <p>
     * This method sets up a {@link DaoAuthenticationProvider} by configuring:
     * </p>
     * <ul>
     *     <li>a {@link PasswordEncoder} for encoding and verifying passwords, obtained via {@code createPasswordEncoder()}</li>
     *     <li>a {@link UserDetailsService} for loading user details during authentication, obtained via {@code createUserDetailsService()}</li>
     * </ul>
     *
     * <p>
     * The returned {@code AuthenticationProvider} is used as part of the Spring Security authentication mechanism
     * to handle authentication requests and validate credentials.
     * </p>
     *
     * @return an instance of {@link AuthenticationProvider} configured with a {@link PasswordEncoder}
     *         and a {@link UserDetailsService}.
     */
    @Bean
    public AuthenticationProvider createAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(createPasswordEncoder());
        provider.setUserDetailsService(createUserDetailsService());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    /**
     * Creates a {@link UserDetailsService} bean that delegates the loading of user details
     * to the {@link UserPrincipalService}. This service is used by Spring Security to retrieve
     * user-specific data during authentication.
     *
     * <p>
     * The returned {@link UserDetailsService} uses the {@code loadUserByUsername} method from
     * {@link UserPrincipalService}, which retrieves user details based on the provided
     * username or email.
     * </p>
     *
     * <p><b>Usage in Security Configuration:</b></p>
     * <ul>
     *     <li>Essential for authentication operations in a Spring Security context.</li>
     *     <li>Works in conjunction with other security components, such as the {@link AuthenticationProvider}.</li>
     * </ul>
     *
     * <p><b>Key Functionality:</b></p>
     * <ul>
     *     <li>Enables the retrieval of user information, including authorities, credentials, and account status.</li>
     *     <li>Throws {@link UsernameNotFoundException} if no user is found for the provided username or email.</li>
     * </ul>
     *
     * @return a configured {@link UserDetailsService} bean for use in Spring Security authentication mechanisms.
     */
    @Bean
    public UserDetailsService createUserDetailsService() {
        return userPrincipalService::loadUserByUsername;
    }

    /**
     * Creates and configures a {@link PasswordEncoder} bean for use in the authentication process.
     *
     * <p>
     * This method returns an instance of {@link BCryptPasswordEncoder}, configured with the strength
     * parameter {@code bcryptStrength}, which determines the computational complexity of encoding.
     * The BCrypt encoding algorithm is widely used for securely hashing passwords due to its ability
     * to incorporate a salt and its computational cost, providing resistance against brute-force attacks.
     * </p>
     *
     * <p>Key Features of the {@link BCryptPasswordEncoder}:</p>
     * <ul>
     *     <li>Incorporates a randomly generated salt for each password, protecting against rainbow table attacks.</li>
     *     <li>Ensures that the encoded password is computationally expensive to hash, deterring brute-force attempts.</li>
     *     <li>Supports automatic verification of passwords during authentication.</li>
     * </ul>
     *
     * @return a {@link PasswordEncoder} instance configured for secure password management.
     */
    @Bean
    public PasswordEncoder createPasswordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }
}
