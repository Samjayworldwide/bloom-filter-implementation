package com.samjay.bloom_filter_project.config;

import com.samjay.bloom_filter_project.bloom.RedisBloomFilter;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.*;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static com.samjay.bloom_filter_project.utilities.Extensions.BLOOM_FILTER_KEY;

@SuppressWarnings("ALL")
@Configuration
public class RedisConfiguration {

    @Value("${redis.pool.host}")
    private String redisHost;

    @Value("${redis.pool.port}")
    private int redisPort;

    @Value("${redis.pool.max-active}")
    private int maxActive;

    @Value("${redis.pool.max-idle}")
    private int maxIdle;

    @Value("${redis.pool.min-idle}")
    private int minIdle;

    @Value("${redis.pool.max-wait-millis}")
    public long maxWaitMillis;

    @Value("${redis.pool.max-total}")
    public int maxTotal;

    @Value("${redis.pool.max-wait}")
    private Duration maxWait;

    @Bean
    public GenericObjectPoolConfig<StatefulConnection<?, ?>> redisPoolConfig() {

        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();

        poolConfig.setMaxTotal(maxActive);

        poolConfig.setMaxIdle(maxIdle);

        poolConfig.setMinIdle(minIdle);

        poolConfig.setMaxWait(maxWait);

        poolConfig.setTestOnBorrow(true);

        poolConfig.setTestWhileIdle(true);

        return poolConfig;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig) {

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration
                .builder()
                .poolConfig(poolConfig)
                .commandTimeout(Duration.ofSeconds(5))
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        RedisSerializer<String> keySerializer = RedisSerializer.string();

        GenericJacksonJsonRedisSerializer valueSerializer = new GenericJacksonJsonRedisSerializer(redisObjectMapper);

        template.setKeySerializer(keySerializer);

        template.setValueSerializer(valueSerializer);

        template.setHashKeySerializer(keySerializer);

        template.setHashValueSerializer(valueSerializer);

        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {

        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisClient redisClient() {

        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();

        poolConfig.setMaxTotal(maxTotal);

        poolConfig.setMaxIdle(maxIdle);

        poolConfig.setMinIdle(minIdle);

        poolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));

        poolConfig.setTestOnBorrow(true);

        poolConfig.setTestWhileIdle(true);

        JedisClientConfig clientConfig = DefaultJedisClientConfig
                .builder()
                .connectionTimeoutMillis(5000)
                .socketTimeoutMillis(5000)
                .build();

        return RedisClient
                .builder()
                .hostAndPort(redisHost, redisPort)
                .clientConfig(clientConfig)
                .poolConfig(poolConfig)
                .build();
    }

    @Bean
    public RedisBloomFilter redisBloomFilter(RedisClient redisClient) {

        return new RedisBloomFilter(
                redisClient,
                BLOOM_FILTER_KEY,
                0.01,
                1_000_000L,
                2
        );

    }
}