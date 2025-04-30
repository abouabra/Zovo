package me.abouabra.zovo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * <p>The <code>CustomAccessDeniedHandler</code> class provides a custom implementation
 * for handling access denied exceptions in a Spring Security context.</p>
 *
 * <ul>
 *   <li>Transforms access denied exceptions into standardized API responses.</li>
 *   <li>Returns a JSON response with a <code>FORBIDDEN</code> status code.</li>
 *   <li>Uses <code>ObjectMapper</code> to serialize the response body.</li>
 * </ul>
 */
@Component
@AllArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ResponseEntity<? extends ApiResponse<?>> responseEntity = ApiResponse.failure(HttpStatus.FORBIDDEN, ApiCode.FORBIDDEN, accessDeniedException.getMessage());

        response.setStatus(responseEntity.getStatusCode().value());
        response.setContentType("application/json");

        objectMapper.writeValue(response.getOutputStream(), responseEntity.getBody());
    }
}
