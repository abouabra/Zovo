package me.abouabra.zovo.exceptions;

/**
 * Exception thrown when an invalid two-factor authentication code is provided.
 * <p>
 * Typically used in scenarios where two-factor authentication is enabled,
 * and the code entered by the user does not match the expected value.
 * <p>
 * This exception is mapped to a specific HTTP response in the application,
 * usually indicating an authentication failure.
 */
public class InvalidTwoFactorCodeException extends RuntimeException {
    public InvalidTwoFactorCodeException(String message) {
        super(message);
    }
}
