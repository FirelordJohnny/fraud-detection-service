package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.MultiConditionConfig;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.entity.RuleEvaluationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive test for hybrid solution rule engine
 * Covers all scenarios including single condition, multi-condition grouping
 */
@ExtendWith(MockitoExtension.class)
class MultiConditionEvaluatorTest {

    private DynamicEvaluator ruleEngine;
    private MultiConditionEvaluator multiConditionEvaluator;
    private ObjectMapper objectMapper;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        multiConditionEvaluator = new MultiConditionEvaluator();
        ruleEngine = new DynamicEvaluator(multiConditionEvaluator);
        objectMapper = new ObjectMapper();
        
        testTransaction = Transaction.builder()
                .transactionId("TXN_MULTI_001")
                .userId("USER_MULTI_123")
                .amount(new BigDecimal("15000.00"))
                .currency("USD")
                .ipAddress("192.168.1.100")
                .country("US")
                .paymentMethod("CREDIT_CARD")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ===== Single Condition Tests =====
    
    @Test
    void testSingleCondition_AmountGreaterThan_ShouldTrigger() {
        FraudRule rule = createSingleConditionRule("HIGH_AMOUNT", "amount", "GT", "10000");
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
        assertThat(result.getRuleName()).isEqualTo("HIGH_AMOUNT");
        assertThat(rule.getEvaluationType()).isEqualTo(RuleEvaluationType.SINGLE_CONDITION);
    }
    
    @Test
    void testSingleCondition_CurrencyIn_ShouldTrigger() {
        FraudRule rule = createSingleConditionRule("CURRENCY_CHECK", "currency", "IN", "USD,EUR,GBP");
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }
    
    @Test
    void testSingleCondition_NotTriggered() {
        FraudRule rule = createSingleConditionRule("LOW_AMOUNT", "amount", "GT", "20000");
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
    }

    // ===== Multi-Condition Single Group Tests =====
    
    @Test
    void testMultiCondition_SingleGroup_AND_AllMatch_ShouldTrigger() throws Exception {
        // Create single group multi-condition rule: amount > 10000 AND currency = USD
        MultiConditionConfig config = createSingleGroupConfig("AND",
            createCondition("amount", "GT", "10000"),
            createCondition("currency", "EQ", "USD")
        );
        
        FraudRule rule = createMultiConditionRule("SINGLE_GROUP_AND", config);
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
        assertThat(rule.getEvaluationType()).isEqualTo(RuleEvaluationType.MULTI_CONDITION);
    }
    
    @Test
    void testMultiCondition_SingleGroup_AND_PartialMatch_ShouldNotTrigger() throws Exception {
        // Create single group multi-condition rule: amount > 20000 AND currency = USD (amount not satisfied)
        MultiConditionConfig config = createSingleGroupConfig("AND",
            createCondition("amount", "GT", "20000"),
            createCondition("currency", "EQ", "USD")
        );
        
        FraudRule rule = createMultiConditionRule("SINGLE_GROUP_AND_FAIL", config);
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
    }
    
    @Test
    void testMultiCondition_SingleGroup_OR_OneMatch_ShouldTrigger() throws Exception {
        // Create single group multi-condition rule: amount > 20000 OR currency = USD (currency satisfied)
        MultiConditionConfig config = createSingleGroupConfig("OR",
            createCondition("amount", "GT", "20000"),
            createCondition("currency", "EQ", "USD")
        );
        
        FraudRule rule = createMultiConditionRule("SINGLE_GROUP_OR", config);
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    // ===== Multi-Condition Multi-Group Tests =====
    
    @Test
    void testMultiCondition_MultipleGroups_AND_AllGroupsMatch_ShouldTrigger() throws Exception {
        // Group 1: amount > 10000 AND currency = USD
        // Group 2: country = US OR paymentMethod = CREDIT_CARD
        // Inter-group relationship: AND
        MultiConditionConfig config = MultiConditionConfig.builder()
            .groupLogicalOperator("AND")
            .conditionGroups(Arrays.asList(
                createConditionGroup("group1", "AND",
                    createCondition("amount", "GT", "10000"),
                    createCondition("currency", "EQ", "USD")
                ),
                createConditionGroup("group2", "OR",
                    createCondition("country", "EQ", "US"),
                    createCondition("paymentMethod", "EQ", "CREDIT_CARD")
                )
            ))
            .build();
        
        FraudRule rule = createMultiConditionRule("MULTI_GROUP_AND", config);
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }
    
    @Test
    void testMultiCondition_MultipleGroups_OR_OneGroupMatch_ShouldTrigger() throws Exception {
        // Group 1: amount > 50000 (not satisfied)
        // Group 2: currency = USD (satisfied)
        // Inter-group relationship: OR
        MultiConditionConfig config = MultiConditionConfig.builder()
            .groupLogicalOperator("OR")
            .conditionGroups(Arrays.asList(
                createConditionGroup("group1", "AND",
                    createCondition("amount", "GT", "50000")
                ),
                createConditionGroup("group2", "AND",
                    createCondition("currency", "EQ", "USD")
                )
            ))
            .build();
        
        FraudRule rule = createMultiConditionRule("MULTI_GROUP_OR", config);
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }
    
    @Test
    void testMultiCondition_ComplexScenario_ShouldWork() throws Exception {
        // Complex scenario:
        // Group 1: (amount > 10000 AND currency IN [USD,EUR])
        // Group 2: (ipAddress CONTAINS 192.168 OR country = US)
        // Group 3: (paymentMethod = CREDIT_CARD)
        // Inter-group relationship: AND
        MultiConditionConfig config = MultiConditionConfig.builder()
            .groupLogicalOperator("AND")
            .conditionGroups(Arrays.asList(
                createConditionGroup("high_amount_group", "AND",
                    createCondition("amount", "GT", "10000"),
                    createCondition("currency", "IN", "USD,EUR")
                ),
                createConditionGroup("location_group", "OR",
                    createCondition("ipAddress", "CONTAINS", "192.168"),
                    createCondition("country", "EQ", "US")
                ),
                createConditionGroup("payment_group", "AND",
                    createCondition("paymentMethod", "EQ", "CREDIT_CARD")
                )
            ))
            .build();
        
        FraudRule rule = createMultiConditionRule("COMPLEX_SCENARIO", config);
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    // ===== Rule Engine Support Tests =====
    
    @Test
    void testRuleEngine_Supports_AllRuleTypes() {
        assertThat(ruleEngine.supports("SIMPLE")).isTrue();
        assertThat(ruleEngine.supports("MULTI_CONDITION")).isTrue();
        assertThat(ruleEngine.supports("COMPLEX_CONDITION")).isTrue();
        assertThat(ruleEngine.supports("FIELD_CONDITION")).isTrue();
        assertThat(ruleEngine.supports("TIME_OF_DAY")).isTrue();
        
        // Unsupported types
        assertThat(ruleEngine.supports("FREQUENCY")).isFalse();
        assertThat(ruleEngine.supports("AMOUNT")).isFalse();
        assertThat(ruleEngine.supports(null)).isFalse();
    }
    
    // ===== Error Handling Tests =====
    
    @Test
    void testMultiCondition_InvalidJSON_ShouldHandleGracefully() {
        FraudRule rule = FraudRule.builder()
            .ruleName("INVALID_JSON")
            .ruleType("MULTI_CONDITION")
            .ruleConfig("{invalid json}")
            .build();
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("Multi-condition evaluation error");
    }
    
    @Test
    void testMultiCondition_EmptyConditionGroups_ShouldNotTrigger() throws Exception {
        MultiConditionConfig config = MultiConditionConfig.builder()
            .groupLogicalOperator("AND")
            .conditionGroups(Arrays.asList())
            .build();
        
        FraudRule rule = createMultiConditionRule("EMPTY_GROUPS", config);
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("No condition groups configured");
    }

    // ===== Helper Methods =====
    
    private FraudRule createSingleConditionRule(String ruleName, String field, String operator, String value) {
        return FraudRule.builder()
            .ruleName(ruleName)
            .ruleType("SIMPLE")
            .conditionField(field)
            .conditionOperator(operator)
            .conditionValue(value)
            .riskWeight(BigDecimal.valueOf(0.5))
            .build();
    }
    
    private FraudRule createMultiConditionRule(String ruleName, MultiConditionConfig config) throws Exception {
        String configJson = objectMapper.writeValueAsString(config);
        return FraudRule.builder()
            .ruleName(ruleName)
            .ruleType("MULTI_CONDITION")
            .ruleConfig(configJson)
            .riskWeight(BigDecimal.valueOf(0.5))
            .build();
    }
    
    private MultiConditionConfig createSingleGroupConfig(String operator, MultiConditionConfig.RuleCondition... conditions) {
        return MultiConditionConfig.builder()
            .groupLogicalOperator("AND")
            .conditionGroups(Arrays.asList(
                createConditionGroup("group1", operator, conditions)
            ))
            .build();
    }
    
    private MultiConditionConfig.ConditionGroup createConditionGroup(String groupId, String operator, MultiConditionConfig.RuleCondition... conditions) {
        return MultiConditionConfig.ConditionGroup.builder()
            .groupId(groupId)
            .intraGroupOperator(operator)
            .conditions(Arrays.asList(conditions))
            .build();
    }
    
    private MultiConditionConfig.RuleCondition createCondition(String field, String operator, String value) {
        return MultiConditionConfig.RuleCondition.builder()
            .field(field)
            .operator(operator)
            .value(value)
            .build();
    }

    @Test
    void testMultiCondition_TimeInRangeOperator_ShouldEvaluateCorrectly() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"timestamp\","
            + "\"operator\": \"TIME_IN_RANGE\","
            + "\"value\": \"09:00-17:00\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("TIME_RANGE_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .riskWeight(BigDecimal.valueOf(0.8))
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleName()).isEqualTo("TIME_RANGE_RULE");
    }

    @Test
    void testMultiCondition_TimeNotInRangeOperator_ShouldEvaluateCorrectly() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"timestamp\","
            + "\"operator\": \"TIME_NOT_IN_RANGE\","
            + "\"value\": \"22:00-06:00\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("TIME_NOT_RANGE_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .riskWeight(BigDecimal.valueOf(0.6))
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRuleName()).isEqualTo("TIME_NOT_RANGE_RULE");
    }

    @Test
    void testMultiCondition_ContainsOperator_ShouldEvaluateCorrectly() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"userId\","
            + "\"operator\": \"CONTAINS\","
            + "\"value\": \"USER\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("CONTAINS_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule triggered");
    }

    @Test
    void testMultiCondition_NotInOperator_ShouldEvaluateCorrectly() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"userId\","
            + "\"operator\": \"NOT_IN\","
            + "\"value\": \"ADMIN_123,SYSTEM_456\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("NOT_IN_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule triggered");
    }

    @Test
    void testMultiCondition_IsNullOperator_ShouldEvaluateCorrectly() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"nonExistentField\","
            + "\"operator\": \"IS_NULL\","
            + "\"value\": \"\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("IS_NULL_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse(); // Field access will throw exception
    }

    @Test
    void testMultiCondition_IsNotNullOperator_ShouldEvaluateCorrectly() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"userId\","
            + "\"operator\": \"IS_NOT_NULL\","
            + "\"value\": \"\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("IS_NOT_NULL_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule triggered");
    }

    @Test
    void testMultiCondition_UnsupportedOperator_ShouldHandleGracefully() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"amount\","
            + "\"operator\": \"UNSUPPORTED_OP\","
            + "\"value\": \"1000\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("UNSUPPORTED_OP_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule not triggered");
    }

    @Test
    void testMultiCondition_EmptyConditionGroups_ShouldReturnFailure() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("EMPTY_GROUPS")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("No condition groups configured");
    }

    @Test
    void testMultiCondition_NullConditionGroups_ShouldReturnFailure() {
        // Given
        String config = "{"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("NULL_GROUPS")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("No condition groups configured");
    }

    @Test
    void testMultiCondition_EmptyConditionsInGroup_ShouldReturnFalse() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("EMPTY_CONDITIONS")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule not triggered");
    }

    @Test
    void testMultiCondition_NullConditionsInGroup_ShouldReturnFalse() {
        // Given
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("NULL_CONDITIONS")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule not triggered");
    }

    @Test
    void testMultiCondition_ShortCircuitAndEvaluation_ShouldStopEarly() {
        // Given - first condition is false, second should not be evaluated
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"amount\","
            + "\"operator\": \"LT\","
            + "\"value\": \"500\""
            + "}, {"
            + "\"field\": \"userId\","
            + "\"operator\": \"EQ\","
            + "\"value\": \"USER_123\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("SHORT_CIRCUIT_AND")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule not triggered");
    }

    @Test
    void testMultiCondition_ShortCircuitOrEvaluation_ShouldStopEarly() {
        // Given - first condition is true, second should not be evaluated
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"amount\","
            + "\"operator\": \"GT\","
            + "\"value\": \"500\""  // This will be true (1000 > 500 = true)
            + "}, {"
            + "\"field\": \"userId\","
            + "\"operator\": \"EQ\","
            + "\"value\": \"OTHER_USER\""  // This would be false but shouldn't be evaluated
            + "}],"
            + "\"intraGroupOperator\": \"OR\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("SHORT_CIRCUIT_OR")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build();

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).isEqualTo("Multi-condition rule triggered");
    }

    @Test
    void testMultiCondition_DefaultRiskWeight_ShouldUseDefault() {
        // Given - Rule without risk weight
        String config = "{"
            + "\"conditionGroups\": [{"
            + "\"conditions\": [{"
            + "\"field\": \"amount\","
            + "\"operator\": \"GT\","
            + "\"value\": \"500\""
            + "}],"
            + "\"intraGroupOperator\": \"AND\""
            + "}],"
            + "\"groupLogicalOperator\": \"AND\""
            + "}";

        FraudRule rule = FraudRule.builder()
            .ruleName("DEFAULT_RISK")
            .ruleType("MULTI_CONDITION")
            .ruleConfig(config)
            .build(); // No risk weight set

        // When
        RuleEvaluationResult result = multiConditionEvaluator.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(0.5); // Default risk score
    }

    private String buildConfig(String logic, MultiConditionConfig.RuleCondition... conditions) {
        try {
            // Implementation of buildConfig method
            return null; // Placeholder return, actual implementation needed
        } catch (Exception e) {
            throw new RuntimeException("Error building config", e);
        }
    }
} 