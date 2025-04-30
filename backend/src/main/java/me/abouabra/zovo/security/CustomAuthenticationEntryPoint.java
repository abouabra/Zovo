package me.abouabra.zovo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * <p>CustomAuthenticationEntryPoint handles unauthorized access exceptions
 * in Spring Security and provides custom JSON responses.</p>
 *
 * <ul>
 *   <li>Triggers when an unauthorized request is made.</li>
 *   <li>Returns a formatted JSON response with error details.</li>
 *   <li>Uses {@code ObjectMapper} to serialize the {@code ApiResponse} body.</li>
 * </ul>
 */
@Component
@AllArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ResponseEntity<? extends ApiResponse<?>> responseEntity = ApiResponse.failure(HttpStatus.UNAUTHORIZED, ApiCode.UNAUTHORIZED, authException.getMessage());

        response.setStatus(responseEntity.getStatusCode().value());
        response.setContentType("application/json");

        objectMapper.writeValue(response.getOutputStream(), responseEntity.getBody());
    }
}
