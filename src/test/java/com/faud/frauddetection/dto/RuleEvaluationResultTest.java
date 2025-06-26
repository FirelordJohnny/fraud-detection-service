package com.faud.frauddetection.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class focusing on equals and hashCode method branch coverage for RuleEvaluationResult
 */
class RuleEvaluationResultTest {

    @Test
    @DisplayName("Should return true for identical RuleEvaluationResult objects")
    void shouldReturnTrueForIdenticalObjects() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.75)
                .reason("Frequency limit exceeded")
                .ruleName("FREQUENCY_RULE")
                .actualValue("15")
                .thresholdValue("10")
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.75)
                .reason("Frequency limit exceeded")
                .ruleName("FREQUENCY_RULE")
                .actualValue("15")
                .thresholdValue("10")
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should return true when comparing same object reference")
    void shouldReturnTrueForSameReference() {
        // Given
        RuleEvaluationResult result = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .build();

        // Then
        assertEquals(result, result);
        assertEquals(result.hashCode(), result.hashCode());
    }

    @Test
    @DisplayName("Should return false when comparing with null")
    void shouldReturnFalseForNull() {
        // Given
        RuleEvaluationResult result = RuleEvaluationResult.builder()
                .triggered(true)
                .build();

        // Then
        assertNotEquals(result, null);
    }

    @Test
    @DisplayName("Should return false when comparing with different class")
    void shouldReturnFalseForDifferentClass() {
        // Given
        RuleEvaluationResult result = RuleEvaluationResult.builder()
                .triggered(true)
                .build();

        // Then
        assertNotEquals(result, "string");
        assertNotEquals(result, 123);
    }

    @Test
    @DisplayName("Should return false when triggered field differs")
    void shouldReturnFalseForDifferentTriggered() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .reason("Test")
                .ruleName("TEST_RULE")
                .actualValue("value")
                .thresholdValue("threshold")
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(false)
                .riskScore(0.5)
                .reason("Test")
                .ruleName("TEST_RULE")
                .actualValue("value")
                .thresholdValue("threshold")
                .build();

        // Then
        assertNotEquals(result1, result2);
        assertNotEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should return false when riskScore differs")
    void shouldReturnFalseForDifferentRiskScore() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.6)
                .build();

        // Then
        assertNotEquals(result1, result2);
        assertNotEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should return false when reason differs")
    void shouldReturnFalseForDifferentReason() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .reason("Reason 1")
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .reason("Reason 2")
                .build();

        // Then
        assertNotEquals(result1, result2);
        assertNotEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should handle null reason in equals")
    void shouldHandleNullReason() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .reason(null)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .reason(null)
                .build();

        RuleEvaluationResult result3 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .reason("Test")
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }

    @Test
    @DisplayName("Should return false when ruleName differs")
    void shouldReturnFalseForDifferentRuleName() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .ruleName("Rule 1")
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .ruleName("Rule 2")
                .build();

        // Then
        assertNotEquals(result1, result2);
        assertNotEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should handle null ruleName in equals")
    void shouldHandleNullRuleName() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .ruleName(null)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .ruleName(null)
                .build();

        RuleEvaluationResult result3 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .ruleName("Test Rule")
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }

    @Test
    @DisplayName("Should return false when actualValue differs")
    void shouldReturnFalseForDifferentActualValue() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .actualValue("value1")
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .actualValue("value2")
                .build();

        // Then
        assertNotEquals(result1, result2);
        assertNotEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should handle null actualValue in equals")
    void shouldHandleNullActualValue() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .actualValue(null)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .actualValue(null)
                .build();

        RuleEvaluationResult result3 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .actualValue("Test Value")
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }

    @Test
    @DisplayName("Should return false when thresholdValue differs")
    void shouldReturnFalseForDifferentThresholdValue() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .thresholdValue("threshold1")
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .thresholdValue("threshold2")
                .build();

        // Then
        assertNotEquals(result1, result2);
        assertNotEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should handle null thresholdValue in equals")
    void shouldHandleNullThresholdValue() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .thresholdValue(null)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .thresholdValue(null)
                .build();

        RuleEvaluationResult result3 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .thresholdValue("Test Threshold")
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }

    @Test
    @DisplayName("Should handle all null fields in equals")
    void shouldHandleAllNullFields() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(false)
                .riskScore(0.0)
                .reason(null)
                .ruleName(null)
                .actualValue(null)
                .thresholdValue(null)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(false)
                .riskScore(0.0)
                .reason(null)
                .ruleName(null)
                .actualValue(null)
                .thresholdValue(null)
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should test edge case with zero risk score")
    void shouldTestZeroRiskScore() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(false)
                .riskScore(0.0)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(false)
                .riskScore(0.0)
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("Should test edge case with maximum risk score")
    void shouldTestMaximumRiskScore() {
        // Given
        RuleEvaluationResult result1 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(1.0)
                .build();

        RuleEvaluationResult result2 = RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(1.0)
                .build();

        // Then
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
    }
} 