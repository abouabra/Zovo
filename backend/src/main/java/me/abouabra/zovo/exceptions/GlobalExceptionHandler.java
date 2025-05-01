package me.abouabra.zovo.exceptions;

import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;


/**
 * Global exception handler that intercepts and handles various application-level
 * exceptions, providing structured API responses for clients.
 * <p>
 * Uses Spring's {@code @RestControllerAdvice} to catch exceptions thrown
 * in the application and return meaningful HTTP responses with appropriate status codes and messages.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ApiResponse.failure(HttpStatus.CONFLICT, ApiCode.USER_ALREADY_EXISTS, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleUserNotFound(UserNotFoundException ex) {
        return ApiResponse.failure(HttpStatus.NOT_FOUND, ApiCode.USER_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleRoleNotFound() {
        return ApiResponse.failure(HttpStatus.NOT_FOUND, ApiCode.ROLE_NOT_FOUND, "No role found with this name.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleUsernameNotFound() {
        return ApiResponse.failure(HttpStatus.NOT_FOUND, ApiCode.USER_NOT_FOUND, "No user found with this email.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleNoResourceFound() {
        return ApiResponse.failure(HttpStatus.NOT_FOUND, ApiCode.RESOURCE_NOT_FOUND, "The requested endpoint or resource could not be found. Please check the URL and try again.");
    }


    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleInternalAuthenticationService(InternalAuthenticationServiceException ex) {
        // Check if the cause is UsernameNotFoundException
        if (ex.getCause() instanceof UsernameNotFoundException)
            return ApiResponse.failure(HttpStatus.NOT_FOUND, ApiCode.USER_NOT_FOUND, "No user found with this email.");

        return ApiResponse.failure(HttpStatus.UNAUTHORIZED, ApiCode.UNAUTHORIZED, "Authentication failed");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleDisabled() {
        return ApiResponse.failure(ApiCode.ACCOUNT_DISABLED, "Your account is not activated. Please check your email for activation instructions.");
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleRateLimited(TooManyRequestsException ex) {
        return ApiResponse.failure(ApiCode.RATE_LIMITED, ex.getMessage());
    }

    @ExceptionHandler(TwoFactorAuthAlreadyEnabledException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleTwoFactorAuthAlreadyEnabled(TwoFactorAuthAlreadyEnabledException ex) {
        return ApiResponse.failure(ApiCode.TWO_FACTOR_ALREADY_ENABLED, ex.getMessage());
    }





    // all the below are just general fallbacks
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleHttpRequestMethodNotSupported() {
        return ApiResponse.failure(HttpStatus.METHOD_NOT_ALLOWED, ApiCode.METHOD_NOT_ALLOWED, "HTTP request method not supported.");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        return ApiResponse.failure(ApiCode.BAD_CREDENTIALS, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Invalid value for parameter: " + ex.getName();
        String details = "";
        if (ex.getRequiredType() != null)
            details = "Expected type: " + ex.getRequiredType().getName() + ", actual value: " + ex.getValue();

        return ApiResponse.failure(HttpStatus.BAD_REQUEST, ApiCode.INVALID_VALUE, errorMessage, List.of(details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .toList();
        return ApiResponse.failure(HttpStatus.BAD_REQUEST, ApiCode.INVALID_VALUE, "Input validation failed for one or more fields.", validationErrors);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<? extends ApiResponse<?>> handleHttpMessageNotReadable() {
        return ApiResponse.failure(ApiCode.INVALID_JSON, "Malformed or unreadable JSON input.");
    }


    //     Catch-all for unexpected exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<? extends ApiResponse<?>> handleAllUncaughtExceptions(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ApiResponse.failure(ApiCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}