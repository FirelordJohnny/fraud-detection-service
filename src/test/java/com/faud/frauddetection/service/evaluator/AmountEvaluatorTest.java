package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.config.FraudDetectionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test cases for AmountEvaluator
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AmountEvaluatorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private FraudDetectionProperties properties;

    @Mock
    private FraudDetectionProperties.TimeWindow timeWindow;

    private AmountEvaluator amountEvaluator;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        
        // Setup mock properties
        when(properties.getTimeWindow()).thenReturn(timeWindow);
        when(timeWindow.getDefaultSeconds()).thenReturn(3600L);
        
        amountEvaluator = new AmountEvaluator(redisTemplate, properties);
        
        testTransaction = Transaction.builder()
                .transactionId("TXN_001")
                .userId("USER_123")
                .amount(BigDecimal.valueOf(1000))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void testSupports_AmountRuleType_ShouldReturnTrue() {
        // When & Then
        assertThat(amountEvaluator.supports("AMOUNT")).isTrue();
        assertThat(amountEvaluator.supports("amount")).isTrue();
        assertThat(amountEvaluator.supports("Amount")).isTrue();
    }

    @Test
    void testSupports_NonAmountRuleType_ShouldReturnFalse() {
        // When & Then
        assertThat(amountEvaluator.supports("FREQUENCY")).isFalse();
        assertThat(amountEvaluator.supports("TIME_OF_DAY")).isFalse();
        assertThat(amountEvaluator.supports("UNKNOWN")).isFalse();
        assertThat(amountEvaluator.supports("")).isFalse();
        assertThat(amountEvaluator.supports(null)).isFalse();
    }

    @Test
    void testEvaluateRule_CumulativeAmountExceedsThreshold_ShouldTrigger() {
        // Given
        FraudRule rule = createAmountRule("cumulative_amount", BigDecimal.valueOf(2000), 
            "{\"timeWindowSeconds\": 3600}");
        
        // Mock Redis to return cumulative amount exceeding threshold
        Set<ZSetOperations.TypedTuple<String>> mockData = new HashSet<>();
        mockData.add(new DefaultTypedTuple<>("500.00", 1000.0));
        mockData.add(new DefaultTypedTuple<>("800.00", 2000.0));
        mockData.add(new DefaultTypedTuple<>("1000.00", 3000.0)); // Current transaction
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(mockData);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
        assertThat(result.getReason()).contains("Cumulative amount 2300.00");
        assertThat(result.getReason()).contains("exceeds threshold 2000");
        assertThat(result.getActualValue()).isEqualTo("2300.00");
        assertThat(result.getThresholdValue()).isEqualTo("2000");
        
        // Verify Redis interactions
        verify(zSetOperations).add(eq("amounts:user:USER_123"), eq("1000"), anyDouble());
        verify(zSetOperations).removeRangeByScore(eq("amounts:user:USER_123"), anyDouble(), anyDouble());
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(3600L), any());
    }

    @Test
    void testEvaluateRule_CumulativeAmountBelowThreshold_ShouldNotTrigger() {
        // Given
        FraudRule rule = createAmountRule("cumulative_amount", BigDecimal.valueOf(5000), 
            "{\"timeWindowSeconds\": 1800}");
        
        // Mock Redis to return cumulative amount below threshold
        Set<ZSetOperations.TypedTuple<String>> mockData = new HashSet<>();
        mockData.add(new DefaultTypedTuple<>("1000.00", 1000.0));
        mockData.add(new DefaultTypedTuple<>("2000.00", 2000.0));
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(2L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(mockData);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Cumulative amount is normal");
        assertThat(result.getActualValue()).isEqualTo("3000.00");
        assertThat(result.getThresholdValue()).isEqualTo("5000");
    }

    @Test
    void testEvaluateRule_CustomTimeWindow_ShouldUseCustomValue() {
        // Given
        FraudRule rule = createAmountRule("custom_window", BigDecimal.valueOf(1000), 
            "{\"timeWindowSeconds\": 7200}"); // 2 hours
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - verify custom time window is used
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(7200L), any());
    }

    @Test
    void testEvaluateRule_NoThresholdValue_ShouldReturnError() {
        // Given
        FraudRule rule = FraudRule.builder()
            .ruleName("no_threshold")
            .ruleType("AMOUNT")
            .thresholdValue(null)
            .build();

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("Threshold value not configured");
        assertThat(result.getRuleName()).isEqualTo("no_threshold");
    }

    @Test
    void testEvaluateRule_InvalidJsonConfig_ShouldUseDefault() {
        // Given
        FraudRule rule = createAmountRule("invalid_json", BigDecimal.valueOf(1000), 
            "{invalid json}");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window (3600 seconds)
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(3600L), any());
    }

    @Test
    void testEvaluateRule_EmptyRuleConfig_ShouldUseDefault() {
        // Given
        FraudRule rule = createAmountRule("empty_config", BigDecimal.valueOf(1000), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window (3600 seconds)
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(3600L), any());
    }

    @Test
    void testEvaluateRule_InvalidAmountInRedis_ShouldHandleGracefully() {
        // Given
        FraudRule rule = createAmountRule("invalid_redis_data", BigDecimal.valueOf(1000), "");
        
        Set<ZSetOperations.TypedTuple<String>> mockData = new HashSet<>();
        mockData.add(new DefaultTypedTuple<>("invalid_amount", 1000.0));
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(mockData);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should handle gracefully and continue
        assertThat(result).isNotNull();
        assertThat(result.getRuleName()).isEqualTo("invalid_redis_data");
    }

    @Test
    void testEvaluateRule_RedisException_ShouldHandleGracefully() {
        // Given
        FraudRule rule = createAmountRule("redis_exception", BigDecimal.valueOf(1000), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble()))
            .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("Rule evaluation error: Redis connection failed");
        assertThat(result.getRuleName()).isEqualTo("redis_exception");
    }

    @Test
    void testEvaluateRule_NullRedisData_ShouldHandleGracefully() {
        // Given
        FraudRule rule = createAmountRule("null_data", BigDecimal.valueOf(1000), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(null);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getActualValue()).isEqualTo("0");
        assertThat(result.getThresholdValue()).isEqualTo("1000");
    }

    @Test
    void testEvaluateRule_ExactThresholdMatch_ShouldNotTrigger() {
        // Given
        FraudRule rule = createAmountRule("exact_match", BigDecimal.valueOf(2000), "");
        
        Set<ZSetOperations.TypedTuple<String>> mockData = new HashSet<>();
        mockData.add(new DefaultTypedTuple<>("1000.00", 1000.0)); // Previous transaction
        mockData.add(new DefaultTypedTuple<>("1000.00", 2000.0)); // Current transaction added by evaluator
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(mockData);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - threshold = 2000, cumulative = 1000 + 1000 = 2000, exactly equal should not trigger
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getActualValue()).isEqualTo("2000.00");
        assertThat(result.getThresholdValue()).isEqualTo("2000");
    }

    @Test
    void testEvaluateRule_RiskScoreCalculation_ShouldBeCorrect() {
        // Given
        FraudRule rule = createAmountRule("risk_calculation", BigDecimal.valueOf(1000), "");
        
        Set<ZSetOperations.TypedTuple<String>> mockData = new HashSet<>();
        mockData.add(new DefaultTypedTuple<>("500.00", 1000.0)); // Previous transaction
        mockData.add(new DefaultTypedTuple<>("1000.00", 2000.0)); // Current transaction
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(mockData);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(1.0); // Should be capped at 1.0
    }

    @Test
    void testGetTimeWindowFromRule_StringTimeWindow_ShouldParseCorrectly() {
        // Given
        FraudRule rule = createAmountRule("string_window", BigDecimal.valueOf(1000), 
            "{\"timeWindowSeconds\": \"7200\"}"); // String value should be parsed
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should parse string value correctly
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(7200L), any());
    }

    @Test
    void testGetTimeWindowFromRule_NumericTimeWindow_ShouldParseCorrectly() {
        // Given
        FraudRule rule = createAmountRule("numeric_window", BigDecimal.valueOf(1000), 
            "{\"timeWindowSeconds\": 1800}"); // Numeric value
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should parse numeric value correctly
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(1800L), any());
    }

    @Test
    void testGetTimeWindowFromRule_MissingTimeWindowKey_ShouldUseDefault() {
        // Given
        FraudRule rule = createAmountRule("missing_key", BigDecimal.valueOf(1000), 
            "{\"otherKey\": 1800}"); // Missing timeWindowSeconds key
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(3600L), any());
    }

    @Test
    void testGetTimeWindowFromRule_NumberFormatException_ShouldUseDefault() {
        // Given
        FraudRule rule = createAmountRule("invalid_number", BigDecimal.valueOf(1000), 
            "{\"timeWindowSeconds\": \"invalid_number\"}"); // Invalid string number
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(3600L), any());
    }

    @Test
    void testGetTimeWindowFromRule_NullRuleConfig_ShouldUseDefault() {
        // Given
        FraudRule rule = createAmountRule("null_config", BigDecimal.valueOf(1000), null);
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(new HashSet<>());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        amountEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window
        verify(redisTemplate).expire(eq("amounts:user:USER_123"), eq(3600L), any());
    }

    @Test
    void testEvaluateRule_LowRiskScore_ShouldCalculateCorrectly() {
        // Given
        FraudRule rule = createAmountRule("low_risk", BigDecimal.valueOf(2000), "");
        
        Set<ZSetOperations.TypedTuple<String>> mockData = new HashSet<>();
        mockData.add(new DefaultTypedTuple<>("500.00", 1000.0)); // Total = 1500, risk = 1500/2000 = 0.75
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(mockData);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse(); // 1500 <= 2000
        assertThat(result.getRiskScore()).isEqualTo(0.0); // Not triggered
    }

    @Test
    void testEvaluateRule_MultipleTransactions_ShouldSumCorrectly() {
        // Given
        FraudRule rule = createAmountRule("multi_tx", BigDecimal.valueOf(3000), "");
        
        Set<ZSetOperations.TypedTuple<String>> mockData = new HashSet<>();
        mockData.add(new DefaultTypedTuple<>("1000.00", 1000.0)); // Previous transaction 1
        mockData.add(new DefaultTypedTuple<>("1500.00", 1500.0)); // Previous transaction 2
        mockData.add(new DefaultTypedTuple<>("1000.00", 2000.0)); // Current transaction
        // Total = 1000 + 1500 + 1000 = 3500
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.rangeByScoreWithScores(anyString(), anyDouble(), anyDouble()))
            .thenReturn(mockData);
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // When
        RuleEvaluationResult result = amountEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getActualValue()).isEqualTo("3500.00");
        assertThat(result.getRiskScore()).isEqualTo(1.0); // 3500/3000 > 1, capped at 1.0
    }

    private FraudRule createAmountRule(String ruleName, BigDecimal threshold, String config) {
        return FraudRule.builder()
            .ruleName(ruleName)
            .ruleType("AMOUNT")
            .thresholdValue(threshold)
            .ruleConfig(config)
            .riskWeight(BigDecimal.valueOf(0.5))
            .enabled(true)
            .build();
    }
} 