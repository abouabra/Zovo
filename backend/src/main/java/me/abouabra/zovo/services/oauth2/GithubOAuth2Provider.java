package me.abouabra.zovo.services.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Slf4j
@Component
public class GithubOAuth2Provider implements OAuth2Provider {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.github.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.github.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.github.user-info-uri}")
    private String userInfoUri;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String redirectUri;


    /**
     * Retrieves the name of the OAuth2 provider.
     *
     * @return the name of the provider as a {@code String}.
     */
    @Override
    public String getName() {
        return "github";
    }

    /**
     * Builds the authorization URL for initiating the OAuth2 flow.
     *
     * @param state A unique state parameter to maintain request validation and prevent CSRF attacks.
     * @return The authorization URL including query parameters such as client ID, redirect URI, scope, and state.
     */
    @Override
    public String buildAuthorizationUrl(String state) {
        return UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "user:email read:user")
                .queryParam("state", state)
                .build().toString();
    }

    /**
     * Builds and returns token request parameters required for the token exchange process.
     *
     * @param code the authorization code obtained from the authorization server.
     * @return a {@code MultiValueMap} containing key-value pairs with the required token request parameters.
     */
    @Override
    public MultiValueMap<String, String> buildTokenRequestParams(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);

        return params;
    }

    /**
     * Retrieves the endpoint URL used to get OAuth2 tokens.
     *
     * @return a {@code String} representing the token endpoint URI.
     */
    @Override
    public String getTokenEndpoint() {
        return tokenUri;
    }

    /**
     * Returns the endpoint URI for retrieving user information.
     *
     * @return the user info endpoint URI as a String.
     */
    @Override
    public String getUserInfoEndpoint() {
        return userInfoUri;
    }

    /**
     * Extracts user details such as email, name, id, and avatar URL from the provided user information.
     *
     * @param userInfo a map containing user information retrieved from the OAuth2 provider.
     * @param accessToken the access token for making additional API calls if needed.
     * @return a map with keys "email", "name", "id", and "avatar_url" containing respective user details.
     */
    @Override
    public Map<String, String> extractUserDetails(Map<String, Object> userInfo, String accessToken) {
        Map<String, String> details = new HashMap<>();

        String email = (String) userInfo.get("email");
        if (email == null || email.isEmpty()) {
            email = fetchPrimaryEmail(accessToken);
        }

        if (email == null || email.isEmpty())
            email = userInfo.get("login") + "@users.noreply.github.com";

        details.put("email", email.toLowerCase(Locale.ROOT));
        details.put("name", (String) userInfo.getOrDefault("name", ""));
        details.put("id", userInfo.get("id").toString());
        details.put("avatar_url", (String) userInfo.getOrDefault("avatar_url", ""));

        return details;
    }

    /**
     * Provides additional HTTP headers for API requests.
     * <p>
     * Adds the "Accept" header with the "application / json" media type to indicate
     * the desired response format for API communication.
     *
     * @return an {@link HttpHeaders} object containing the additional headers.
     */
    @Override
    public HttpHeaders getAdditionalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    /**
     * Fetches the primary and verified email address of a user from the GitHub API.
     *
     * @param accessToken The OAuth2 access token to authenticate the request.
     * @return The primary and verified email address, or {@code null} if none is found.
     */
    private String fetchPrimaryEmail(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Map<String, Object>> emails = response.getBody();
        if (emails != null) {
            for (Map<String, Object> emailEntry : emails) {
                Boolean primary = (Boolean) emailEntry.get("primary");
                Boolean verified = (Boolean) emailEntry.get("verified");
                if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                    return (String) emailEntry.get("email");
                }
            }
        }

        return null;
    }

}