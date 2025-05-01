package me.abouabra.zovo.configs;

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

@Configuration
@EnableCaching
public class CacheConfig extends AbstractHttpSessionApplicationInitializer {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // Default TTL for all caches
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jdkRedisSerializer())
                );
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration())
                .build();
    }

    @Bean
    public RedisSerializer<Object> jdkRedisSerializer() {
        return new JdkSerializationRedisSerializer();
    }

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

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return jdkRedisSerializer();
    }
}