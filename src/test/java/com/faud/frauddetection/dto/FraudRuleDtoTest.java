package com.faud.frauddetection.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class focusing on equals and hashCode method branch coverage for FraudRuleDto
 */
class FraudRuleDtoTest {

    @Test
    @DisplayName("Should return true for identical FraudRuleDto objects")
    void shouldReturnTrueForIdenticalObjects() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("HIGH_AMOUNT_RULE")
                .ruleType("AMOUNT")
                .description("High amount detection rule")
                .ruleConfig("{\"threshold\": 1000}")
                .enabled(true)
                .thresholdValue(BigDecimal.valueOf(1000))
                .conditionField("amount")
                .conditionOperator("GT")
                .conditionValue("1000")
                .riskWeight(BigDecimal.valueOf(0.8))
                .priority(1)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("HIGH_AMOUNT_RULE")
                .ruleType("AMOUNT")
                .description("High amount detection rule")
                .ruleConfig("{\"threshold\": 1000}")
                .enabled(true)
                .thresholdValue(BigDecimal.valueOf(1000))
                .conditionField("amount")
                .conditionOperator("GT")
                .conditionValue("1000")
                .riskWeight(BigDecimal.valueOf(0.8))
                .priority(1)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return true when comparing same object reference")
    void shouldReturnTrueForSameReference() {
        // Given
        FraudRuleDto dto = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .build();

        // Then
        assertEquals(dto, dto);
        assertEquals(dto.hashCode(), dto.hashCode());
    }

    @Test
    @DisplayName("Should return false when comparing with null")
    void shouldReturnFalseForNull() {
        // Given
        FraudRuleDto dto = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .build();

        // Then
        assertNotEquals(dto, null);
    }

    @Test
    @DisplayName("Should return false when comparing with different class")
    void shouldReturnFalseForDifferentClass() {
        // Given
        FraudRuleDto dto = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .build();

        // Then
        assertNotEquals(dto, "string");
        assertNotEquals(dto, 123);
    }

    @Test
    @DisplayName("Should return false when ruleName differs")
    void shouldReturnFalseForDifferentRuleName() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("Rule 1")
                .ruleType("TEST")
                .enabled(true)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("Rule 2")
                .ruleType("TEST")
                .enabled(true)
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when ruleType differs")
    void shouldReturnFalseForDifferentRuleType() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("AMOUNT")
                .enabled(true)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("FREQUENCY")
                .enabled(true)
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when description differs")
    void shouldReturnFalseForDifferentDescription() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .description("Description 1")
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .description("Description 2")
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should handle null description in equals")
    void shouldHandleNullDescription() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .description(null)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .description(null)
                .build();

        FraudRuleDto dto3 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .description("Test Description")
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should return false when ruleConfig differs")
    void shouldReturnFalseForDifferentRuleConfig() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .ruleConfig("{\"config\": \"value1\"}")
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .ruleConfig("{\"config\": \"value2\"}")
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when enabled differs")
    void shouldReturnFalseForDifferentEnabled() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(false)
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when thresholdValue differs")
    void shouldReturnFalseForDifferentThresholdValue() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .thresholdValue(BigDecimal.valueOf(1000))
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .thresholdValue(BigDecimal.valueOf(2000))
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should handle null thresholdValue in equals")
    void shouldHandleNullThresholdValue() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .thresholdValue(null)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .thresholdValue(null)
                .build();

        FraudRuleDto dto3 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .thresholdValue(BigDecimal.valueOf(1000))
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should return false when conditionField differs")
    void shouldReturnFalseForDifferentConditionField() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .conditionField("amount")
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .conditionField("userId")
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when conditionOperator differs")
    void shouldReturnFalseForDifferentConditionOperator() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .conditionOperator("GT")
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .conditionOperator("LT")
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when conditionValue differs")
    void shouldReturnFalseForDifferentConditionValue() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .conditionValue("1000")
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .conditionValue("2000")
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when riskWeight differs")
    void shouldReturnFalseForDifferentRiskWeight() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .riskWeight(BigDecimal.valueOf(0.5))
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .riskWeight(BigDecimal.valueOf(0.8))
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should return false when priority differs")
    void shouldReturnFalseForDifferentPriority() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .priority(1)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .priority(2)
                .build();

        // Then
        assertNotEquals(dto1, dto2);
        assertNotEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should handle null priority in equals")
    void shouldHandleNullPriority() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .priority(null)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .priority(null)
                .build();

        FraudRuleDto dto3 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .priority(1)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    @DisplayName("Should handle all null optional fields in equals")
    void shouldHandleAllNullOptionalFields() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .description(null)
                .ruleConfig(null)
                .thresholdValue(null)
                .conditionField(null)
                .conditionOperator(null)
                .conditionValue(null)
                .riskWeight(null)
                .priority(null)
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .description(null)
                .ruleConfig(null)
                .thresholdValue(null)
                .conditionField(null)
                .conditionOperator(null)
                .conditionValue(null)
                .riskWeight(null)
                .priority(null)
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("Should test BigDecimal precision in equals")
    void shouldTestBigDecimalPrecision() {
        // Given
        FraudRuleDto dto1 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .thresholdValue(BigDecimal.valueOf(1000))
                .riskWeight(BigDecimal.valueOf(0.5))
                .build();

        FraudRuleDto dto2 = FraudRuleDto.builder()
                .ruleName("TEST_RULE")
                .ruleType("TEST")
                .enabled(true)
                .thresholdValue(BigDecimal.valueOf(1000))
                .riskWeight(BigDecimal.valueOf(0.5))
                .build();

        // Then
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
} 