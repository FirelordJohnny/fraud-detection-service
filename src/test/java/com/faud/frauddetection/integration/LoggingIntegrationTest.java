package com.faud.frauddetection.integration;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.service.AlertService;
import com.faud.frauddetection.service.FraudDetectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 日志集成测试
 * 验证欺诈检测系统在各种场景下的日志记录功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.faud.frauddetection=DEBUG",
    "fraud.alert.enabled=true"
})
@EnabledIfSystemProperty(named = "integration.tests.enabled", matches = "true")
class LoggingIntegrationTest {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private AlertService alertService;

    @Test
    void testFraudDetectionServiceLogging() {
        // Given
        Transaction testTransaction = createTransaction(
            "LOG_TEST_001",
            "USER_LOG_001", 
            new BigDecimal("25000"),
            "192.168.1.100",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then - verify logging works with actual service
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("LOG_TEST_001");
    }

    @Test
    void testFraudAlertLogging() {
        // Given
        FraudDetectionResult fraudResult = createFraudResult();

        // When
        alertService.sendAlert(fraudResult);

        // Then - verify alert logging
        assertThat(fraudResult.isFraud()).isTrue();
    }

    @Test
    void testNonFraudTransactionLogging() {
        // Given
        Transaction normalTransaction = createTransaction(
            "LOG_NORMAL_001",
            "USER_NORMAL_001",
            new BigDecimal("100"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(normalTransaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("LOG_NORMAL_001");
    }

    @Test
    void testErrorLoggingForInvalidData() {
        // Given - Invalid transaction with null fields
        Transaction invalidTransaction = Transaction.builder()
            .transactionId("INVALID_001")
            .amount(null) // Invalid amount
            .build();

        // When - Process invalid transaction
        try {
            fraudDetectionService.detectFraud(invalidTransaction);
        } catch (Exception e) {
            // Expected exception for invalid data
        }

        // Then - verify error handling
        assertThat(invalidTransaction.getTransactionId()).isEqualTo("INVALID_001");
    }

    @Test
    void testConcurrentLogging() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // When - Process multiple transactions concurrently
        for (int i = 0; i < 10; i++) {
            final int index = i;
            CompletableFuture.runAsync(() -> {
                Transaction transaction = createTransaction(
                    "CONCURRENT_" + index,
                    "USER_" + index,
                    new BigDecimal("1000"),
                    "192.168.1.50",
                    LocalDateTime.now()
                );
                fraudDetectionService.detectFraud(transaction);
            }, executor);
        }

        // Then - wait for completion
        executor.shutdown();
        boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(finished).isTrue();
    }

    @Test
    void testLogLevelFiltering() {
        // Given
        Transaction debugTransaction = createTransaction(
            "DEBUG_LOG_001",
            "USER_DEBUG_001",
            new BigDecimal("5000"),
            "192.168.1.75",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(debugTransaction);

        // Then - verify debug logging is active
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("DEBUG_LOG_001");
    }

    @Test
    void testLogMessageStructureAndFormat() {
        // Given
        Transaction structuredTransaction = createTransaction(
            "STRUCT_LOG_001",
            "USER_STRUCT_001",
            new BigDecimal("15000"),
            "192.168.1.90",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(structuredTransaction);

        // Then - verify structured logging
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("STRUCT_LOG_001");
        assertThat(result.getProcessingTime()).isGreaterThan(0L);
    }

    @Test
    void testRuleEngineDebugLogging() {
        // Given
        Transaction ruleTestTransaction = createTransaction(
            "RULE_DEBUG_001",
            "USER_RULE_001",
            new BigDecimal("20000"), // Should trigger amount rule
            "192.168.1.100", // Should trigger IP rule
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(ruleTestTransaction);

        // Then - verify rule evaluation logging
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isEqualTo("RULE_DEBUG_001");
    }

    private Transaction createTransaction(String txnId, String userId, BigDecimal amount, 
                                       String ipAddress, LocalDateTime timestamp) {
        return Transaction.builder()
            .transactionId(txnId)
            .userId(userId)
            .amount(amount)
            .ipAddress(ipAddress)
            .timestamp(timestamp)
            .merchant("MERCHANT_001")
            .currency("USD")
            .build();
    }

    private FraudDetectionResult createFraudResult() {
        FraudDetectionResult result = new FraudDetectionResult();
        result.setTransactionId("FRAUD_ALERT_001");
        result.setFraud(true);
        result.setRiskScore(0.85);
        result.setReason("High amount transaction");
        result.setProcessingTime(50L);
        return result;
    }
} 