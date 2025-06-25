package com.faud.frauddetection.integration;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.repository.FraudRuleRepository;
import com.faud.frauddetection.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Comprehensive fraud detection simulation tests
 * Simulates various fraudulent transaction scenarios to verify detection accuracy
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "fraud.detection.enabled=true",
    "fraud.detection.fraud-threshold=0.3"
})
class FraudDetectionSimulationTest {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @MockBean
    private FraudRuleRepository fraudRuleRepository;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ZSetOperations<String, String> zSetOperations;

    private List<FraudRule> testRules;

    @BeforeEach
    void setUp() {
        // Mock Redis operations
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // Set up test fraud rules
        setupTestRules();
        when(fraudRuleRepository.findAllEnabled()).thenReturn(testRules);
    }

    @Test
    void testHighAmountFraudDetection() {
        // Given - High amount transaction that should trigger fraud detection
        Transaction highAmountTransaction = createTransaction(
            "TXN_HIGH_AMOUNT_001",
            "USER_001",
            new BigDecimal("25000"), // Above 10k threshold
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(highAmountTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.3);
        assertThat(result.getTransactionId()).isEqualTo("TXN_HIGH_AMOUNT_001");
        assertThat(result.getReason()).contains("Transaction amount");
    }

    @Test
    void testNormalAmountTransactionIsNotFlagged() {
        // Given - Normal amount transaction
        Transaction normalTransaction = createTransaction(
            "TXN_NORMAL_001",
            "USER_002",
            new BigDecimal("500"), // Below threshold
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(normalTransaction);

        // Then
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getRiskScore()).isLessThan(0.3);
        assertThat(result.getTransactionId()).isEqualTo("TXN_NORMAL_001");
    }

    @Test
    void testSuspiciousTimeTransactionDetection() {
        // Given - Transaction at suspicious time (2 AM)
        Transaction nightTransaction = createTransaction(
            "TXN_NIGHT_001",
            "USER_003",
            new BigDecimal("5000"),
            "192.168.1.50",
            LocalDateTime.now().with(LocalTime.of(2, 0))
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(nightTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.0);
        assertThat(result.getTransactionId()).isEqualTo("TXN_NIGHT_001");
    }

    @Test
    void testBlacklistedIpDetection() {
        // Given - Transaction from blacklisted IP
        Transaction blacklistedIpTransaction = createTransaction(
            "TXN_BLACKLIST_001",
            "USER_004",
            new BigDecimal("1000"),
            "192.168.1.100", // Blacklisted IP
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(blacklistedIpTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.3);
        assertThat(result.getTransactionId()).isEqualTo("TXN_BLACKLIST_001");
    }

    @Test
    void testHighFrequencyTransactionDetection() {
        // Given - Mock high frequency for user
        when(zSetOperations.zCard(anyString())).thenReturn(10L); // Above threshold of 5

        Transaction frequentTransaction = createTransaction(
            "TXN_FREQUENT_001",
            "USER_005",
            new BigDecimal("1000"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(frequentTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.3);
        assertThat(result.getTransactionId()).isEqualTo("TXN_FREQUENT_001");
    }

    @Test
    void testMultipleFraudRulesTriggered() {
        // Given - Transaction that triggers multiple rules
        when(zSetOperations.zCard(anyString())).thenReturn(8L); // High frequency

        Transaction multiRuleTransaction = createTransaction(
            "TXN_MULTI_001",
            "USER_006",
            new BigDecimal("20000"), // High amount
            "192.168.1.100", // Blacklisted IP
            LocalDateTime.now().with(LocalTime.of(3, 0)) // Suspicious time
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(multiRuleTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.5); // Should be high due to multiple rules
        assertThat(result.getTransactionId()).isEqualTo("TXN_MULTI_001");
    }

    @Test
    void testConcurrentFraudDetection() throws Exception {
        // Given - Multiple transactions processed concurrently
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        List<CompletableFuture<FraudDetectionResult>> futures = IntStream.range(0, 20)
            .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                Transaction transaction = createTransaction(
                    "TXN_CONCURRENT_" + i,
                    "USER_" + (i % 5), // 5 different users
                    new BigDecimal(String.valueOf(5000 + (i * 1000))), // Varying amounts
                    "192.168.1." + (50 + (i % 10)), // Different IPs
                    LocalDateTime.now()
                );
                return fraudDetectionService.detectFraud(transaction);
            }, executor))
            .toList();

        // When
        List<FraudDetectionResult> results = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        // Then
        assertThat(results).hasSize(20);
        
        // Verify all transactions were processed
        results.forEach(result -> {
            assertThat(result.getTransactionId()).isNotNull();
            assertThat(result.getRiskScore()).isGreaterThanOrEqualTo(0.0);
        });

        // Verify some transactions were flagged as fraud (high amounts)
        long fraudCount = results.stream()
            .filter(FraudDetectionResult::isFraud)
            .count();
        
        assertThat(fraudCount).isGreaterThan(0);
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void testFraudDetectionPerformance() {
        // Given - Large number of transactions for performance testing
        int numberOfTransactions = 100;
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < numberOfTransactions; i++) {
            Transaction transaction = createTransaction(
                "TXN_PERF_" + i,
                "USER_PERF_" + (i % 10),
                new BigDecimal(String.valueOf(1000 + (i * 100))),
                "192.168.1.50",
                LocalDateTime.now()
            );
            fraudDetectionService.detectFraud(transaction);
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Then - Performance assertions
        assertThat(totalTime).isLessThan(10000); // Should complete within 10 seconds
        
        double averageTimePerTransaction = (double) totalTime / numberOfTransactions;
        assertThat(averageTimePerTransaction).isLessThan(100); // Less than 100ms per transaction
    }

    @Test
    void testEdgeCaseTransactions() {
        // Test edge cases that might cause issues

        // 1. Zero amount transaction
        Transaction zeroAmountTransaction = createTransaction(
            "TXN_ZERO_001",
            "USER_EDGE_001",
            BigDecimal.ZERO,
            "192.168.1.50",
            LocalDateTime.now()
        );

        FraudDetectionResult zeroResult = fraudDetectionService.detectFraud(zeroAmountTransaction);
        assertThat(zeroResult.isFraud()).isFalse();

        // 2. Very large amount transaction
        Transaction largeAmountTransaction = createTransaction(
            "TXN_LARGE_001",
            "USER_EDGE_002",
            new BigDecimal("999999999.99"),
            "192.168.1.50",
            LocalDateTime.now()
        );

        FraudDetectionResult largeResult = fraudDetectionService.detectFraud(largeAmountTransaction);
        assertThat(largeResult.isFraud()).isTrue();

        // 3. Transaction with null IP (should handle gracefully)
        Transaction nullIpTransaction = createTransaction(
            "TXN_NULL_IP_001",
            "USER_EDGE_003",
            new BigDecimal("5000"),
            null,
            LocalDateTime.now()
        );

        FraudDetectionResult nullIpResult = fraudDetectionService.detectFraud(nullIpTransaction);
        assertThat(nullIpResult).isNotNull(); // Should not crash
    }

    @Test
    void testFraudDetectionWithDisabledRules() {
        // Given - All rules disabled
        testRules.forEach(rule -> rule.setEnabled(false));
        when(fraudRuleRepository.findAllEnabled()).thenReturn(Arrays.asList()); // Empty list

        Transaction transaction = createTransaction(
            "TXN_NO_RULES_001",
            "USER_NO_RULES",
            new BigDecimal("50000"), // Would normally trigger fraud
            "192.168.1.100",
            LocalDateTime.now()
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);

        // Then
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
    }

    @Test
    void testComplexFraudScenario() {
        // Given - Complex scenario: User making multiple high-value transactions in short time
        String userId = "USER_COMPLEX_001";
        when(zSetOperations.zCard(anyString())).thenReturn(15L); // Very high frequency

        // First transaction - High amount at suspicious time from blacklisted IP
        Transaction complexTransaction = createTransaction(
            "TXN_COMPLEX_001",
            userId,
            new BigDecimal("30000"),
            "192.168.1.100",
            LocalDateTime.now().with(LocalTime.of(1, 30))
        );

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(complexTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.8); // Should be very high
        assertThat(result.getTransactionId()).isEqualTo("TXN_COMPLEX_001");
        
        // Verify processing time is reasonable even for complex scenarios
        assertThat(result.getProcessingTime()).isLessThan(1000L); // Less than 1 second
    }

    private void setupTestRules() {
        FraudRule amountRule = FraudRule.builder()
            .id(1L)
            .ruleName("HIGH_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .thresholdValue(new BigDecimal("10000"))
            .riskWeight(new BigDecimal("0.3"))
            .enabled(true)
            .build();

        FraudRule frequencyRule = FraudRule.builder()
            .id(2L)
            .ruleName("HIGH_FREQUENCY_RULE")
            .ruleType("FREQUENCY")
            .thresholdValue(new BigDecimal("5"))
            .riskWeight(new BigDecimal("0.25"))
            .enabled(true)
            .build();

        FraudRule timeRule = FraudRule.builder()
            .id(3L)
            .ruleName("SUSPICIOUS_TIME_RULE")
            .ruleType("TIME_OF_DAY")
            .thresholdValue(BigDecimal.ZERO)
            .riskWeight(new BigDecimal("0.2"))
            .enabled(true)
            .build();

        FraudRule ipRule = FraudRule.builder()
            .id(4L)
            .ruleName("IP_BLACKLIST_RULE")
            .ruleType("IP_BLACKLIST")
            .thresholdValue(BigDecimal.ZERO)
            .riskWeight(new BigDecimal("0.4"))
            .enabled(true)
            .build();

        testRules = Arrays.asList(amountRule, frequencyRule, timeRule, ipRule);
    }

    private Transaction createTransaction(String transactionId, String userId, BigDecimal amount, 
                                       String ipAddress, LocalDateTime timestamp) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setCurrency("USD");
        transaction.setIpAddress(ipAddress);
        transaction.setTimestamp(timestamp);
        return transaction;
    }
} 