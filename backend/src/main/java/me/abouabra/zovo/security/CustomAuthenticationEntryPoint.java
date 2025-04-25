package me.abouabra.zovo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.exceptions.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * A custom implementation of {@link AuthenticationEntryPoint} that handles authentication
 * errors in a Spring Security context by sending a structured JSON response.
 *
 * <p>
 * This entry point is triggered whenever an unauthenticated user attempts to access a secured
 * resource requiring authentication. Instead of the default Spring Security error page, this class
 * provides a JSON-formatted error response containing details about the error.
 * </p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li>Formats the error response as JSON.</li>
 *     <li>Uses {@link ErrorResponse} to encapsulate error details consistently.</li>
 *     <li>Returns an HTTP 401 (Unauthorized) status code.</li>
 * </ul>
 *
 * <p><b>Implementation Details:</b></p>
 * <ul>
 *     <li>Sets the HTTP response status to <code>401 (Unauthorized)</code>.</li>
 *     <li>Sets the content type of the response to <code>application/json</code>.</li>
 *     <li>Serializes the {@link ErrorResponse} object to the response output stream using
 *         a provided {@link ObjectMapper}.</li>
 *     <li>Extracts the error message from {@link AuthenticationException} and includes it
 *         in the error response.</li>
 * </ul>
 *
 * <p>
 * This class is annotated with <code>@Component</code>, allowing it to be automatically
 * detected and registered as a Spring bean. Additionally, the <code>@AllArgsConstructor</code>
 * annotation is used for dependency injection of the {@link ObjectMapper}.
 * </p>
 *
 * <p><b>JSON Response Structure:</b></p>
 * <ul>
 *     <li><code>code</code>: The error code, set as "401" to represent an unauthorized request.</li>
 *     <li><code>message</code>: A detailed message explaining the reason for the authentication error.</li>
 * </ul>
 */
@Component
@AllArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        ErrorResponse errorResponse = new ErrorResponse(
            "401",
            authException.getMessage()
        );

        response.setStatus(401);
        response.setContentType("application/json");

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
