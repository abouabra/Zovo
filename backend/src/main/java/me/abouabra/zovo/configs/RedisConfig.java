package me.abouabra.zovo.configs;

import me.abouabra.zovo.enums.RedisNamespace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

import java.time.Duration;

/**
 * The <code>RedisConfig</code> class configures Redis-related settings for caching, session
 * management, and template operations in a Spring application.
 * <p>
 * It defines default cache behavior, including TTL, custom key prefixes, and serialization mechanisms,
 * while establishing beans for Redis interaction, such as templates, cache managers, and serializers.
 * <p>
 * It extends {@link AbstractHttpSessionApplicationInitializer} to facilitate HTTP session management
 * with Redis.
 */
@Configuration
@EnableCaching
public class RedisConfig extends AbstractHttpSessionApplicationInitializer {

    private final int defaultTTL;

    /**
     * Creates a new instance of the RedisConfig class with a default TTL for cache entries.
     *
     * @param defaultTTL The default time-to-live (TTL) in seconds for cached entries.
     *                   If not specified, it defaults to 600 seconds.
     */
    public RedisConfig(@Value("${app.redis.cache.ttls.default:600}") int defaultTTL) {
        this.defaultTTL = defaultTTL;
    }

    /**
     * Configures the default Redis cache settings such as TTL, key prefixes, and value serialization.
     * <p>
     * Returns a {@link RedisCacheConfiguration} configured with:
     * <ul>
     * <li>Default time-to-live (TTL) for cache entries.</li>
     * <li>Disabled caching for null values.</li>
     * <li>Custom cache key prefixes.</li>
     * <li>JDK-based value serialization.</li>
     * </ul>
     *
     * @return the configured {@link RedisCacheConfiguration}.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(defaultTTL)) // Default 600 sec TTL for all caches
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> RedisNamespace.CACHE + ":" + cacheName + ":")
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jdkRedisSerializer())
                );
    }

    /**
     * Creates a {@link RedisCacheManager} for managing Redis-based caches.
     * <p>
     * The manager is configured with default cache settings including TTL
     * and serialization provided by {@link #cacheConfiguration()}.
     *
     * @param connectionFactory the Redis connection factory to connect to the Redis server
     * @return a configured {@link RedisCacheManager} instance
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration())
                .build();
    }

    /**
     * Provides a RedisSerializer for serializing objects using Java's default
     * serialization mechanism.
     *
     * @return a {@link RedisSerializer<Object>} instance using JDK serialization.
     */
    @Bean
    public RedisSerializer<Object> jdkRedisSerializer() {
        return new JdkSerializationRedisSerializer();
    }

    /**
     * Configures and returns a {@link RedisTemplate} for interacting with Redis.
     * <p>
     * This template uses custom serializers for keys, values, hash keys, and hash values
     * to ensure proper serialization and deserialization of data.
     *
     * @param connectionFactory the factory to configure connections to the Redis database
     * @return a configured instance of {@link RedisTemplate}
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(jdkRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jdkRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configures the default Redis serializer for Spring session.
     * <p>
     * This method defines a RedisSerializer for serializing session data stored in Redis,
     * using the JDK serialization mechanism.
     *
     * @return a {@link RedisSerializer} instance for serializing session objects.
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return jdkRedisSerializer();
    }
}