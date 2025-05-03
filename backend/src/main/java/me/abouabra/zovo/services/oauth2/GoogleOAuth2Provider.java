package me.abouabra.zovo.services.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Provides an implementation of the {@link OAuth2Provider} interface for Google OAuth2.
 * <p>
 * This class handles building authorization URLs, token request parameters,
 * and extracting user details from the Google OAuth2 provider.
 * <p>
 * Properties such as client ID, client secret, and endpoint URIs
 * are injected through application configuration.
 */
@Slf4j
@Component
public class GoogleOAuth2Provider implements OAuth2Provider {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;


    /**
     * Retrieves the name of the OAuth2 provider.
     *
     * @return the name of the provider, which is "google".
     */
    @Override
    public String getName() {
        return "google";
    }

    /**
     * Builds the authorization URL for the Google OAuth2 flow.
     *
     * @param state a unique state parameter to prevent CSRF and maintain the request state.
     * @return the complete authorization URL as a String.
     */
    @Override
    public String buildAuthorizationUrl(String state) {

        return UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .build().toString();
    }

    /**
     * Builds a map of parameters for the token request during OAuth2 authorization.
     *
     * @param code the authorization code received from the authorization server.
     * @return a {@link MultiValueMap} containing request parameters such as
     * client ID, client secret, redirect URI, the received code, and grant type.
     */
    @Override
    public MultiValueMap<String, String> buildTokenRequestParams(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        return params;
    }

    /**
     * Retrieves the token endpoint URI for the OAuth2 provider.
     *
     * @return the token endpoint URI as a {@code String}.
     */
    @Override
    public String getTokenEndpoint() {
        return tokenUri;
    }

    /**
     * Provides the configured URI for fetching user information from the OAuth2 provider.
     *
     * @return A <code>String</code> representing the user information endpoint URI.
     */
    @Override
    public String getUserInfoEndpoint() {
        return userInfoUri;
    }

    /**
     * Extracts user details from the provided user information map and access token.
     *
     * @param userInfo    a {@link Map} containing user information retrieved from the provider.
     * @param accessToken a {@link String} representing the access token of the user.
     * @return a {@link Map} containing user details such as email, name, and ID.
     */
    @Override
    public Map<String, String> extractUserDetails(Map<String, Object> userInfo, String accessToken) {
        Map<String, String> details = new HashMap<>();
        String email = (String) userInfo.get("email");
        email = email.toLowerCase(Locale.ROOT);

        details.put("email", email);
        details.put("name", (String) userInfo.getOrDefault("name", email));
        details.put("id", (String) userInfo.get("sub"));

        return details;
    }
}