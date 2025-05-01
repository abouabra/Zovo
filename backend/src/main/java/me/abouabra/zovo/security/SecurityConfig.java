package me.abouabra.zovo.security;

import me.abouabra.zovo.services.UserPrincipalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.context.*;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;


/**
 * <p>The <code>SecurityConfig</code> class is a configuration class for setting up security policies and
 * integrating Spring Security into the application.</p>
 *
 * <p>It configures authentication, authorization, custom security behavior, and session handling using
 * Spring Security's APIs. This includes defining public and protected endpoints, password encoding,
 * and exception handling.</p>
 *
 * <p>Annotated with <code>@Configuration</code> and <code>@EnableWebSecurity</code>, it enables security
 * support and customization within the Spring context.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINTS = {"/api/v1/auth/**", "/favicon.ico"};
    private static final String[] TWO_FACTOR_AUTH_ENDPOINTS = {"/api/v1/auth/2fa/**"};
    private static final String[] ADMIN_ACCESS_PATHS = {"/api/v1/admin/**"};
    private final UserPrincipalService userPrincipalService;
    private final int bcryptStrength;


    public SecurityConfig(UserPrincipalService userPrincipalService,
                          @Value("${security.bcrypt.strength}") int bcryptStrength) {
        this.userPrincipalService = userPrincipalService;
        this.bcryptStrength = bcryptStrength;
    }


    /**
     * Configures and builds a {@link SecurityFilterChain} for application security settings.
     *
     * @param httpSecurity        the {@link HttpSecurity} object to configure security.
     * @param authEntryPoint      the custom {@link AuthenticationEntryPoint} for handling authentication errors.
     * @param accessDeniedHandler the custom {@link AccessDeniedHandler} for handling access denials.
     * @return the configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity,
                                           CustomAuthenticationEntryPoint authEntryPoint,
                                           CustomAccessDeniedHandler accessDeniedHandler,
                                           SecurityContextRepository securityContextRepository,
                                           HttpSessionRequestCache requestCache,
                                           RefreshSessionCookieFilter refreshSessionCookieFilter) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .securityContext(securityContext -> securityContext
                        .securityContextRepository(securityContextRepository)
                )
                .requestCache(cache -> cache
                        .requestCache(requestCache)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(TWO_FACTOR_AUTH_ENDPOINTS).authenticated()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(ADMIN_ACCESS_PATHS).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )


                .authenticationProvider(createAuthenticationProvider())

                .addFilterAfter(refreshSessionCookieFilter, SecurityContextHolderFilter.class)

                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentSecurityPolicy(csp -> csp.policyDirectives("script-src 'self'"))
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );
        return httpSecurity.build();
    }


    /**
     * Creates and provides an {@link AuthenticationProvider} bean for authentication.
     *
     * <p>Configures a {@link DaoAuthenticationProvider} with a password encoder and
     * a user details service for handling authentication logic.</p>
     *
     * @return an instance of {@link AuthenticationProvider}.
     */
    @Bean
    public AuthenticationProvider createAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(createPasswordEncoder());
        provider.setUserDetailsService(createUserDetailsService());
        return provider;
    }

    /**
     * Provides the {@link AuthenticationManager} bean for authentication configuration.
     *
     * @param config the {@link AuthenticationConfiguration} to retrieve the authentication manager from.
     * @return an instance of {@link AuthenticationManager}.
     * @throws Exception if unable to retrieve the authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    /**
     * Creates and provides a {@link UserDetailsService} bean for authentication.
     *
     * <p>Delegates user-loading functionality to {@code userPrincipalService::loadUserByUsername},
     * enabling integration with Spring Security for user authentication.</p>
     *
     * @return an instance of {@link UserDetailsService} that retrieves user details by username.
     */
    @Bean
    public UserDetailsService createUserDetailsService() {
        return userPrincipalService;
    }


    /**
     * Provides a bean instance of {@link PasswordEncoder} using BCrypt hashing.
     *
     * <p>Uses the configured bcrypt strength to ensure secure password encoding.</p>
     *
     * @return an instance of {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder createPasswordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    /**
     * Configures and provides a {@link SecurityContextRepository} bean that stores
     * the {@link org.springframework.security.core.context.SecurityContext} in the
     * HTTP session.
     *
     * @return an instance of {@link HttpSessionSecurityContextRepository}.
     */
//    @Bean
//    public SecurityContextRepository securityContextRepository() {
//        return new HttpSessionSecurityContextRepository();
//    }
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new HttpSessionSecurityContextRepository(),
                new RequestAttributeSecurityContextRepository()
        );
    }

    /**
     * Configure the request cache to use HTTP sessions
     */
    @Bean
    public HttpSessionRequestCache httpSessionRequestCache() {
        return new HttpSessionRequestCache();
    }

}
