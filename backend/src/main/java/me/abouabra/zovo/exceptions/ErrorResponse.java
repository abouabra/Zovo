package me.abouabra.zovo.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the structure of an error response returned by the application
 * in case of exceptions or errors. This class encapsulates details
 * about the error to provide clear feedback and facilitate debugging.
 * <p>
 * The structure includes:
 * <ul>
 *     <li>A unique error code that identifies the type of error.</li>
 *     <li>A message explaining the error.</li>
 *     <li>Additional details that may provide further context about the error.</li>
 *     <li>A timestamp indicating when the error occurred.</li>
 * </ul>
 * <p>
 * This class provides multiple constructors for flexibility in creating
 * error responses with varying levels of detail.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private List<String> details;
    private String timestamp;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }
    public ErrorResponse(String code, String message, List<String> details) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = LocalDateTime.now().toString();
    }

}