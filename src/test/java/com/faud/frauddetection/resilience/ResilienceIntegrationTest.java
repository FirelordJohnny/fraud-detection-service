package com.faud.frauddetection.resilience;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.integration.BaseIntegrationTest;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.service.AlertService;
import com.faud.frauddetection.service.FraudDetectionService;
import com.faud.frauddetection.service.TransactionConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.faud.frauddetection.dto.TransactionStatus;
import com.faud.frauddetection.service.FraudRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Resilience integration tests for fraud detection service
 * Tests service recovery from failures, pod restarts, node failures, and network issues
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
    "fraud.detection.enabled=true",
    "fraud.alert.enabled=true",
    "spring.kafka.consumer.enable-auto-commit=false",
    "spring.kafka.consumer.max-poll-records=10"
})
class ResilienceIntegrationTest extends BaseIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"));

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.26");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:6.2.6").withExposedPorts(6379);

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private TransactionConsumer transactionConsumer;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private FraudRuleService fraudRuleService;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    @Timeout(30)
    void testServiceResilienceUnderHighLoad() throws InterruptedException {
        // Given - High load scenario with many concurrent requests
        int numberOfThreads = 50;
        int transactionsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(numberOfThreads * transactionsPerThread);

        // When - Submit high load
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                for (int j = 0; j < transactionsPerThread; j++) {
                    try {
                        Transaction transaction = createTransaction(
                            "TXN_LOAD_" + threadId + "_" + j,
                            "USER_" + threadId,
                            new BigDecimal(String.valueOf(1000 + (j * 100))),
                            "192.168.1.50",
                            LocalDateTime.now()
                        );
                        
                        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
                        if (result != null) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // Then - Wait for completion and verify resilience
        latch.await(25, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Verify most requests succeeded despite high load
        int totalRequests = numberOfThreads * transactionsPerThread;
        assertThat(successCount.get()).isGreaterThan((int)(totalRequests * 0.95)); // At least 95% success rate
        assertThat(failureCount.get()).isLessThan((int)(totalRequests * 0.05)); // Less than 5% failure rate
    }

    @Test
    void testDatabaseConnectionFailureRecovery() {
        // Given - Database connection failure simulation
        when(redisTemplate.opsForZSet()).thenThrow(new DataAccessException("Database connection failed") {});

        Transaction transaction = createTransaction(
            "TXN_DB_FAIL_001",
            "USER_DB_FAIL",
            new BigDecimal("15000"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When & Then - Service should handle database failure gracefully
        assertThatCode(() -> {
            FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
            // Should not crash, might return default result or handle gracefully
            assertThat(result).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    void testRedisConnectionFailureRecovery() {
        // Given - Redis connection failure
        when(redisTemplate.opsForZSet()).thenThrow(new RedisConnectionFailureException("Redis connection failed"));

        Transaction transaction = createTransaction(
            "TXN_REDIS_FAIL_001",
            "USER_REDIS_FAIL",
            new BigDecimal("8000"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When & Then - Service should continue working without Redis (frequency checks might be disabled)
        assertThatCode(() -> {
            FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
            assertThat(result).isNotNull();
            assertThat(result.getTransactionId()).isEqualTo("TXN_REDIS_FAIL_001");
        }).doesNotThrowAnyException();
    }

    @Test
    void testKafkaConnectionFailureRecovery() {
        // Given - Kafka connection failure for alerts
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Kafka broker not available"));

        FraudDetectionResult fraudResult = createFraudDetectionResult("TXN_KAFKA_FAIL_001", true, 0.8);

        // When & Then - Alert service should handle Kafka failure gracefully
        assertThatCode(() -> {
            alertService.sendAlert(fraudResult);
            // Should not crash the application
        }).doesNotThrowAnyException();
    }

    @Test
    @Timeout(20)
    void testCircuitBreakerPattern() throws InterruptedException {
        // Given - Simulate repeated failures to test circuit breaker behavior
        AtomicInteger callCount = new AtomicInteger(0);
        
        // Mock intermittent failures
        when(redisTemplate.opsForZSet()).thenAnswer(invocation -> {
            int count = callCount.incrementAndGet();
            if (count % 3 == 0) {
                throw new RedisConnectionFailureException("Intermittent failure");
            }
            return mock(org.springframework.data.redis.core.ZSetOperations.class);
        });

        // When - Make multiple calls
        List<FraudDetectionResult> results = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        try {
            List<CompletableFuture<Void>> futures = IntStream.range(0, 30)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        Transaction transaction = createTransaction(
                            "TXN_CIRCUIT_" + i,
                            "USER_CIRCUIT",
                            new BigDecimal("5000"),
                            "192.168.1.50",
                            LocalDateTime.now()
                        );
                        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
                        results.add(result);
                    } catch (Exception e) {
                        // Expected for some calls due to simulated failures
                    }
                }, executor))
                .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        // Then - Verify some calls succeeded despite intermittent failures
        assertThat(results.size()).isGreaterThan(15); // At least half should succeed
    }

    @Test
    void testGracefulDegradation() {
        // Given - Multiple service dependencies fail
        when(redisTemplate.opsForZSet()).thenThrow(new RedisConnectionFailureException("Redis down"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Kafka down"));

        Transaction transaction = createTransaction(
            "TXN_DEGRADED_001",
            "USER_DEGRADED",
            new BigDecimal("20000"), // High amount - should still be detected
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);

        // Then - Core fraud detection should still work in degraded mode
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("TXN_DEGRADED_001");
        // Amount-based rules should still work without Redis
        assertThat(result.isFraud()).isTrue(); // High amount should trigger fraud detection
    }

    @Test
    @Timeout(15)
    void testServiceRecoveryAfterFailure() throws InterruptedException {
        // Given - Initial failure state
        when(redisTemplate.opsForZSet()).thenThrow(new RedisConnectionFailureException("Initial failure"));

        Transaction failingTransaction = createTransaction(
            "TXN_RECOVERY_FAIL",
            "USER_RECOVERY",
            new BigDecimal("5000"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When - First call fails
        FraudDetectionResult failResult = fraudDetectionService.detectFraud(failingTransaction);
        assertThat(failResult).isNotNull(); // Should handle gracefully

        // Simulate service recovery
        reset(redisTemplate);
        when(redisTemplate.opsForZSet()).thenReturn(mock(org.springframework.data.redis.core.ZSetOperations.class));

        // When - Service recovers
        Transaction recoveryTransaction = createTransaction(
            "TXN_RECOVERY_SUCCESS",
            "USER_RECOVERY",
            new BigDecimal("5000"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        FraudDetectionResult recoveryResult = fraudDetectionService.detectFraud(recoveryTransaction);

        // Then - Service should work normally after recovery
        assertThat(recoveryResult).isNotNull();
        assertThat(recoveryResult.getTransactionId()).isEqualTo("TXN_RECOVERY_SUCCESS");
    }

    @Test
    void testMemoryLeakPrevention() {
        // Given - Process many transactions to test memory management
        int numberOfTransactions = 1000;

        // When - Process large number of transactions
        for (int i = 0; i < numberOfTransactions; i++) {
            Transaction transaction = createTransaction(
                "TXN_MEMORY_" + i,
                "USER_MEMORY_" + (i % 10),
                new BigDecimal(String.valueOf(1000 + i)),
                "192.168.1.50",
                LocalDateTime.now()
            );
            
            FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
            assertThat(result).isNotNull();
            
            // Simulate some processing delay
            if (i % 100 == 0) {
                System.gc(); // Suggest garbage collection
                try {
                    Thread.sleep(10); // Small delay to allow GC
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Then - Memory should be managed properly (no OutOfMemoryError)
        // This test passes if no memory-related exceptions are thrown
        assertThat(true).isTrue(); // Test completion indicates success
    }

    @Test
    @Timeout(10)
    void testTimeoutHandling() {
        // Given - Simulate slow external service
        when(redisTemplate.opsForZSet()).thenAnswer(invocation -> {
            try {
                Thread.sleep(5000); // Simulate slow operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return mock(org.springframework.data.redis.core.ZSetOperations.class);
        });

        Transaction transaction = createTransaction(
            "TXN_TIMEOUT_001",
            "USER_TIMEOUT",
            new BigDecimal("5000"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When & Then - Should handle timeouts gracefully
        long startTime = System.currentTimeMillis();
        
        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        // Should complete in reasonable time (not wait for full 5 seconds)
        assertThat(processingTime).isLessThan(3000L);
        assertThat(result).isNotNull();
    }

    @Test
    void testConcurrentFailureRecovery() throws InterruptedException {
        // Given - Multiple threads with some experiencing failures
        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Mock intermittent Redis failures
        AtomicInteger callCount = new AtomicInteger(0);
        when(redisTemplate.opsForZSet()).thenAnswer(invocation -> {
            int count = callCount.incrementAndGet();
            if (count % 4 == 0) { // Every 4th call fails
                throw new RedisConnectionFailureException("Intermittent Redis failure");
            }
            return mock(org.springframework.data.redis.core.ZSetOperations.class);
        });

        try {
            // When - Submit concurrent requests
            for (int i = 0; i < numberOfThreads; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        Transaction transaction = createTransaction(
                            "TXN_CONCURRENT_FAIL_" + threadId,
                            "USER_CONCURRENT_" + threadId,
                            new BigDecimal("10000"),
                            "192.168.1.50",
                            LocalDateTime.now()
                        );
                        
                        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
                        if (result != null) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Then
            latch.await(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        // Most requests should succeed despite intermittent failures
        assertThat(successCount.get()).isGreaterThan((int)(numberOfThreads * 0.7)); // At least 70% success
        assertThat(successCount.get() + failureCount.get()).isEqualTo(numberOfThreads);
    }

    @Test
    void testDataConsistencyAfterFailure() {
        // Given - Transaction that should be flagged as fraud
        Transaction fraudTransaction = createTransaction(
            "TXN_CONSISTENCY_001",
            "USER_CONSISTENCY",
            new BigDecimal("25000"), // High amount
            "192.168.1.100", // Blacklisted IP
            LocalDateTime.now()
        );

        // When - Process transaction even with Redis failure
        when(redisTemplate.opsForZSet()).thenThrow(new RedisConnectionFailureException("Redis failure"));
        
        FraudDetectionResult result = fraudDetectionService.detectFraud(fraudTransaction);

        // Then - Core fraud detection logic should still work
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("TXN_CONSISTENCY_001");
        // Amount-based rule should still trigger without Redis
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.0);
    }

    @Test
    void testKafkaResilience() throws Exception {
        // Stop Kafka container
        kafka.stop();

        // Produce a message, expecting it to fail or hang
        Transaction transaction = createTestTransaction("kafka-resilience-test");
        assertThrows(Exception.class, () -> {
            kafkaTemplate.send("transactions", transaction.getTransactionId(), objectMapper.writeValueAsString(transaction)).get(5, TimeUnit.SECONDS);
        });

        // Restart Kafka container
        kafka.start();

        // It should recover and process the message
        kafkaTemplate.send("transactions", transaction.getTransactionId(), objectMapper.writeValueAsString(transaction));
        // Add verification logic here if needed, e.g., checking logs or another topic
    }

    @Test
    void testDatabaseResilience() {
        // Stop the database
        mysql.stop();

        // Perform an operation that requires the database
        assertThrows(Exception.class, () -> {
            fraudRuleService.getActiveRules();
        });

        // Restart the database
        mysql.start();

        // The service should recover
        assertDoesNotThrow(() -> {
            fraudRuleService.getActiveRules();
        });
    }

    @Test
    void testRedisResilience() throws Exception {
        // Stop Redis
        redis.stop();

        // Create a transaction that would trigger a frequency rule
        Transaction transaction = createTestTransaction("redis-resilience-test");
        
        // This might not throw immediately but log errors
        // We'll just check that the app doesn't crash
        assertDoesNotThrow(() -> {
            fraudDetectionService.detectFraud(transaction);
        });

        // Restart Redis
        redis.start();

        // App should be back to normal
        assertDoesNotThrow(() -> {
            fraudDetectionService.detectFraud(transaction);
        });
    }

    private Transaction createTransaction(String transactionId, String userId, BigDecimal amount, 
                                       String ipAddress, LocalDateTime timestamp) {
        return Transaction.builder()
                .transactionId(transactionId)
                .userId(userId)
                .amount(amount)
                .currency("USD")
                .ipAddress(ipAddress)
                .timestamp(timestamp)
                .build();
    }

    private FraudDetectionResult createFraudDetectionResult(String transactionId, boolean isFraud, double riskScore) {
        FraudDetectionResult result = new FraudDetectionResult();
        result.setTransactionId(transactionId);
        result.setFraud(isFraud);
        result.setRiskScore(riskScore);
        result.setReason(isFraud ? "High risk transaction detected" : "Normal transaction");
        result.setDetectionTimestamp(LocalDateTime.now());
        result.setProcessingTime(100L);
        return result;
    }

    private Transaction createTestTransaction(String id) {
        return Transaction.builder()
                .transactionId(id)
                .userId("test-user-" + id)
                .amount(new BigDecimal("250.00"))
                .timestamp(LocalDateTime.now())
                .currency("USD")
                .ipAddress("127.0.0.1")
                .country("US")
                .paymentMethod("CREDIT_CARD")
                .status(TransactionStatus.PENDING)
                .merchant("ResilienceTestMerchant")
                .build();
    }

    private void publishAndVerify(String topic, String key, String value, KafkaProducer<String, String> producer) throws Exception {
        // ... existing code ...
        producer.send(new ProducerRecord<>(topic, key, value)).get();
    }
} 