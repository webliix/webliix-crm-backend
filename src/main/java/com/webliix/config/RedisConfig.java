package com.webliix.config;

import java.net.URI;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig implements CachingConfigurer {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.username:}")
    private String redisUsername;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean redisSsl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String host = redisHost;
        int port = redisPort;
        String username = redisUsername;
        String password = redisPassword;
        boolean useSsl = redisSsl;

        // Smart URI parsing for Cloud Redis URLs (e.g., redis://user:pass@host:port or rediss://...)
        if (host != null && (host.startsWith("redis://") || host.startsWith("rediss://"))) {
            try {
                URI uri = URI.create(host);
                useSsl = host.startsWith("rediss://");
                host = uri.getHost();
                if (uri.getPort() != -1) {
                    port = uri.getPort();
                }
                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }
                log.info("Parsed cloud Redis URI successfully. Target host: {}, port: {}, SSL: {}", host, port, useSsl);
            } catch (Exception e) {
                log.error("Failed to parse Cloud Redis URI: {}. Falling back to standard properties.", host, e);
            }
        }

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);

        if (username != null && !username.isBlank()) {
            config.setUsername(username);
        }

        if (password != null && !password.isBlank()) {
            config.setPassword(password);
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientConfig =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(3))
                        .shutdownTimeout(Duration.ofSeconds(2));

        if (useSsl) {
            clientConfig.useSsl();
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig.build());
        factory.setValidateConnection(false); // Prevent blocking startup if Redis is unreachable
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CustomCacheErrorHandler();
    }
}