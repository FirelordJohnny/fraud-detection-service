package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.service.evaluator.impl.DynamicRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Test cases for Dynamic Rule Engine
 */
@ExtendWith(MockitoExtension.class)
class DynamicRuleEngineTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private DynamicRuleEngine ruleEngine;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        // Use lenient to avoid unnecessary stubbing errors
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        ruleEngine = new DynamicRuleEngine(redisTemplate);
        
        testTransaction = new Transaction();
        testTransaction.setTransactionId("TXN_001");
        testTransaction.setUserId("USER_123");
        testTransaction.setAmount(BigDecimal.valueOf(15000));
        testTransaction.setCurrency("USD");
        testTransaction.setIpAddress("192.168.1.100");
        testTransaction.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testSupports_AllSupportedTypes_ShouldReturnTrue() {
        assertThat(ruleEngine.supports("AMOUNT")).isTrue();
        assertThat(ruleEngine.supports("FREQUENCY")).isTrue();
        assertThat(ruleEngine.supports("TIME_OF_DAY")).isTrue();
        assertThat(ruleEngine.supports("IP_BLACKLIST")).isTrue();
        assertThat(ruleEngine.supports("CUSTOM")).isTrue();
    }

    @Test
    void testSupports_UnsupportedType_ShouldReturnFalse() {
        assertThat(ruleEngine.supports("UNKNOWN_TYPE")).isFalse();
        assertThat(ruleEngine.supports("INVALID")).isFalse();
        assertThat(ruleEngine.supports("")).isFalse();
        // Note: null check is handled in supports method
    }

    @Test
    void testSupports_NullRuleType_ShouldReturnFalse() {
        assertThat(ruleEngine.supports(null)).isFalse();
    }

    @Test
    void testEvaluateAmountRule_AmountExceedsThreshold_ShouldTrigger() {
        // Given
        FraudRule amountRule = FraudRule.builder()
            .ruleName("HIGH_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .thresholdValue(BigDecimal.valueOf(10000))
            .riskWeight(BigDecimal.valueOf(0.3))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(amountRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
        assertThat(result.getRuleName()).isEqualTo("HIGH_AMOUNT_RULE");
        assertThat(result.getReason()).contains("Transaction amount");
        assertThat(result.getReason()).contains("exceeds threshold");
    }

    @Test
    void testEvaluateAmountRule_AmountBelowThreshold_ShouldNotTrigger() {
        // Given
        FraudRule amountRule = FraudRule.builder()
            .ruleName("HIGH_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .thresholdValue(BigDecimal.valueOf(20000))
            .riskWeight(BigDecimal.valueOf(0.3))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(amountRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getRuleName()).isEqualTo("HIGH_AMOUNT_RULE");
        assertThat(result.getReason()).contains("Transaction amount is normal");
    }

    @Test
    void testEvaluateFrequencyRule_ExceedsFrequencyLimit_ShouldTrigger() {
        // Given
        FraudRule frequencyRule = FraudRule.builder()
            .ruleName("HIGH_FREQUENCY_RULE")
            .ruleType("FREQUENCY")
            .thresholdValue(BigDecimal.valueOf(5))
            .riskWeight(BigDecimal.valueOf(0.25))
            .build();

        // Mock Redis to return high transaction count
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(6L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(frequencyRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
        assertThat(result.getReason()).contains("User has 6 transactions in 1 hour");
        assertThat(result.getReason()).contains("exceeds threshold 5");
    }

    @Test
    void testEvaluateFrequencyRule_BelowFrequencyLimit_ShouldNotTrigger() {
        // Given
        FraudRule frequencyRule = FraudRule.builder()
            .ruleName("HIGH_FREQUENCY_RULE")
            .ruleType("FREQUENCY")
            .thresholdValue(BigDecimal.valueOf(5))
            .riskWeight(BigDecimal.valueOf(0.25))
            .build();

        // Mock Redis to return low transaction count
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(frequencyRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Transaction frequency is normal");
    }

    @Test
    void testEvaluateTimeOfDayRule_SuspiciousHours_ShouldTrigger() {
        // Given - transaction at 2 AM (suspicious hour)
        Transaction nightTransaction = new Transaction();
        nightTransaction.setTransactionId("TXN_NIGHT");
        nightTransaction.setUserId("USER_123");
        nightTransaction.setAmount(BigDecimal.valueOf(5000));
        nightTransaction.setTimestamp(LocalDateTime.now().with(LocalTime.of(2, 0)));

        FraudRule timeRule = FraudRule.builder()
            .ruleName("NIGHT_TIME_RULE")
            .ruleType("TIME_OF_DAY")
            .riskWeight(BigDecimal.valueOf(0.2))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(timeRule, nightTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(0.3);
        assertThat(result.getReason()).contains("is suspicious period");
    }

    @Test
    void testEvaluateTimeOfDayRule_NormalHours_ShouldNotTrigger() {
        // Given - transaction at 2 PM (normal hour)
        Transaction dayTransaction = new Transaction();
        dayTransaction.setTransactionId("TXN_DAY");
        dayTransaction.setUserId("USER_123");
        dayTransaction.setAmount(BigDecimal.valueOf(5000));
        dayTransaction.setTimestamp(LocalDateTime.now().with(LocalTime.of(14, 0)));

        FraudRule timeRule = FraudRule.builder()
            .ruleName("NIGHT_TIME_RULE")
            .ruleType("TIME_OF_DAY")
            .riskWeight(BigDecimal.valueOf(0.2))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(timeRule, dayTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Transaction time is normal");
    }

    @Test
    void testEvaluateIpBlacklistRule_BlacklistedIp_ShouldTrigger() {
        // Given
        Transaction blacklistedIpTransaction = new Transaction();
        blacklistedIpTransaction.setTransactionId("TXN_BLOCKED");
        blacklistedIpTransaction.setUserId("USER_123");
        blacklistedIpTransaction.setAmount(BigDecimal.valueOf(5000));
        blacklistedIpTransaction.setIpAddress("10.0.0.1");

        FraudRule ipRule = FraudRule.builder()
            .ruleName("IP_BLACKLIST_RULE")
            .ruleType("IP_BLACKLIST")
            .riskWeight(BigDecimal.valueOf(0.8))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(ipRule, blacklistedIpTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(0.8);
        assertThat(result.getReason()).contains("IP address");
        assertThat(result.getReason()).contains("is in blacklist");
    }

    @Test
    void testEvaluateIpBlacklistRule_SafeIp_ShouldNotTrigger() {
        // Given
        Transaction safeIpTransaction = new Transaction();
        safeIpTransaction.setTransactionId("TXN_SAFE");
        safeIpTransaction.setUserId("USER_123");
        safeIpTransaction.setAmount(BigDecimal.valueOf(5000));
        safeIpTransaction.setIpAddress("203.208.60.1");

        FraudRule ipRule = FraudRule.builder()
            .ruleName("IP_BLACKLIST_RULE")
            .ruleType("IP_BLACKLIST")
            .riskWeight(BigDecimal.valueOf(0.8))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(ipRule, safeIpTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("IP address is safe");
    }

    @Test
    void testEvaluateCustomRule_ShouldNotTrigger() {
        // Given
        FraudRule customRule = FraudRule.builder()
            .ruleName("CUSTOM_BUSINESS_RULE")
            .ruleType("CUSTOM")
            .riskWeight(BigDecimal.valueOf(0.15))
            .ruleConfig("{\"businessLogic\": \"complex_rule\"}")
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(customRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Custom rule evaluation not implemented");
    }

    @Test
    void testEvaluateRule_UnsupportedRuleType_ShouldNotTrigger() {
        // Given
        FraudRule unsupportedRule = FraudRule.builder()
            .ruleName("UNKNOWN_RULE")
            .ruleType("UNKNOWN_TYPE")
            .riskWeight(BigDecimal.valueOf(0.5))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(unsupportedRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Unsupported rule type");
    }

    @Test
    void testEvaluateRule_ThresholdNotConfigured() {
        // Given
        FraudRule amountRule = FraudRule.builder()
            .ruleName("NO_THRESHOLD_RULE")
            .ruleType("AMOUNT")
            .thresholdValue(null)
            .riskWeight(BigDecimal.valueOf(0.3))
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(amountRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Threshold value not configured");
    }

    @Test
    void testEvaluateRule_RedisException_ShouldHandleGracefully() {
        // Given
        FraudRule frequencyRule = FraudRule.builder()
            .ruleName("FREQUENCY_RULE")
            .ruleType("FREQUENCY")
            .thresholdValue(BigDecimal.valueOf(5))
            .riskWeight(BigDecimal.valueOf(0.25))
            .build();

        // Mock Redis to throw exception
        when(zSetOperations.add(anyString(), anyString(), anyDouble()))
            .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(frequencyRule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Rule evaluation error");
    }

    @Test
    void testTimeOfDayBoundaryConditions() {
        FraudRule timeRule = FraudRule.builder()
            .ruleName("NIGHT_TIME_RULE")
            .ruleType("TIME_OF_DAY")
            .riskWeight(BigDecimal.valueOf(0.2))
            .build();

        // Test exact boundary - 22:00 (start of suspicious period)
        Transaction boundary1 = new Transaction();
        boundary1.setTransactionId("TXN_BOUNDARY_1");
        boundary1.setUserId("USER_123");
        boundary1.setAmount(BigDecimal.valueOf(5000));
        boundary1.setTimestamp(LocalDateTime.now().with(LocalTime.of(22, 0)));

        RuleEvaluationResult result1 = ruleEngine.evaluateRule(timeRule, boundary1);
        assertThat(result1.isTriggered()).isTrue();

        // Test exact boundary - 06:00 (end of suspicious period)
        Transaction boundary2 = new Transaction();
        boundary2.setTransactionId("TXN_BOUNDARY_2");
        boundary2.setUserId("USER_123");
        boundary2.setAmount(BigDecimal.valueOf(5000));
        boundary2.setTimestamp(LocalDateTime.now().with(LocalTime.of(6, 0)));

        RuleEvaluationResult result2 = ruleEngine.evaluateRule(timeRule, boundary2);
        assertThat(result2.isTriggered()).isFalse(); // 06:00 should be normal hours

        // Test just before boundary - 21:59
        Transaction beforeBoundary = new Transaction();
        beforeBoundary.setTransactionId("TXN_BEFORE");
        beforeBoundary.setUserId("USER_123");
        beforeBoundary.setAmount(BigDecimal.valueOf(5000));
        beforeBoundary.setTimestamp(LocalDateTime.now().with(LocalTime.of(21, 59)));

        RuleEvaluationResult result3 = ruleEngine.evaluateRule(timeRule, beforeBoundary);
        assertThat(result3.isTriggered()).isFalse();
    }
} 