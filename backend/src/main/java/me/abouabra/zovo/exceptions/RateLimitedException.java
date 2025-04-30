package me.abouabra.zovo.exceptions;

/**
 * Exception thrown when a user exceeds the allowed rate limit for requests.
 * <p>
 * This exception is typically used to enforce rate-limiting policies
 * and is mapped to an appropriate HTTP response, such as "429 Too Many Requests."
 */
public class RateLimitedException extends RuntimeException {
    public RateLimitedException(String message) {
        super(message);
    }
}
