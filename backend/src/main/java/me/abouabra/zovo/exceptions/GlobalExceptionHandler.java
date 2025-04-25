package me.abouabra.zovo.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Global exception handler for managing errors and exceptions in a centralized manner.
 * <p>
 * This class utilizes the {@code @RestControllerAdvice} annotation to globally handle exceptions
 * thrown by controllers in the application. It maps different exception types to appropriate
 * HTTP response statuses and provides consistent error responses.
 * <p>
 * The handler generates responses using the {@code ErrorResponse} class, which ensures standardized
 * error structures for clients.
 *
 * <p>
 * Key features:
 * <ul>
 *     <li>Handles various specific exceptions such as {@code DataIntegrityViolationException},
 *         {@code UserNotFoundException}, and {@code RoleNotFoundException}, among others.</li>
 *     <li>Returns meaningful HTTP status codes such as 404 Not Found, 409 Conflict, 400 Bad Request,
 *         and 500 Internal Server Error, depending on the exception type.</li>
 *     <li>Provides detailed feedback to users, including error codes, messages, and additional
 *         context where applicable.</li>
 *     <li>Includes a fallback handler for unexpected exceptions, ensuring no unhandled errors propagate
 *         beyond the API boundary.</li>
 * </ul>
 *
 * <p>
 * Exception types handled:
 * <ul>
 *     <li>{@code DataIntegrityViolationException} - Conflicts in database operations like duplicate keys.</li>
 *     <li>{@code UserAlreadyExistsException} - Triggered when a user with the given details already exists.</li>
 *     <li>{@code UserNotFoundException} - Triggered when a requested user is not found.</li>
 *     <li>{@code RoleNotFoundException} - Triggered when a requested role is not found.</li>
 *     <li>{@code UsernameNotFoundException} - Used primarily for user authentication issues.</li>
 *     <li>{@code NoResourceFoundException} - For identifying missing resources based on endpoint requests.</li>
 *     <li>{@code InternalAuthenticationServiceException} - For authentication service failures,
 *         including nested {@code UsernameNotFoundException} scenarios.</li>
 *     <li>{@code MethodArgumentTypeMismatchException} - When a parameter type mismatch occurs in URL or method calls.</li>
 *     <li>{@code BadCredentialsException} - For invalid login credentials.</li>
 *     <li>{@code MethodArgumentNotValidException} - For input validation failures on request bodies.</li>
 *     <li>{@code HttpMessageNotReadableException} - For malformed or unreadable JSON input payloads.</li>
 *     <li>Generic {@code Exception} - Catch-all for unexpected exceptions.</li>
 * </ul>
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Logs unexpected exceptions using the application logger.</li>
 *     <li>Returns meaningful responses to facilitate debugging and enhance client interaction with the API.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage(); // gets "Key (email)=... already exists"
        ErrorResponse errorResponse = new ErrorResponse("DUPLICATE_KEY", "A record with the same unique field already exists.", List.of(message));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_ALREADY_EXISTS", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFoundException(RoleNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("ROLE_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_NOT_FOUND", "No user found with this email.");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("RESOURCE_NOT_FOUND", "No Resource found with specified endpoint.");
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException ex) {
        // Check if the cause is UsernameNotFoundException
        if (ex.getCause() instanceof UsernameNotFoundException) {
            ErrorResponse errorResponse = new ErrorResponse("USER_NOT_FOUND", "No user found with this email.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Handle other internal authentication errors
        ErrorResponse errorResponse = new ErrorResponse("AUTHENTICATION_ERROR", "Authentication failed");
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }












    // all the below are just general fall backs

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse("BAD_CREDENTIALS", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Invalid value for parameter: " + ex.getName();
        String details = "Expected type: " + ex.getRequiredType().getName();

        ErrorResponse errorResponse = new ErrorResponse(errorMessage, details);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .toList();

        ErrorResponse errorResponse = new ErrorResponse("VALIDATION_ERROR", "Input validation failed for one or more fields.", validationErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ErrorResponse errorResponse = new ErrorResponse("INVALID_JSON", "Malformed or unreadable JSON input.");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    //     Catch-all for unexpected exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllUncaughtExceptions(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred");
    }
}