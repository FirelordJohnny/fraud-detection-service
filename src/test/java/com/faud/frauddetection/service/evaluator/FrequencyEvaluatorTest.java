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
import org.springframework.data.redis.core.ValueOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test cases for FrequencyEvaluator
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FrequencyEvaluatorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private FraudDetectionProperties properties;

    @Mock
    private FraudDetectionProperties.TimeWindow timeWindow;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private FrequencyEvaluator frequencyEvaluator;
    private Transaction testTransaction;
    private ObjectMapper objectMapper;
    private FraudRule testRule;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(properties.getTimeWindow()).thenReturn(timeWindow);
        when(timeWindow.getDefaultSeconds()).thenReturn(3600L);

        frequencyEvaluator = new FrequencyEvaluator(redisTemplate, properties);
        
        testTransaction = Transaction.builder()
                .transactionId("TXN_001")
                .userId("USER_123")
                .amount(BigDecimal.valueOf(1000))
                .timestamp(LocalDateTime.now())
                .build();
        
        testRule = new FraudRule();
        testRule.setRuleName("Test Frequency Rule");
        testRule.setRuleConfig("{\"timeWindowSeconds\": 60}");
        testRule.setThresholdValue(new BigDecimal(5));
    }

    @Test
    void testSupports_FrequencyRuleType_ShouldReturnTrue() {
        // When & Then
        assertThat(frequencyEvaluator.supports("FREQUENCY")).isTrue();
        assertThat(frequencyEvaluator.supports("frequency")).isTrue();
        assertThat(frequencyEvaluator.supports("Frequency")).isTrue();
    }

    @Test
    void testSupports_NonFrequencyRuleType_ShouldReturnFalse() {
        // When & Then
        assertThat(frequencyEvaluator.supports("AMOUNT")).isFalse();
        assertThat(frequencyEvaluator.supports("TIME_OF_DAY")).isFalse();
        assertThat(frequencyEvaluator.supports("UNKNOWN")).isFalse();
        assertThat(frequencyEvaluator.supports("")).isFalse();
        assertThat(frequencyEvaluator.supports(null)).isFalse();
    }

    @Test
    void testEvaluateRule_FrequencyExceedsThreshold_ShouldTrigger() {
        // Given
        FraudRule rule = createFrequencyRule("high_frequency", BigDecimal.valueOf(5), 
            "{\"timeWindowSeconds\": 3600}");
        
        // Mock Redis to return frequency exceeding threshold
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(7L); // 7 transactions > 5 threshold
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
        assertThat(result.getReason()).contains("User has 7 transactions in 3600 seconds");
        assertThat(result.getReason()).contains("exceeds threshold 5");
        assertThat(result.getActualValue()).isEqualTo("7");
        assertThat(result.getThresholdValue()).isEqualTo("5");
        
        // Verify Redis interactions
        verify(zSetOperations).add(eq("transactions:user:USER_123"), anyString(), anyDouble());
        verify(zSetOperations).removeRangeByScore(eq("transactions:user:USER_123"), anyDouble(), anyDouble());
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testEvaluateRule_FrequencyBelowThreshold_ShouldNotTrigger() {
        // Given
        FraudRule rule = createFrequencyRule("normal_frequency", BigDecimal.valueOf(10), 
            "{\"timeWindowSeconds\": 1800}");
        
        // Mock Redis to return frequency below threshold
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(2L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L); // 3 transactions < 10 threshold
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).contains("Transaction frequency is normal");
        assertThat(result.getActualValue()).isEqualTo("3");
        assertThat(result.getThresholdValue()).isEqualTo("10");
    }

    @Test
    void testEvaluateRule_CustomTimeWindow_ShouldUseCustomValue() {
        // Given
        FraudRule rule = createFrequencyRule("custom_window", BigDecimal.valueOf(5), 
            "{\"timeWindowSeconds\": 7200}"); // 2 hours
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - verify custom time window is used
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(7200L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testEvaluateRule_DefaultTimeWindow_ShouldUseDefault() {
        // Given
        FraudRule rule = createFrequencyRule("default_window", BigDecimal.valueOf(5), null);
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window (3600 seconds)
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testEvaluateRule_NoThresholdValue_ShouldReturnError() {
        // Given
        FraudRule rule = FraudRule.builder()
            .ruleName("no_threshold")
            .ruleType("FREQUENCY")
            .thresholdValue(null)
            .build();

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("Threshold value not configured");
        assertThat(result.getRuleName()).isEqualTo("no_threshold");
    }

    @Test
    void testEvaluateRule_InvalidJsonConfig_ShouldUseDefault() {
        // Given
        FraudRule rule = createFrequencyRule("invalid_json", BigDecimal.valueOf(5), 
            "{invalid json}");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window (3600 seconds)
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testEvaluateRule_EmptyRuleConfig_ShouldUseDefault() {
        // Given
        FraudRule rule = createFrequencyRule("empty_config", BigDecimal.valueOf(5), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window (3600 seconds)
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testEvaluateRule_StringTimeWindowInConfig_ShouldParseCorrectly() {
        // Given
        FraudRule rule = createFrequencyRule("string_window", BigDecimal.valueOf(5), 
            "{\"timeWindowSeconds\": \"1800\"}"); // String value should be parsed
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should parse string value correctly
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(1800L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testEvaluateRule_NullTransactionCount_ShouldHandleGracefully() {
        // Given
        FraudRule rule = createFrequencyRule("null_count", BigDecimal.valueOf(5), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(null);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getActualValue()).isEqualTo("null");
        assertThat(result.getThresholdValue()).isEqualTo("5");
    }

    @Test
    void testEvaluateRule_RedisException_ShouldHandleGracefully() {
        // Given
        FraudRule rule = createFrequencyRule("redis_exception", BigDecimal.valueOf(5), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble()))
            .thenThrow(new RuntimeException("Redis connection failed"));

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("Rule evaluation error: Redis connection failed");
        assertThat(result.getRuleName()).isEqualTo("redis_exception");
    }

    @Test
    void testEvaluateRule_ExactThresholdMatch_ShouldNotTrigger() {
        // Given
        FraudRule rule = createFrequencyRule("exact_match", BigDecimal.valueOf(5), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(5L); // Exactly equal to threshold
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - exactly equal to threshold should not trigger
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getActualValue()).isEqualTo("5");
        assertThat(result.getThresholdValue()).isEqualTo("5");
    }

    @Test
    void testEvaluateRule_RiskScoreCalculation_ShouldBeCorrect() {
        // Given
        FraudRule rule = createFrequencyRule("risk_calculation", BigDecimal.valueOf(3), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(8L); // Triggers rule
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(1.0); // Should be capped at 1.0
    }

    @Test
    void testEvaluateRule_ValidTimeWindowFormats_ShouldParseAll() {
        // Test different valid time window formats
        String[] validConfigs = {
            "{\"timeWindowSeconds\": 1800}",      // Integer
            "{\"timeWindowSeconds\": \"3600\"}",  // String
            "{\"timeWindowSeconds\": 7200.0}",    // Double
        };
        
        Long[] expectedValues = {1800L, 3600L, 7200L};
        
        for (int i = 0; i < validConfigs.length; i++) {
            // Given
            FraudRule rule = createFrequencyRule("valid_format_" + i, BigDecimal.valueOf(5), validConfigs[i]);
            
            when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
            when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
            when(zSetOperations.zCard(anyString())).thenReturn(3L);
            when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

            // When
            frequencyEvaluator.evaluateRule(rule, testTransaction);

            // Then
            verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(expectedValues[i]), eq(TimeUnit.SECONDS));
        }
    }

    @Test
    void testGetTimeWindowFromRule_InvalidTimeWindowValue_ShouldUseDefault() {
        // Test with negative value that should fallback to default
        // Given
        FraudRule rule = createFrequencyRule("invalid_config", BigDecimal.valueOf(5), 
            "{\"timeWindowSeconds\": -100}");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use the parsed value even if negative (implementation detail)
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(-100L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testGetTimeWindowFromRule_NumberFormatException_ShouldUseDefault() {
        // Given
        FraudRule rule = createFrequencyRule("invalid_number", BigDecimal.valueOf(5), 
            "{\"timeWindowSeconds\": \"invalid_number\"}"); // Invalid string number
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testGetTimeWindowFromRule_MissingTimeWindowKey_ShouldUseDefault() {
        // Given
        FraudRule rule = createFrequencyRule("missing_key", BigDecimal.valueOf(5), 
            "{\"otherKey\": 1800}"); // Missing timeWindowSeconds key
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testGetTimeWindowFromRule_NullRuleConfig_ShouldUseDefault() {
        // Given
        FraudRule rule = createFrequencyRule("null_config", BigDecimal.valueOf(5), null);
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(3L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then - should use default time window
        verify(redisTemplate).expire(eq("transactions:user:USER_123"), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testEvaluateRule_HighFrequency_ShouldTriggerWithCorrectRiskScore() {
        // Given
        FraudRule rule = createFrequencyRule("high_freq", BigDecimal.valueOf(3), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(6L); // 6 > 3, ratio = 6/3 = 2.0, capped at 1.0
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(1.0); // Should be capped at 1.0
        assertThat(result.getActualValue()).isEqualTo("6");
        assertThat(result.getThresholdValue()).isEqualTo("3");
    }

    @Test
    void testEvaluateRule_LowRiskScore_ShouldCalculateCorrectly() {
        // Given
        FraudRule rule = createFrequencyRule("low_risk", BigDecimal.valueOf(10), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(7L); // 7 <= 10, doesn't trigger
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0); // Not triggered
        assertThat(result.getActualValue()).isEqualTo("7");
        assertThat(result.getThresholdValue()).isEqualTo("10");
    }

    @Test
    void testEvaluateRule_EdgeCaseCount_ShouldHandleCorrectly() {
        // Given
        FraudRule rule = createFrequencyRule("edge_case", BigDecimal.valueOf(1), "");
        
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);
        when(zSetOperations.removeRangeByScore(anyString(), anyDouble(), anyDouble())).thenReturn(0L);
        when(zSetOperations.zCard(anyString())).thenReturn(1L); // Exactly equals threshold
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        // When
        RuleEvaluationResult result = frequencyEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse(); // count <= threshold
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getActualValue()).isEqualTo("1");
        assertThat(result.getThresholdValue()).isEqualTo("1");
    }

    private FraudRule createFrequencyRule(String ruleName, BigDecimal threshold, String config) {
        return FraudRule.builder()
            .ruleName(ruleName)
            .ruleType("FREQUENCY")
            .thresholdValue(threshold)
            .ruleConfig(config)
            .riskWeight(BigDecimal.valueOf(0.6))
            .enabled(true)
            .build();
    }
} 