package me.abouabra.zovo.services;

import me.abouabra.zovo.enums.RedisGroupAction;
import me.abouabra.zovo.enums.RedisNamespace;
import me.abouabra.zovo.models.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service for managing Redis-based storage for various namespace-based actions.
 * Provides capabilities for storing, retrieving, and handling TTL-based cache data.
 * <p>
 * Supports operations to handle keys, values, 2FA sessions, and roles in Redis.
 */
@Service
public class RedisStorageService {
    private final int twoFaTTL;
    private final int roleTTL;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Service for managing Redis-based storage for various namespace-based actions.
     * Provides capabilities for storing, retrieving, and handling TTL-based cache data.
     */
    public RedisStorageService(@Value("${app.redis.cache.ttls.two-fa:300}") int twoFaTTL, @Value("${app.redis.cache.ttls.role:3600}") int roleTTL, RedisTemplate<String, Object> redisTemplate) {
        this.twoFaTTL = twoFaTTL;
        this.roleTTL = roleTTL;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Constructs a Redis key by combining the storage namespace with the provided token.
     *
     * @param token The unique identifier to append to the storage namespace.
     * @return A fully qualified Redis key in the format "storage:<token>".
     */
    public String key(String token) {
        return RedisNamespace.STORAGE + ":" + token;
    }

    /**
     * Stores a value in Redis with the specified key and Time-To-Live (TTL).
     *
     * @param key the key under which the value is stored
     * @param value the value to store
     * @param ttl the duration for which the value should remain in the cache
     */
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * Retrieves a value from Redis by key and casts it to the specified type.
     *
     * @param key the Redis key to retrieve the value from.
     * @param clazz the expected class type of the retrieved value.
     * @return the value cast to the specified type, or null if not found or type mismatch.
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    /**
     * Deletes the specified key from the Redis storage.
     *
     * @param key The key to be deleted from Redis.
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Checks if a specified key exists in the Redis storage.
     *
     * @param key the key to check for existence in Redis.
     * @return {@code true} if the key exists, {@code false} otherwise.
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
     * Stores a role in Redis with a specified key and time-to-live (TTL).
     *
     * @param roleName the name of the role to be stored, used as part of the Redis key.
     * @param role the {@link Role} object to be stored in Redis.
     */
    public void setRole(String roleName, Role role) {
        String k = key(RedisGroupAction.ROLE + ":" + roleName);
        set(k, role, Duration.ofSeconds(roleTTL));
    }

    /**
     * Retrieves a Role object associated with the given role name from the Redis cache.
     *
     * @param roleName the name of the role to be retrieved.
     * @return the Role object if found, otherwise null.
     */
    public Role getRole(String roleName) {
        String k = key(RedisGroupAction.ROLE + ":" + roleName);
        return get(k, Role.class);
    }

    /**
     * Stores a Two-Factor Authentication (2FA) session in Redis with a specific TTL.
     *
     * @param token the unique 2FA token associated with the session
     * @param userEmail the email address of the user initiating the session
     */
    public void set2FASession(String token, String userEmail) {
        String k = key(RedisGroupAction.TWO_FA + ":" + token);
        set(k, userEmail, Duration.ofSeconds(twoFaTTL));
    }

    /**
     * Retrieves the session information associated with the provided two-factor authentication token.
     *
     * @param token The token used to identify the two-factor authentication session.
     * @return The session data as a String if it exists; otherwise, null.
     */
    public String get2FASession(String token) {
        String k = key(RedisGroupAction.TWO_FA + ":" + token);
        return get(k, String.class);
    }

    /**
     * Deletes a two-factor authentication (2FA) session associated with the specified token.
     * <p>
     * This removes the corresponding key-value entry from the Redis storage.
     *
     * @param token the unique identifier for the 2FA session to be deleted
     */
    public void delete2FASession(String token) {
        String k = key(RedisGroupAction.TWO_FA + ":" + token);
        delete(k);
    }
}
