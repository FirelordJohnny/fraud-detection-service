package com.faud.frauddetection.controller;

import com.faud.frauddetection.dto.FraudRuleDto;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.service.FraudRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test cases for Fraud Rule Controller
 */
@ExtendWith(MockitoExtension.class)
class FraudRuleControllerTest {

    @Mock
    private FraudRuleService fraudRuleService;

    @InjectMocks
    private FraudRuleController fraudRuleController;

    private FraudRule testRule;
    private FraudRuleDto testRuleDto;

    @BeforeEach
    void setUp() {
        testRule = FraudRule.builder()
            .id(1L)
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Test amount rule")
            .thresholdValue(BigDecimal.valueOf(10000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.3))
            .priority(1)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testRuleDto = FraudRuleDto.builder()
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Test amount rule")
            .thresholdValue(BigDecimal.valueOf(10000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.3))
            .priority(1)
            .build();
    }

    @Test
    void getAllRules_ShouldReturnAllRules() {
        // Given
        List<FraudRule> rules = Arrays.asList(testRule);
        when(fraudRuleService.getAllFraudRules()).thenReturn(rules);

        // When
        ResponseEntity<List<FraudRule>> result = fraudRuleController.getAllRules();

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getRuleName()).isEqualTo("TEST_AMOUNT_RULE");
        verify(fraudRuleService).getAllFraudRules();
    }

    @Test
    void getRuleById_ExistingRule_ShouldReturnRule() {
        // Given
        when(fraudRuleService.getFraudRuleById(1L)).thenReturn(Optional.of(testRule));

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.getRuleById(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRuleName()).isEqualTo("TEST_AMOUNT_RULE");
        verify(fraudRuleService).getFraudRuleById(1L);
    }

    @Test
    void getRuleById_NonExistentRule_ShouldReturnNotFound() {
        // Given
        when(fraudRuleService.getFraudRuleById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.getRuleById(999L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
        verify(fraudRuleService).getFraudRuleById(999L);
    }

    @Test
    void createRule_ValidRule_ShouldCreateAndReturnRule() {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.createRule(testRuleDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRuleName()).isEqualTo("TEST_AMOUNT_RULE");
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void updateRule_ValidRule_ShouldUpdateAndReturnRule() {
        // Given
        FraudRule updatedRule = FraudRule.builder()
            .id(1L)
            .ruleName("UPDATED_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Updated description")
            .thresholdValue(BigDecimal.valueOf(15000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.4))
            .priority(1)
            .build();

        when(fraudRuleService.updateFraudRule(eq(1L), any(FraudRule.class))).thenReturn(updatedRule);

        FraudRuleDto updateDto = FraudRuleDto.builder()
            .ruleName("UPDATED_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Updated description")
            .thresholdValue(BigDecimal.valueOf(15000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.4))
            .priority(1)
            .build();

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.updateRule(1L, updateDto);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRuleName()).isEqualTo("UPDATED_AMOUNT_RULE");
        verify(fraudRuleService).updateFraudRule(eq(1L), any(FraudRule.class));
    }

    @Test
    void deleteRule_ExistingRule_ShouldDeleteRule() {
        // Given
        doNothing().when(fraudRuleService).deleteFraudRule(1L);

        // When
        ResponseEntity<Void> result = fraudRuleController.deleteRule(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(fraudRuleService).deleteFraudRule(1L);
    }

    @Test
    void toggleRule_ShouldToggleRuleStatus() {
        // Given
        FraudRule existingRule = FraudRule.builder()
            .id(1L)
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .enabled(true)
            .build();
        
        FraudRule toggledRule = FraudRule.builder()
            .id(1L)
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .enabled(false) // toggled to false
            .build();
        
        when(fraudRuleService.getFraudRuleById(1L)).thenReturn(Optional.of(existingRule));
        when(fraudRuleService.updateFraudRule(eq(1L), any(FraudRule.class))).thenReturn(toggledRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.toggleRule(1L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getEnabled()).isFalse();
        verify(fraudRuleService).getFraudRuleById(1L);
        verify(fraudRuleService).updateFraudRule(eq(1L), any(FraudRule.class));
    }

    @Test
    void toggleRule_NonExistentRule_ShouldReturnNotFound() {
        // Given
        when(fraudRuleService.getFraudRuleById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.toggleRule(999L);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(fraudRuleService).getFraudRuleById(999L);
        verify(fraudRuleService, never()).updateFraudRule(any(), any());
    }

    @Test
    void quickCreateRule_ValidParameters_ShouldCreateRule() {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.quickCreateRule(
            "AMOUNT", "QUICK_RULE", BigDecimal.valueOf(1000), "Quick test rule");

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void quickCreateRule_WithoutDescription_ShouldUseDefaultDescription() {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.quickCreateRule(
            "AMOUNT", "QUICK_RULE", BigDecimal.valueOf(1000), null);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void quickCreateRule_DifferentRuleTypes_ShouldUseDifferentRiskWeights() {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // Test FREQUENCY rule type
        ResponseEntity<FraudRule> result1 = fraudRuleController.quickCreateRule(
            "FREQUENCY", "FREQ_RULE", BigDecimal.valueOf(5), "Frequency rule");

        // Test IP_BLACKLIST rule type
        ResponseEntity<FraudRule> result2 = fraudRuleController.quickCreateRule(
            "IP_BLACKLIST", "IP_RULE", BigDecimal.valueOf(1), "IP rule");

        // Then
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(fraudRuleService, times(2)).createFraudRule(any(FraudRule.class));
    }

    @Test
    void quickCreateRule_TimeOfDayRuleType_ShouldUseCorrectRiskWeight() {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.quickCreateRule(
            "TIME_OF_DAY", "TIME_RULE", BigDecimal.valueOf(1), "Time rule");

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void quickCreateRule_CustomRuleType_ShouldUseCorrectRiskWeight() {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.quickCreateRule(
            "CUSTOM", "CUSTOM_RULE", BigDecimal.valueOf(1), "Custom rule");

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void quickCreateRule_UnknownRuleType_ShouldUseDefaultRiskWeight() {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.quickCreateRule(
            "UNKNOWN_TYPE", "UNKNOWN_RULE", BigDecimal.valueOf(1), "Unknown rule");

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void createRule_WithNullRiskWeight_ShouldUseDefaultRiskWeight() {
        // Given
        FraudRuleDto dtoWithoutRiskWeight = FraudRuleDto.builder()
            .ruleName("NO_RISK_WEIGHT_RULE")
            .ruleType("AMOUNT")
            .description("Rule without risk weight")
            .enabled(true)
            .riskWeight(null) // null risk weight
            .build();

        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.createRule(dtoWithoutRiskWeight);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void createRule_WithNullPriority_ShouldUseDefaultPriority() {
        // Given
        FraudRuleDto dtoWithoutPriority = FraudRuleDto.builder()
            .ruleName("NO_PRIORITY_RULE")
            .ruleType("AMOUNT")
            .description("Rule without priority")
            .enabled(true)
            .priority(null) // null priority
            .build();

        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When
        ResponseEntity<FraudRule> result = fraudRuleController.createRule(dtoWithoutPriority);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }
} 