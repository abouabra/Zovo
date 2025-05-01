package me.abouabra.zovo.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.abouabra.zovo.enums.ApiCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * A generic API response wrapper for consistent response structure.
 * <p>
 * Encapsulates a message, response code, and optional details payload.
 * Provides utility methods for constructing success and failure responses.
 * </p>
 *
 * @param <T> The type of the optional details payload.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> implements Serializable {
    private String message;
    private ApiCode code;
    private T details;


    public static <T> ResponseEntity<ApiResponse<T>> success(String message) {
        return ResponseEntity.ok(new ApiResponse<>(message, ApiCode.SUCCESS, null));
    }
    public static <T> ResponseEntity<ApiResponse<T>> success(T details) {
        return ResponseEntity.ok(new ApiResponse<>(null, ApiCode.SUCCESS, details));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T details) {
        return ResponseEntity.ok(new ApiResponse<>(message, ApiCode.SUCCESS, details));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, ApiCode code, T details) {
        return ResponseEntity.ok(new ApiResponse<>(message, code, details));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(ApiCode code, String message) {
        return failure(HttpStatus.BAD_REQUEST, code, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(HttpStatus status, ApiCode code, String message) {
        return ResponseEntity.status(status).body(new ApiResponse<>(message, code, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failure(HttpStatus status, ApiCode code, String message, T details) {
        return ResponseEntity.status(status).body(new ApiResponse<>(message, code, details));
    }
}

