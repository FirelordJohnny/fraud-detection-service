package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Simplified tests for DynamicEvaluator focusing on core functionality
 */
@ExtendWith(MockitoExtension.class)
class DynamicEvaluatorTest {

    private DynamicEvaluator ruleEngine;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        MultiConditionEvaluator multiConditionEvaluator = new MultiConditionEvaluator();
        ruleEngine = new DynamicEvaluator(multiConditionEvaluator);
        
        testTransaction = Transaction.builder()
                .transactionId("TXN_001")
                .userId("USER_123")
                .amount(new BigDecimal("15000.00"))
                .currency("USD")
                .ipAddress("192.168.1.100")
                .timestamp(LocalDateTime.now())
                .country("US")
                .paymentMethod("CREDIT_CARD")
                .build();
    }

    // Core functionality tests
    @Test
    void testSupports_SupportedTypes_ShouldReturnTrue() {
        // Test a few key supported types
        assertThat(ruleEngine.supports("SIMPLE")).isTrue();
        assertThat(ruleEngine.supports("FIELD_CONDITION")).isTrue();
        assertThat(ruleEngine.supports("SINGLE_AMOUNT")).isTrue();
        assertThat(ruleEngine.supports("TIME_OF_DAY")).isTrue();
    }

    @Test
    void testSupports_UnsupportedType_ShouldReturnFalse() {
        assertThat(ruleEngine.supports("FREQUENCY")).isFalse();
        assertThat(ruleEngine.supports("AMOUNT")).isFalse();
    }

    @Test
    void testSupports_NullRuleType_ShouldReturnFalse() {
        assertThat(ruleEngine.supports(null)).isFalse();
    }

    // Operator tests - focus on functionality, not message format
    @Test
    void testEvaluateRule_GTOperator_ShouldWork() {
        FraudRule rule = createFieldComparisonRule("HIGH_AMOUNT", "amount", "GT", "10000");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
        assertThat(result.getReason()).isNotEmpty();
    }

    @Test
    void testEvaluateRule_LTOperator_ShouldWork() {
        FraudRule rule = createFieldComparisonRule("HIGH_AMOUNT", "amount", "LT", "20000");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    @Test
    void testEvaluateRule_EQOperator_ShouldWork() {
        FraudRule rule = createFieldComparisonRule("EXACT_AMOUNT", "amount", "EQ", "15000.00");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    @Test
    void testEvaluateRule_INOperator_ShouldWork() {
        FraudRule rule = createFieldComparisonRule("CURRENCY_CHECK", "currency", "IN", "USD,EUR,GBP");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    @Test
    void testEvaluateRule_CONTAINSOperator_ShouldWork() {
        FraudRule rule = createFieldComparisonRule("IP_CHECK", "ipAddress", "CONTAINS", "192.168");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    @Test
    void testEvaluateRule_TimeInRange_ShouldWork() {
        Transaction nightTransaction = createTransactionAtTime(LocalTime.of(2, 0));
        FraudRule rule = createFieldComparisonRule("NIGHT_HOURS", "timestamp", "TIME_IN_RANGE", "22:00-06:00");
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, nightTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    // Error handling tests - focus on graceful failure
    @Test
    void testEvaluateRule_InvalidField_ShouldHandleGracefully() {
        FraudRule rule = createFieldComparisonRule("INVALID_FIELD", "invalidField", "EQ", "test");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("error");
    }

    @Test
    void testEvaluateRule_NullTransaction_ShouldHandleGracefully() {
        FraudRule rule = createFieldComparisonRule("NULL_TXN", "amount", "GT", "500");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, null);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("Rule evaluation error");
    }

    @Test
    void testEvaluateRule_InvalidConfig_ShouldHandleGracefully() {
        FraudRule rule = FraudRule.builder()
            .ruleName("INVALID_CONFIG")
            .ruleType("SIMPLE")
            .conditionField(null)  // Missing required field
            .conditionOperator(null)  // Missing required operator
            .build();
        
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).isNotEmpty();
    }

    // Performance and edge cases
    @Test
    void testEvaluateRule_WhenConditionNotMet_ShouldNotTrigger() {
        FraudRule rule = createFieldComparisonRule("LOW_AMOUNT", "amount", "GT", "20000");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
    }

    @Test
    void testEvaluateRule_CaseInsensitiveOperator_ShouldWork() {
        FraudRule rule = createFieldComparisonRule("CASE_TEST", "amount", "gt", "10000");  // lowercase
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0);
    }

    @Test
    void testEvaluateRule_ValidTransactionIdField_ShouldEvaluateCorrectly() {
        FraudRule rule = createDynamicRule("TXN_ID_CHECK", "transactionId", "EQ", "TXN_001");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_ValidTimestampField_ShouldEvaluateCorrectly() {
        FraudRule rule = createDynamicRule("TIMESTAMP_CHECK", "timestamp", "IS_NOT_NULL", "");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_CompareTo_LongComparison() {
        FraudRule rule = createDynamicRule("AMOUNT_LONG", "amount", "EQ", "15000.00");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_CompareTo_DoubleComparison() {
        FraudRule rule = createDynamicRule("AMOUNT_DOUBLE", "amount", "GT", "999.99");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_CompareTo_BigDecimalComparison() {
        FraudRule rule = createDynamicRule("AMOUNT_DECIMAL", "amount", "LTE", "20000.00");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_CompareTo_StringComparison() {
        FraudRule rule = createDynamicRule("USER_STRING", "userId", "GT", "USER_100");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_UnsupportedOperator_ShouldReturnFalse() {
        FraudRule rule = createDynamicRule("UNSUPPORTED", "amount", "UNKNOWN_OP", "1000");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("not met");
    }

    @Test
    void testEvaluateRule_IN_Operator_WithMatch() {
        FraudRule rule = createDynamicRule("IN_MATCH", "userId", "IN", "USER_123,USER_456,USER_789");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_IN_Operator_WithoutMatch() {
        FraudRule rule = createDynamicRule("IN_NO_MATCH", "userId", "IN", "USER_456,USER_789");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("not met");
    }

    @Test
    void testEvaluateRule_NOT_IN_Operator_WithMatch() {
        FraudRule rule = createDynamicRule("NOT_IN_MATCH", "userId", "NOT_IN", "USER_456,USER_789");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_CONTAINS_Operator_WithMatch() {
        FraudRule rule = createDynamicRule("CONTAINS_MATCH", "userId", "CONTAINS", "USER");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_CONTAINS_Operator_WithoutMatch() {
        FraudRule rule = createDynamicRule("CONTAINS_NO_MATCH", "userId", "CONTAINS", "ADMIN");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("not met");
    }

    @Test
    void testEvaluateRule_IS_NULL_Operator_WithNullValue() {
        // Create transaction with null currency
        Transaction nullFieldTransaction = Transaction.builder()
                .transactionId("TXN_NULL")
                .userId("USER_123")
                .amount(BigDecimal.valueOf(1000))
                .timestamp(LocalDateTime.now())
                // currency is null
                .build();
                
        FraudRule rule = createDynamicRule("NULL_CHECK", "currency", "IS_NULL", "");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, nullFieldTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_IS_NOT_NULL_Operator_WithValue() {
        FraudRule rule = createDynamicRule("NOT_NULL_CHECK", "userId", "IS_NOT_NULL", "");
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_NullRuleConfig_ShouldCreateDefaultConfig() {
        FraudRule rule = FraudRule.builder()
            .ruleName("NULL_CONFIG")
            .ruleType("DYNAMIC")
            .build();
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("No valid rule configuration");
    }

    @Test
    void testEvaluateRule_EmptyRuleConfig_ShouldCreateDefaultConfig() {
        FraudRule rule = FraudRule.builder()
            .ruleName("EMPTY_CONFIG")
            .ruleType("DYNAMIC")
            .ruleConfig("")
            .build();
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);
        
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("No valid rule configuration");
    }

    @Test
    void testEvaluateRule_InvalidJSON_ShouldHandleGracefully() {
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
    void testEvaluateRule_TimeInRange_DaytimeRange_ShouldWork() {
        // Given
        FraudRule rule = createDynamicRule("TIME_RANGE", "timestamp", "TIME_IN_RANGE", "09:00-17:00");
        
        Transaction dayTransaction = Transaction.builder()
                .transactionId("TXN_DAY")
                .userId("USER_123")
                .amount(BigDecimal.valueOf(1000))
                .timestamp(LocalDateTime.of(2023, 6, 15, 14, 30)) // 2:30 PM
                .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, dayTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("Field timestamp TIME_IN_RANGE 09:00-17:00 condition met");
    }

    @Test
    void testEvaluateRule_TimeInRange_OvernightRange_ShouldWork() {
        // Given
        FraudRule rule = createDynamicRule("NIGHT_TIME", "timestamp", "TIME_IN_RANGE", "22:00-06:00");
        
        Transaction nightTransaction = Transaction.builder()
                .transactionId("TXN_NIGHT")
                .userId("USER_123")
                .amount(BigDecimal.valueOf(1000))
                .timestamp(LocalDateTime.of(2023, 6, 15, 23, 30)) // 11:30 PM
                .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, nightTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
    }

    @Test
    void testEvaluateRule_TimeNotInRange_ShouldWork() {
        // Given
        FraudRule rule = createDynamicRule("NOT_NIGHT", "timestamp", "TIME_NOT_IN_RANGE", "22:00-06:00");
        
        Transaction dayTransaction = Transaction.builder()
                .transactionId("TXN_DAY")
                .userId("USER_123")
                .amount(BigDecimal.valueOf(1000))
                .timestamp(LocalDateTime.of(2023, 6, 15, 14, 30)) // 2:30 PM
                .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, dayTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
    }

    @Test
    void testEvaluateRule_InvalidTimeRange_ShouldReturnFalse() {
        // Given
        FraudRule rule = createDynamicRule("INVALID_TIME", "timestamp", "TIME_IN_RANGE", "invalid-format");

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    void testEvaluateRule_GTE_Operator_ShouldWork() {
        // Given
        FraudRule rule = createDynamicRule("GTE_TEST", "amount", "GTE", "1000");

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("Field amount GTE 1000 condition met");
    }

    @Test
    void testEvaluateRule_LTE_Operator_ShouldWork() {
        // Given
        FraudRule rule = createDynamicRule("LTE_TEST", "amount", "LTE", "20000");

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("condition met");
    }

    @Test
    void testEvaluateRule_NE_Operator_ShouldWork() {
        // Given  
        FraudRule rule = createDynamicRule("NE_TEST", "userId", "NE", "OTHER_USER");

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getReason()).contains("Field userId NE OTHER_USER condition met");
    }

    @Test
    void testEvaluateRule_NumberComparison_Integer_ShouldWork() {
        // Given - test with Integer value comparison
        FraudRule rule = createDynamicRule("INT_TEST", "amount", "GT", "500");
        
        Transaction intTransaction = Transaction.builder()
                .transactionId("TXN_INT")
                .userId("USER_123")
                .amount(BigDecimal.valueOf(1000)) // This will be treated as BigDecimal
                .timestamp(LocalDateTime.now())
                .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, intTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
    }

    @Test
    void testEvaluateRule_StringWithSpaces_InOperator_ShouldWork() {
        // Given
        FraudRule rule = createDynamicRule("SPACES_TEST", "userId", "IN", " USER_123 , USER_456 , USER_789 ");

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
    }

    @Test
    void testEvaluateRule_ReflectionException_ShouldHandleGracefully() {
        // Given - using invalid field name
        FraudRule rule = createDynamicRule("INVALID_FIELD", "nonExistentField", "EQ", "value");

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getReason()).contains("Field condition evaluation error");
    }

    @Test
    void testSupports_AllSupportedTypes_ShouldReturnTrue() {
        // Test all supported types
        assertThat(ruleEngine.supports("SIMPLE")).isTrue();
        assertThat(ruleEngine.supports("GENERIC")).isTrue();
        assertThat(ruleEngine.supports("FIELD_CONDITION")).isTrue();
        assertThat(ruleEngine.supports("TIME_OF_DAY")).isTrue();
        assertThat(ruleEngine.supports("IP_BLACKLIST")).isTrue();
        assertThat(ruleEngine.supports("IP_WHITELIST")).isTrue();
        assertThat(ruleEngine.supports("LOCATION")).isTrue();
        assertThat(ruleEngine.supports("DEVICE")).isTrue();
        assertThat(ruleEngine.supports("MULTI_CONDITION")).isTrue();
        assertThat(ruleEngine.supports("COMPLEX_CONDITION")).isTrue();
        
        // Test case insensitive
        assertThat(ruleEngine.supports("simple")).isTrue();
        assertThat(ruleEngine.supports("Generic")).isTrue();
        
        // Test unsupported type
        assertThat(ruleEngine.supports("UNSUPPORTED_TYPE")).isFalse();
        assertThat(ruleEngine.supports("UNKNOWN")).isFalse();
    }

    @Test
    void testEvaluateRule_TimeParsingError_ShouldHandleGracefully() {
        // Given
        FraudRule rule = createDynamicRule("TIME_ERROR", "userId", "TIME_IN_RANGE", "09:00-17:00");

        // When - userId is not a time value
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
    }

    @Test
    void testEvaluateRule_DefaultRiskWeight_ShouldUse0Point5() {
        // Given - rule without risk weight
        FraudRule rule = FraudRule.builder()
            .ruleName("DEFAULT_RISK")
            .ruleType("DYNAMIC")
            .conditionField("amount")
            .conditionOperator("GT")
            .conditionValue("500")
            // No risk weight set
            .build();

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(0.5); // Default risk weight
    }

    @Test
    void testEvaluateRule_FalseCondition_ShouldHaveZeroRiskScore() {
        // Given
        FraudRule rule = createDynamicRule("FALSE_CONDITION", "amount", "LT", "500");

        // When
        RuleEvaluationResult result = ruleEngine.evaluateRule(rule, testTransaction);

        // Then
        assertThat(result.isTriggered()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).isEqualTo("Condition not met");
    }

    // Helper methods
    private FraudRule createFieldComparisonRule(String ruleName, String field, String operator, String value) {
        return FraudRule.builder()
            .ruleName(ruleName)
            .ruleType("SIMPLE")
            .conditionField(field)
            .conditionOperator(operator)
            .conditionValue(value)
            .riskWeight(BigDecimal.valueOf(0.5))
            .build();
    }

    private Transaction createTransactionAtTime(LocalTime time) {
        return Transaction.builder()
                .transactionId("TXN_TIME_TEST")
                .userId("USER_TIME")
                .amount(BigDecimal.valueOf(1000))
                .currency("USD")
                .ipAddress("192.168.1.100")
                .timestamp(LocalDateTime.of(2025, 1, 1, time.getHour(), time.getMinute()))
                .build();
    }

    private FraudRule createDynamicRule(String ruleName, String field, String operator, String value) {
        return FraudRule.builder()
            .ruleName(ruleName)
            .ruleType("DYNAMIC")
            .conditionField(field)
            .conditionOperator(operator)
            .conditionValue(value)
            .riskWeight(BigDecimal.valueOf(0.5))
            .build();
    }
} 