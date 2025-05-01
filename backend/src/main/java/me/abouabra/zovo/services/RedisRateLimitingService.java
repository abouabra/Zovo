package me.abouabra.zovo.services;

import lombok.RequiredArgsConstructor;
import me.abouabra.zovo.enums.RateLimitingAction;
import me.abouabra.zovo.exceptions.TooManyRequestsException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A service for implementing rate-limiting functionality using Redis.
 *
 * <p>This service tracks user actions to enforce rate limits by maintaining
 * attempt counts and expiration windows in Redis. It supports identifying
 * when limits are exceeded, recording failed attempts, and resetting counters.</p>
 */
@Service
@RequiredArgsConstructor
public class RedisRateLimitingService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final StringRedisTemplate redis;

    /**
     * Generates a SHA-256 hash of the provided identifier.
     *
     * @param identifier the input string to be hashed
     * @return the SHA-256 hexadecimal representation of the input
     */
    public String hashIdentifier(String identifier) {
        return DigestUtils.sha256Hex(identifier);
    }

    /**
     * Generates a unique Redis key for rate-limiting purposes.
     *
     * @param identifier The identifier of the subject to be rate-limited.
     * @param action The specific action being rate-limited.
     * @return A unique key for storing rate-limiting data in Redis.
     */
    private String key(String identifier, String action) {
        return "ratelimit:" + action + ":" + hashIdentifier(identifier);
    }

    /**
     * Checks if the given identifier and action have reached the rate limit.
     *
     * <p>This method determines if the number of attempts for a specific identifier and action
     * exceeds the predefined limit.</p>
     *
     * @param identifier The unique identifier for the user or entity being rate-limited.
     * @param action The action to be checked for rate limiting.
     * @return {@code true} if the action is rate-limited, {@code false} otherwise.
     */
    public boolean isRateLimited(String identifier, String action) {
        String k = key(identifier, action);
        String value = redis.opsForValue().get(k);
        return value != null && Integer.parseInt(value) >= MAX_ATTEMPTS;
    }

    /**
     * Records a failed attempt for a specific identifier and action, incrementing the attempt count.
     * If it is the first attempt, sets an expiration time for the rate-limit window.
     *
     * @param identifier A unique identifier, such as a user ID or IP address, used for tracking attempts.
     * @param action     The specific action being rate-limited (e.g., login or registration).
     */
    public void recordFailedAttempt(String identifier, String action) {
        String k = key(identifier, action);

        Long count = redis.opsForValue().increment(k);
        if (count != null && count == 1L)
            redis.expire(k, WINDOW.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Resets the failed attempt count for a specific identifier and action.
     *
     * @param identifier the unique identifier for the entity being rate limited, such as a user or IP address.
     * @param action the specific action being rate limited (e.g., login attempts).
     */
    public void resetAttempts(String identifier, String action) {
        redis.delete(key(identifier, action));
    }

    /**
     * Retrieves the remaining lockout duration for a given identifier and action.
     *
     * @param identifier the unique identifier of the user or entity.
     * @param action the specific action being rate-limited.
     * @return the remaining lockout duration in seconds, or 0 if no lockout is active.
     */
    public long getLockoutDurationRemaining(String identifier, String action) {
        Long ttl = redis.getExpire(key(identifier, action), TimeUnit.SECONDS);
        return ttl > 0 ? ttl : 0L;
    }


    /**
     * Wraps the execution of a function while applying rate limiting logic.
     *
     * @param <T>           The type of the function's return value.
     * @param identifier    The unique identifier for rate-limiting checks.
     * @param action        The specific action being performed (e.g., LOGIN, REGISTER).
     * @param function      The main function to execute if the rate limit is not exceeded.
     * @param onRateLimited The fallback function to execute if the rate limit is exceeded.
     * @return The result from either the main function or the fallback function.
     */
    public <T> T wrap(String identifier, RateLimitingAction action, Supplier<T> function, Supplier<T> onRateLimited) {
        if (isRateLimited(identifier, action.toString())) {
            return onRateLimited.get();
        }

        try {
            return function.get();
        } catch (Exception e) {
            recordFailedAttempt(identifier, action.toString());
            throw e;
        }
    }

    /**
     * Wraps the execution of a function while applying rate limiting.
     *
     * @param <T>        The type of the return value.
     * @param identifier A unique identifier for the entity to be rate-limited.
     * @param action     The specific action being rate-limited (e.g., login or register).
     * @param function   The function to execute if the rate limit is not exceeded.
     * @return The result of the executed function.
     * @throws TooManyRequestsException if the rate limit is exceeded.
     */
    public <T> T wrap(String identifier, RateLimitingAction action, Supplier<T> function) {
        if (isRateLimited(identifier, action.toString())) {
            long timeRemaining = getLockoutDurationRemaining(identifier, action.toString());
            String message = String.format("Too many failed attempts. Try again in %d minutes.", (timeRemaining / 60));
            throw new TooManyRequestsException(message);
        }

        try {
            return function.get();
        } catch (Exception e) {
            recordFailedAttempt(identifier, action.toString());
            throw e;
        }
    }
}
