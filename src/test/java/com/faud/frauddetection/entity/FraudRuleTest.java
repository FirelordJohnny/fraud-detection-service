package com.faud.frauddetection.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FraudRuleTest {
    
    private FraudRule fraudRule;
    
    @BeforeEach
    void setUp() {
        fraudRule = new FraudRule();
    }
    
    @Test
    void testBuilder_ShouldCreateCompleteRule() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        FraudRule rule = FraudRule.builder()
                .id(1L)
                .ruleName("test-rule")
                .ruleType("AMOUNT")
                .description("Test rule description")
                .ruleConfig("{\"maxAmount\": 1000}")
                .enabled(true)
                .thresholdValue(new BigDecimal("500.00"))
                .conditionField("amount")
                .conditionOperator("GT")
                .conditionValue("1000")
                .riskWeight(new BigDecimal("0.8"))
                .priority(5)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        // Then
        assertThat(rule.getId()).isEqualTo(1L);
        assertThat(rule.getRuleName()).isEqualTo("test-rule");
        assertThat(rule.getRuleType()).isEqualTo("AMOUNT");
        assertThat(rule.getDescription()).isEqualTo("Test rule description");
        assertThat(rule.getRuleConfig()).isEqualTo("{\"maxAmount\": 1000}");
        assertThat(rule.getEnabled()).isTrue();
        assertThat(rule.getThresholdValue()).isEqualTo(new BigDecimal("500.00"));
        assertThat(rule.getConditionField()).isEqualTo("amount");
        assertThat(rule.getConditionOperator()).isEqualTo("GT");
        assertThat(rule.getConditionValue()).isEqualTo("1000");
        assertThat(rule.getRiskWeight()).isEqualTo(new BigDecimal("0.8"));
        assertThat(rule.getPriority()).isEqualTo(5);
        assertThat(rule.getCreatedAt()).isEqualTo(now);
        assertThat(rule.getUpdatedAt()).isEqualTo(now);
    }
    
    @Test
    void testNoArgsConstructor_ShouldCreateEmptyRule() {
        // When
        FraudRule rule = new FraudRule();
        
        // Then
        assertThat(rule.getId()).isNull();
        assertThat(rule.getRuleName()).isNull();
        assertThat(rule.getRuleType()).isNull();
        assertThat(rule.getDescription()).isNull();
        assertThat(rule.getRuleConfig()).isNull();
        assertThat(rule.getEnabled()).isTrue(); // Default value
        assertThat(rule.getThresholdValue()).isNull();
        assertThat(rule.getConditionField()).isNull();
        assertThat(rule.getConditionOperator()).isNull();
        assertThat(rule.getConditionValue()).isNull();
        assertThat(rule.getRiskWeight()).isNull();
        assertThat(rule.getPriority()).isNull();
        assertThat(rule.getCreatedAt()).isNull();
        assertThat(rule.getUpdatedAt()).isNull();
    }
    
    @Test
    void testAllArgsConstructor_ShouldCreateCompleteRule() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        FraudRule rule = new FraudRule(
                1L,
                "test-rule",
                "AMOUNT", 
                "Test description",
                "{\"config\": true}",
                true,
                new BigDecimal("100.00"),
                "amount",
                "GT",
                "50",
                new BigDecimal("0.5"),
                2,
                now,
                now
        );
        
        // Then
        assertThat(rule.getId()).isEqualTo(1L);
        assertThat(rule.getRuleName()).isEqualTo("test-rule");
        assertThat(rule.getRuleType()).isEqualTo("AMOUNT");
        assertThat(rule.getDescription()).isEqualTo("Test description");
        assertThat(rule.getRuleConfig()).isEqualTo("{\"config\": true}");
        assertThat(rule.getEnabled()).isTrue();
        assertThat(rule.getThresholdValue()).isEqualTo(new BigDecimal("100.00"));
        assertThat(rule.getConditionField()).isEqualTo("amount");
        assertThat(rule.getConditionOperator()).isEqualTo("GT");
        assertThat(rule.getConditionValue()).isEqualTo("50");
        assertThat(rule.getRiskWeight()).isEqualTo(new BigDecimal("0.5"));
        assertThat(rule.getPriority()).isEqualTo(2);
        assertThat(rule.getCreatedAt()).isEqualTo(now);
        assertThat(rule.getUpdatedAt()).isEqualTo(now);
    }
    
    @Test
    void testSettersAndGetters_ShouldWorkCorrectly() {
        // When
        fraudRule.setId(123L);
        fraudRule.setRuleName("custom-rule");
        fraudRule.setRuleType("FREQUENCY");
        fraudRule.setDescription("Custom test rule");
        fraudRule.setRuleConfig("{\"frequency\": 5}");
        fraudRule.setEnabled(false);
        fraudRule.setThresholdValue(new BigDecimal("999.99"));
        fraudRule.setConditionField("userId");
        fraudRule.setConditionOperator("EQ");
        fraudRule.setConditionValue("suspicious-user");
        fraudRule.setRiskWeight(new BigDecimal("0.9"));
        fraudRule.setPriority(10);
        
        LocalDateTime testTime = LocalDateTime.now();
        fraudRule.setCreatedAt(testTime);
        fraudRule.setUpdatedAt(testTime);
        
        // Then
        assertThat(fraudRule.getId()).isEqualTo(123L);
        assertThat(fraudRule.getRuleName()).isEqualTo("custom-rule");
        assertThat(fraudRule.getRuleType()).isEqualTo("FREQUENCY");
        assertThat(fraudRule.getDescription()).isEqualTo("Custom test rule");
        assertThat(fraudRule.getRuleConfig()).isEqualTo("{\"frequency\": 5}");
        assertThat(fraudRule.getEnabled()).isFalse();
        assertThat(fraudRule.getThresholdValue()).isEqualTo(new BigDecimal("999.99"));
        assertThat(fraudRule.getConditionField()).isEqualTo("userId");
        assertThat(fraudRule.getConditionOperator()).isEqualTo("EQ");
        assertThat(fraudRule.getConditionValue()).isEqualTo("suspicious-user");
        assertThat(fraudRule.getRiskWeight()).isEqualTo(new BigDecimal("0.9"));
        assertThat(fraudRule.getPriority()).isEqualTo(10);
        assertThat(fraudRule.getCreatedAt()).isEqualTo(testTime);
        assertThat(fraudRule.getUpdatedAt()).isEqualTo(testTime);
    }
    
    @Test
    void testOnCreate_WhenFirstTimeCall_ShouldSetTimestampsAndDefaults() {
        // Given
        FraudRule rule = new FraudRule();
        rule.setEnabled(null);
        rule.setRiskWeight(null);
        rule.setPriority(null);
        
        // When
        rule.onCreate();
        
        // Then
        assertThat(rule.getCreatedAt()).isNotNull();
        assertThat(rule.getUpdatedAt()).isNotNull();
        assertThat(rule.getEnabled()).isTrue();
        assertThat(rule.getRiskWeight()).isEqualTo(new BigDecimal("0.2"));
        assertThat(rule.getPriority()).isEqualTo(1);
    }
    
    @Test
    void testOnCreate_WhenCreatedAtAlreadySet_ShouldNotOverrideCreatedAt() {
        // Given
        LocalDateTime existingCreatedAt = LocalDateTime.now().minusDays(1);
        fraudRule.setCreatedAt(existingCreatedAt);
        
        // When
        fraudRule.onCreate();
        
        // Then
        assertThat(fraudRule.getCreatedAt()).isEqualTo(existingCreatedAt);
        assertThat(fraudRule.getUpdatedAt()).isNotNull();
        assertThat(fraudRule.getUpdatedAt()).isAfter(existingCreatedAt);
    }
    
    @Test
    void testOnCreate_WhenEnabledAlreadySet_ShouldNotOverrideEnabled() {
        // Given
        fraudRule.setEnabled(false);
        
        // When
        fraudRule.onCreate();
        
        // Then
        assertThat(fraudRule.getEnabled()).isFalse();
    }
    
    @Test
    void testOnCreate_WhenRiskWeightAlreadySet_ShouldNotOverrideRiskWeight() {
        // Given
        BigDecimal customWeight = new BigDecimal("0.7");
        fraudRule.setRiskWeight(customWeight);
        
        // When
        fraudRule.onCreate();
        
        // Then
        assertThat(fraudRule.getRiskWeight()).isEqualTo(customWeight);
    }
    
    @Test
    void testOnCreate_WhenPriorityAlreadySet_ShouldNotOverridePriority() {
        // Given
        fraudRule.setPriority(5);
        
        // When
        fraudRule.onCreate();
        
        // Then
        assertThat(fraudRule.getPriority()).isEqualTo(5);
    }
    
    @Test
    void testOnUpdate_ShouldUpdateTimestamp() {
        // Given
        LocalDateTime oldTime = LocalDateTime.now().minusHours(1);
        fraudRule.setUpdatedAt(oldTime);
        
        // When
        fraudRule.onUpdate();
        
        // Then
        assertThat(fraudRule.getUpdatedAt()).isNotNull();
        assertThat(fraudRule.getUpdatedAt()).isAfter(oldTime);
    }
    
    @Test
    void testGetEvaluationType_WithSingleCondition_ShouldReturnSingleCondition() {
        // Given
        fraudRule.setConditionField("amount");
        fraudRule.setConditionOperator("GT");
        fraudRule.setRuleConfig("{\"some\": \"config\"}"); // This should be ignored
        
        // When
        RuleEvaluationType type = fraudRule.getEvaluationType();
        
        // Then
        assertThat(type).isEqualTo(RuleEvaluationType.SINGLE_CONDITION);
    }
    
    @Test
    void testGetEvaluationType_WithMultiCondition_ShouldReturnMultiCondition() {
        // Given
        fraudRule.setRuleConfig("{\"conditions\": [...]}");
        fraudRule.setConditionField(null);
        fraudRule.setConditionOperator(null);
        
        // When
        RuleEvaluationType type = fraudRule.getEvaluationType();
        
        // Then
        assertThat(type).isEqualTo(RuleEvaluationType.MULTI_CONDITION);
    }
    
    @Test
    void testGetEvaluationType_WithEmptyRuleConfig_ShouldReturnInvalid() {
        // Given
        fraudRule.setRuleConfig("");
        fraudRule.setConditionField(null);
        fraudRule.setConditionOperator(null);
        
        // When
        RuleEvaluationType type = fraudRule.getEvaluationType();
        
        // Then
        assertThat(type).isEqualTo(RuleEvaluationType.INVALID);
    }
    
    @Test
    void testGetEvaluationType_WithWhitespaceRuleConfig_ShouldReturnInvalid() {
        // Given
        fraudRule.setRuleConfig("   ");
        fraudRule.setConditionField(null);
        fraudRule.setConditionOperator(null);
        
        // When
        RuleEvaluationType type = fraudRule.getEvaluationType();
        
        // Then
        assertThat(type).isEqualTo(RuleEvaluationType.INVALID);
    }
    
    @Test
    void testGetEvaluationType_WithNullValues_ShouldReturnInvalid() {
        // Given
        fraudRule.setRuleConfig(null);
        fraudRule.setConditionField(null);
        fraudRule.setConditionOperator(null);
        
        // When
        RuleEvaluationType type = fraudRule.getEvaluationType();
        
        // Then
        assertThat(type).isEqualTo(RuleEvaluationType.INVALID);
    }
    
    @Test
    void testGetEvaluationType_WithPartialSingleCondition_ShouldReturnInvalid() {
        // Given - Only conditionField set, missing conditionOperator
        fraudRule.setConditionField("amount");
        fraudRule.setConditionOperator(null);
        fraudRule.setRuleConfig(null);
        
        // When
        RuleEvaluationType type = fraudRule.getEvaluationType();
        
        // Then  
        assertThat(type).isEqualTo(RuleEvaluationType.INVALID);
    }
    
    @Test
    void testEquals_WithSameValues_ShouldBeEqual() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        FraudRule rule1 = FraudRule.builder()
                .id(1L)
                .ruleName("test")
                .ruleType("AMOUNT")
                .enabled(true)
                .createdAt(now)
                .build();
                
        FraudRule rule2 = FraudRule.builder()
                .id(1L)
                .ruleName("test")
                .ruleType("AMOUNT")
                .enabled(true)
                .createdAt(now)
                .build();
        
        // Then
        assertThat(rule1).isEqualTo(rule2);
        assertThat(rule1.hashCode()).isEqualTo(rule2.hashCode());
    }
    
    @Test
    void testEquals_WithDifferentValues_ShouldNotBeEqual() {
        // Given
        FraudRule rule1 = FraudRule.builder()
                .id(1L)
                .ruleName("test1")
                .build();
                
        FraudRule rule2 = FraudRule.builder()
                .id(1L)
                .ruleName("test2")
                .build();
        
        // Then
        assertThat(rule1).isNotEqualTo(rule2);
    }
    
    @Test
    void testToString_ShouldNotBeEmpty() {
        // Given
        fraudRule.setId(1L);
        fraudRule.setRuleName("test-rule");
        
        // When
        String result = fraudRule.toString();
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).contains("FraudRule");
        assertThat(result).contains("id=1");
        assertThat(result).contains("ruleName=test-rule");
    }
    
    @Test 
    void testNullHandling_ShouldHandleNullValues() {
        // Given
        FraudRule rule = new FraudRule();
        
        // When - Setting null values should not cause exceptions
        rule.setId(null);
        rule.setRuleName(null);
        rule.setRuleType(null);
        rule.setDescription(null);
        rule.setRuleConfig(null);
        rule.setEnabled(null);
        rule.setThresholdValue(null);
        rule.setConditionField(null);
        rule.setConditionOperator(null);
        rule.setConditionValue(null);
        rule.setRiskWeight(null);
        rule.setPriority(null);
        rule.setCreatedAt(null);
        rule.setUpdatedAt(null);
        
        // Then
        assertThat(rule.getId()).isNull();
        assertThat(rule.getRuleName()).isNull();
        assertThat(rule.getRuleType()).isNull();
        assertThat(rule.getDescription()).isNull();
        assertThat(rule.getRuleConfig()).isNull();
        assertThat(rule.getEnabled()).isNull();
        assertThat(rule.getThresholdValue()).isNull();
        assertThat(rule.getConditionField()).isNull();
        assertThat(rule.getConditionOperator()).isNull();
        assertThat(rule.getConditionValue()).isNull();
        assertThat(rule.getRiskWeight()).isNull();
        assertThat(rule.getPriority()).isNull();
        assertThat(rule.getCreatedAt()).isNull();
        assertThat(rule.getUpdatedAt()).isNull();
    }
    
    @Test
    void testDefaultEnabledValue_ShouldBeTrueByDefault() {
        // When
        FraudRule rule = new FraudRule();
        
        // Then
        assertThat(rule.getEnabled()).isTrue();
    }
} 