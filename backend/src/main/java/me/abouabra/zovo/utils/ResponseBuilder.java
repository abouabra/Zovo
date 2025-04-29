package me.abouabra.zovo.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Utility class for building standardized HTTP responses.
 * <p>
 * Provides methods to construct success, error, or custom status
 * responses with a consistent structure.
 */
@Component
public class ResponseBuilder {

    /**
     * Builds a successful HTTP response with a status of "success" and a custom message.
     *
     * @param message The message to include in the response body.
     * @return ResponseEntity containing a map with "status" and "message" entries.
     */
    public ResponseEntity<Map<String, String>> success(String message) {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", message
        ));
    }

    /**
     * Constructs a bad request response with an error message.
     *
     * @param message the error message to include in the response body.
     * @return a {@code ResponseEntity} containing a map with keys "status" set to "error"
     *         and "message" containing the provided error message.
     */
    public ResponseEntity<Map<String, String>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", message
        ));
    }

    /**
     * Creates a ResponseEntity with a given HTTP status, status message, and message body.
     *
     * @param status the HTTP status to set in the response
     * @param statusMessage the status message to include in the response body
     * @param message the detailed message to include in the response body
     * @return a ResponseEntity containing the status, status message, and message body
     */
    public ResponseEntity<Map<String, String>> response(HttpStatus status, String statusMessage, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", statusMessage,
                "message", message
        ));
    }
}