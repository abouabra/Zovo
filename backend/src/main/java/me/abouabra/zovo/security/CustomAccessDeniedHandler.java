package me.abouabra.zovo.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import me.abouabra.zovo.exceptions.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * A custom implementation of {@link AccessDeniedHandler} that handles Access Denied exceptions
 * in a Spring Security context.
 *
 * <p>
 * This handler is invoked when a user attempts to access a resource for which they do not have the
 * necessary permissions. It sends a JSON-formatted error response containing details about the
 *  access-denied error.
 * </p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li>Formats the error response as JSON.</li>
 *     <li>Uses {@link ErrorResponse} to encapsulate error details.</li>
 *     <li>Returns an HTTP 403 (Forbidden) status code.</li>
 * </ul>
 *
 * <p><b>Implementation Details:</b></p>
 * <ul>
 *     <li>Sets the HTTP response status to <code>403</code>.</li>
 *     <li>Sets the content type of the response to <code>application/json</code>.</li>
 *     <li>Serializes the {@link ErrorResponse} object to the response output stream
 *         using a provided {@link ObjectMapper}.</li>
 * </ul>
 *
 * <p>
 * This class is annotated with <code>@Component</code>, so it is automatically detected
 * and registered as a bean within the Spring context. Additionally, <code>@AllArgsConstructor</code>
 * is used to generate a constructor that autowires dependencies such as {@link ObjectMapper}.
 * </p>
 *
 * <p><b>JSON Response Structure:</b></p>
 * <ul>
 *     <li><code>code</code>: The error code, set as "403" to represent a forbidden request.</li>
 *     <li><code>message</code>: A detailed message explaining the cause of the access denial.</li>
 * </ul>
 */
@Component
@AllArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponse errorResponse = new ErrorResponse(
                "403",
                accessDeniedException.getMessage()
        );

        response.setStatus(403);
        response.setContentType("application/json");

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
