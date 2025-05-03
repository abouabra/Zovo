package me.abouabra.zovo.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.abouabra.zovo.enums.ApiCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Serializable;
import java.util.Map;


/**
 * <p>Represents a standardized structure for API responses.</p>
 *
 * <ul>
 *   <li>Encapsulates a message, an API response code, and optional details.</li>
 *   <li>Provides utility methods for uniform success and failure responses.</li>
 *   <li>Facilitates structured REST API communication.</li>
 *   <li>Supports generic type details for flexible response payloads.</li>
 * </ul>
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

    public static <T> ResponseEntity<ApiResponse<T>> redirect(String callbackUri, Map<String, Object> responseData) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(callbackUri);

        for (Map.Entry<String, Object> entry : responseData.entrySet()) {
            uriComponentsBuilder.queryParam(entry.getKey(), entry.getValue());
        }
        String redirectUri = uriComponentsBuilder.build().toString();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUri)
                .build();
    }
}

