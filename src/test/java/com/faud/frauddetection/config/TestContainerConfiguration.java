package com.faud.frauddetection.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.MySQLContainer;
import redis.embedded.RedisServer;

import jakarta.annotation.PreDestroy;

/**
 * Test configuration for managing test containers and embedded services
 */
@TestConfiguration
@Profile("test")
@Slf4j
public class TestContainerConfiguration {

    private static final MySQLContainer<?> mysqlContainer;
    private static RedisServer redisServer;

    static {
        // MySQL Container setup
        mysqlContainer = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("fraud_detection_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        
        if (!mysqlContainer.isRunning()) {
            mysqlContainer.start();
            log.info("MySQL TestContainer started on {}:{}", 
                mysqlContainer.getHost(), mysqlContainer.getFirstMappedPort());
        }

        // Embedded Redis setup
        try {
            redisServer = new RedisServer(6370); // Use different port to avoid conflicts
            redisServer.start();
            log.info("Embedded Redis started on port 6370");
        } catch (Exception e) {
            log.error("Failed to start embedded Redis", e);
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory testRedisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory("localhost", 6370);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    @Primary
    public StringRedisTemplate testRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(testRedisConnectionFactory());
        template.afterPropertiesSet();
        return template;
    }

    public static String getMysqlJdbcUrl() {
        return mysqlContainer.getJdbcUrl();
    }

    public static String getMysqlUsername() {
        return mysqlContainer.getUsername();
    }

    public static String getMysqlPassword() {
        return mysqlContainer.getPassword();
    }

    @PreDestroy
    public void cleanup() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
            log.info("Embedded Redis stopped");
        }
        
        if (mysqlContainer != null && mysqlContainer.isRunning()) {
            mysqlContainer.stop();
            log.info("MySQL TestContainer stopped");
        }
    }
} 