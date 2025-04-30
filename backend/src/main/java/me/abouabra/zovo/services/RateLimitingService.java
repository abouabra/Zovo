package me.abouabra.zovo.services;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Replace this with a proper rate limiting implementation with Redis
// TODO: understand this
@Service
public class RateLimitingService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    // Store attempts count with key format "action:identifier"
    private final Map<String, Integer> attemptCounter = new ConcurrentHashMap<>();

    // Store lockout expiry time with the key format "action:identifier:lockout"
    private final Map<String, Instant> lockoutExpiry = new ConcurrentHashMap<>();

    /**
     * Checks if the given identifier and action combination is currently rate-limited.
     *
     * @param identifier the unique identifier for the user or entity being checked.
     * @param action     the specific action being attempted.
     * @return {@code true} if the combination is rate-limited, {@code false} otherwise.
     */
    public boolean isRateLimited(String identifier, String action) {
        String key = generateKey(identifier, action);
        String lockoutKey = key + ":lockout";

        Instant expiryTime = lockoutExpiry.get(lockoutKey);
        if (expiryTime != null && expiryTime.isAfter(Instant.now()))
            return true;
        else if (expiryTime != null)
            lockoutExpiry.remove(lockoutKey);

        Integer attempts = attemptCounter.get(key);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    /**
     * Records a failed attempt for a specific action and identifier.
     * <p>
     * If the number of failed attempts exceeds the maximum allowed,
     * the identifier will be locked for a predefined duration.
     *
     * @param identifier A unique identifier for the entity (e.g., user ID).
     * @param action The action being attempted (e.g., "login").
     */
    public void recordFailedAttempt(String identifier, String action) {
        String key = generateKey(identifier, action);
        String lockoutKey = key + ":lockout";

        Integer attempts = attemptCounter.getOrDefault(key, 0);
        int newAttempts = attempts + 1;
        attemptCounter.put(key, newAttempts);

        if (newAttempts >= MAX_ATTEMPTS) {
            Instant expiry = Instant.now().plus(Duration.ofMinutes(LOCKOUT_DURATION_MINUTES));
            lockoutExpiry.put(lockoutKey, expiry);

            attemptCounter.put(key + ":cleanup", (int) Instant.now().getEpochSecond());
        }
    }

    /**
     * Resets the recorded attempts and lockout status for a specific identifier and action.
     *
     * @param identifier the unique identifier for the user or entity.
     * @param action the specific action associated with the attempts.
     */
    public void resetAttempts(String identifier, String action) {
        String key = generateKey(identifier, action);
        attemptCounter.remove(key);
        lockoutExpiry.remove(key + ":lockout");
    }


    /**
     * Retrieves the remaining lockout duration for a specific identifier and action.
     *
     * @param identifier The unique identifier associated with the action.
     * @param action The action for which the lockout duration is being retrieved.
     * @return The number of seconds remaining in the lockout period, or 0 if no lockout is active.
     */
    public long getLockoutDurationRemaining(String identifier, String action) {
        String lockoutKey = generateKey(identifier, action) + ":lockout";

        Instant expiryTime = lockoutExpiry.get(lockoutKey);
        if (expiryTime != null && expiryTime.isAfter(Instant.now())) {
            return Duration.between(Instant.now(), expiryTime).getSeconds();
        }

        return 0;
    }

    /**
     * Generates a unique key by concatenating the action and identifier.
     * <p>The format of the returned key is "action:identifier".</p>
     *
     * @param identifier A unique identifier for the entity or user.
     * @param action The action associated with the key.
     * @return A concatenated string representing the unique key.
     */
    private String generateKey(String identifier, String action) {
        return action + ":" + identifier;
    }

    /**
     * Removes expired entries from the lockout expiry map.
     * <p>
     * Entries with timestamps earlier than the current time
     * are removed to free up resources and maintain map integrity.
     * <p>
     * This method is designed to handle expired lockout entries
     * efficiently in the rate-limiting process.
     */
    public void cleanupExpiredEntries() {
        Instant now = Instant.now();

        lockoutExpiry.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}