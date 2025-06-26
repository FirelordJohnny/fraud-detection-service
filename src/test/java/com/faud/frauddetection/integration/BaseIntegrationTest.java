package com.faud.frauddetection.integration;

import com.faud.frauddetection.config.TestContainerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with TestContainers support
 * Provides MySQL, Redis, and Kafka test infrastructure
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration")
@Import(TestContainerConfiguration.class)
@EmbeddedKafka(
    partitions = 1,
    topics = {"transactions", "fraud-alerts"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "fraud.alert.enabled=true",
    "fraud.alert.topic=fraud-alerts"
})
@Slf4j
public abstract class BaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("fraud_detection_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL properties
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        
        // Redis properties (using embedded Redis on port 6370)
        registry.add("redis.host", () -> "localhost");
        registry.add("redis.port", () -> "6370");
        
        log.info("Test containers configured:");
        log.info("MySQL: {}", mysqlContainer.getJdbcUrl());
        log.info("Redis: localhost:6370");
        log.info("Kafka: embedded broker");
    }

    @BeforeAll
    static void beforeAll() {
        // Ensure containers are started
        if (!mysqlContainer.isRunning()) {
            mysqlContainer.start();
        }
        
        log.info("Integration test environment ready");
        log.info("MySQL container: {}:{}", mysqlContainer.getHost(), mysqlContainer.getFirstMappedPort());
    }

    @AfterAll
    static void afterAll() {
        // Ensure containers are properly stopped
        if (mysqlContainer != null && mysqlContainer.isRunning()) {
            mysqlContainer.stop();
            log.info("MySQL container stopped");
        }
    }
} 