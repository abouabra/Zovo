package me.abouabra.zovo.services.oauth2;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * Represents an OAuth2 provider interface to standardize interaction with various OAuth2 services.
 * <p>
 * This interface provides methods for authorization, token retrieval, and user information extraction.
 * Different providers (e.g., Google, GitHub) are expected to implement this interface.
 */
public interface OAuth2Provider {
    String getName();

    String buildAuthorizationUrl(String state);

    MultiValueMap<String, String> buildTokenRequestParams(String code);

    String getTokenEndpoint();

    String getUserInfoEndpoint();

    Map<String, String> extractUserDetails(Map<String, Object> userInfo, String accessToken);

    default HttpHeaders getAdditionalHeaders() {
        return new HttpHeaders();
    }

}