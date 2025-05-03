package me.abouabra.zovo.services.redis;

import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.enums.RedisGroupAction;
import me.abouabra.zovo.enums.RedisNamespace;
import me.abouabra.zovo.exceptions.TooManyRequestsException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A service for managing rate-limiting functionality using Redis.
 * <p>
 * Implements logic to limit the frequency of actions performed by users or entities
 * by tracking attempts and enforcing lockout durations.
 */
@Slf4j
@Service
public class RedisRateLimitingService {

    private final int maxAttempts;
    private final int windowInSeconds;
    private final StringRedisTemplate redis;

    /**
     * Constructs the RedisRateLimitingService with specified rate-limiting configurations.
     *
     * @param maxAttempts     The maximum number of allowed attempts during the rate-limiting window.
     * @param windowInSeconds The duration of the rate-limiting window in seconds.
     * @param redis           The StringRedisTemplate instance for accessing Redis.
     */
    public RedisRateLimitingService(@Value("${app.redis.rate-limiting.max-attempts:5}") int maxAttempts, @Value("${app.redis.rate-limiting.window-seconds:900}") int windowInSeconds, StringRedisTemplate redis) {
        this.maxAttempts = maxAttempts;
        this.windowInSeconds = windowInSeconds;
        this.redis = redis;
    }

    /**
     * Generates a SHA-256 hash for the given identifier.
     *
     * @param identifier the input string to be hashed
     * @return the hashed string in hexadecimal format
     */
    public String hashIdentifier(String identifier) {
        return DigestUtils.sha256Hex(identifier);
    }

    /**
     * Constructs a Redis key for rate-limiting purposes based on the identifier and action.
     *
     * @param identifier The unique identifier for the client or user.
     * @param action     The specific action being rate-limited.
     * @return A formatted Redis key as a string.
     */
    private String key(String identifier, String action) {
        return RedisNamespace.RATE_LIMIT + ":" + action + ":" + hashIdentifier(identifier);
    }

    /**
     * Checks if an action associated with a specific identifier is rate-limited.
     *
     * @param identifier A unique identifier for the user or entity (e.g., user ID or IP address).
     * @param action     The specific action that is being checked for rate limiting.
     * @return {@code true} if the action is rate-limited, {@code false} otherwise.
     */
    public boolean isRateLimited(String identifier, String action) {
        String k = key(identifier, action);
        String value = redis.opsForValue().get(k);
        return value != null && Integer.parseInt(value) >= maxAttempts;
    }

    /**
     * Records a failed attempt for the specified identifier and action.
     * <p>
     * Increments the failure count in Redis and sets an expiration time
     * if this is the first recorded attempt.
     *
     * @param identifier the unique identifier for the subject (e.g., user, IP).
     * @param action     the specific action being rate limited.
     */
    public void recordFailedAttempt(String identifier, String action) {
        String k = key(identifier, action);

        Long count = redis.opsForValue().increment(k);
        if (count != null && count == 1L)
            redis.expire(k, Duration.ofSeconds(windowInSeconds).getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Retrieves the remaining lockout duration for the specified identifier and action.
     *
     * @param identifier The unique identifier for the user or entity.
     * @param action     The specific action being rate-limited.
     * @return The remaining lockout duration in seconds, or 0 if no lockout is active.
     */
    public long getLockoutDurationRemaining(String identifier, String action) {
        Long ttl = redis.getExpire(key(identifier, action), TimeUnit.SECONDS);
        return ttl > 0 ? ttl : 0L;
    }

    /**
     * Wraps the execution of a function with rate-limiting checks and failure handling.
     *
     * @param <T>        The return type of the function being wrapped.
     * @param action     The Redis action being performed.
     * @param identifier A unique identifier related to the action, used for rate-limiting.
     * @param function   The logic to execute, supplied as a {@link Supplier}.
     * @return The result produced by the executed function.
     * @throws TooManyRequestsException if the rate limit is exceeded for the given identifier and action.
     */
    public <T> T wrap(RedisGroupAction action, String identifier, Supplier<T> function) {
        if (isRateLimited(identifier, action.toString())) {
            long timeRemaining = getLockoutDurationRemaining(identifier, action.toString());
            String message = String.format("Too many failed attempts. Try again in %d minutes.", (timeRemaining / 60));
            throw new TooManyRequestsException(message);
        }

        try {
            return function.get();
        } catch (Exception e) {
            log.debug("Rate-limiting failed for action {} and identifier {}", action, identifier);
            recordFailedAttempt(identifier, action.toString());
            throw e;
        }
    }
}
