package me.abouabra.zovo.enums;

/**
 * Represents various namespaces used in Redis operations.
 * <p>
 * This enum provides a predefined set of namespaces for categorizing Redis keys:
 * <ul>
 *   <li><b>RATE_LIMIT</b>: Used for rate-limiting operations.</li>
 *   <li><b>STORAGE</b>: Used for general storage operations.</li>
 *   <li><b>CACHE</b>: Used for caching purposes.</li>
 * </ul>
 */
public enum RedisNamespace {
    RATE_LIMIT("rateLimit"),
    STORAGE("storage"),
    CACHE("cache");

    private final String namespace;

    RedisNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return namespace;
    }
}
