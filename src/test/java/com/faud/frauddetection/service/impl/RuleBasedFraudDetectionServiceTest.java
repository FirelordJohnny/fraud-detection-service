package com.faud.frauddetection.service.impl;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.service.FraudDetectionResultService;
import com.faud.frauddetection.repository.FraudRuleRepository;
import com.faud.frauddetection.service.evaluator.RuleEngine;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test cases for RuleBasedFraudDetectionService with Dynamic Rule Engine
 */
@ExtendWith(MockitoExtension.class)
class RuleBasedFraudDetectionServiceTest {

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @Mock
    private FraudDetectionResultService fraudDetectionResultService;

    @Mock
    private RuleEngine ruleEngine;

    private RuleBasedFraudDetectionService fraudDetectionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        fraudDetectionService = new RuleBasedFraudDetectionService(
            fraudRuleRepository, 
            ruleEngine,
            fraudDetectionResultService
        );

        testTransaction = new Transaction();
        testTransaction.setTransactionId("TXN_001");
        testTransaction.setUserId("USER_123");
        testTransaction.setAmount(BigDecimal.valueOf(15000));
        testTransaction.setCurrency("USD");
        testTransaction.setIpAddress("192.168.1.100");
        testTransaction.setTimestamp(LocalDateTime.now());
    }

    @Test
    void detectFraud_NoRulesConfigured_ShouldReturnNotFraud() {
        // Given
        when(fraudRuleRepository.findAllEnabled()).thenReturn(Collections.emptyList());

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getTransactionId()).isEqualTo("TXN_001");
        assertThat(result.getReason()).isEmpty();

        verify(fraudDetectionResultService).saveResult(any());
    }

    @Test
    void detectFraud_SingleRuleTriggered_AboveThreshold_ShouldReturnFraud() {
        // Given
        FraudRule amountRule = FraudRule.builder()
            .id(1L)
            .ruleName("HIGH_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .enabled(true)
            .thresholdValue(BigDecimal.valueOf(10000))
            .riskWeight(BigDecimal.valueOf(0.5))
            .build();

        when(fraudRuleRepository.findAllEnabled()).thenReturn(Arrays.asList(amountRule));
        when(ruleEngine.supports("AMOUNT")).thenReturn(true);
        when(ruleEngine.evaluateRule(amountRule, testTransaction))
            .thenReturn(RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.5)
                .ruleName("HIGH_AMOUNT_RULE")
                .reason("Transaction amount 15000.00 exceeds threshold 10000.00")
                .build());

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(0.5);
        assertThat(result.getTransactionId()).isEqualTo("TXN_001");
        assertThat(result.getReason()).contains("Transaction amount 15000.00 exceeds threshold 10000.00");

        verify(fraudDetectionResultService).saveResult(any());
    }

    @Test
    void detectFraud_MultipleRulesTriggered_CombinedRiskScore_ShouldReturnFraud() {
        // Given
        FraudRule amountRule = FraudRule.builder()
            .id(1L)
            .ruleName("HIGH_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.2))
            .build();

        FraudRule timeRule = FraudRule.builder()
            .id(2L)
            .ruleName("NIGHT_TIME_RULE")
            .ruleType("TIME_OF_DAY")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.15))
            .build();

        when(fraudRuleRepository.findAllEnabled()).thenReturn(Arrays.asList(amountRule, timeRule));
        when(ruleEngine.supports("AMOUNT")).thenReturn(true);
        when(ruleEngine.supports("TIME_OF_DAY")).thenReturn(true);

        // Mock rule evaluations - both triggered
        when(ruleEngine.evaluateRule(amountRule, testTransaction))
            .thenReturn(RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.2)
                .ruleName("HIGH_AMOUNT_RULE")
                .reason("Amount rule triggered")
                .build());

        when(ruleEngine.evaluateRule(timeRule, testTransaction))
            .thenReturn(RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.15)
                .ruleName("NIGHT_TIME_RULE")
                .reason("Night time rule triggered")
                .build());

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isTrue(); // 0.35 > 0.3 threshold
        assertThat(result.getRiskScore()).isEqualTo(0.35);
        assertThat(result.getReason()).contains("Amount rule triggered", "Night time rule triggered");

        verify(fraudDetectionResultService).saveResult(any());
    }

    @Test
    void detectFraud_RulesTriggered_BelowThreshold_ShouldReturnNotFraud() {
        // Given
        FraudRule lowRiskRule = FraudRule.builder()
            .id(1L)
            .ruleName("LOW_RISK_RULE")
            .ruleType("AMOUNT")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.1))
            .build();

        when(fraudRuleRepository.findAllEnabled()).thenReturn(Arrays.asList(lowRiskRule));
        when(ruleEngine.supports("AMOUNT")).thenReturn(true);
        when(ruleEngine.evaluateRule(lowRiskRule, testTransaction))
            .thenReturn(RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.1)
                .ruleName("LOW_RISK_RULE")
                .reason("Low risk rule triggered")
                .build());

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isFalse(); // 0.1 < 0.3 threshold
        assertThat(result.getRiskScore()).isEqualTo(0.1);
        assertThat(result.getReason()).contains("Low risk rule triggered");

        verify(fraudDetectionResultService).saveResult(any());
    }

    @Test
    void detectFraud_DisabledRule_ShouldBeSkipped() {
        // Given - findAllEnabled should not return disabled rules
        when(fraudRuleRepository.findAllEnabled()).thenReturn(Collections.emptyList());

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).isEmpty();

        // Rule engine should not be called for disabled rules
        verify(ruleEngine, never()).supports(any());
        verify(ruleEngine, never()).evaluateRule(any(), any());
        verify(fraudDetectionResultService).saveResult(any());
    }

    @Test
    void detectFraud_UnsupportedRuleType_ShouldBeSkipped() {
        // Given
        FraudRule unsupportedRule = FraudRule.builder()
            .id(1L)
            .ruleName("UNSUPPORTED_RULE")
            .ruleType("UNKNOWN_TYPE")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.5))
            .build();

        when(fraudRuleRepository.findAllEnabled()).thenReturn(Arrays.asList(unsupportedRule));
        when(ruleEngine.supports("UNKNOWN_TYPE")).thenReturn(false);

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getReason()).isEmpty();

        verify(ruleEngine).supports("UNKNOWN_TYPE");
        verify(ruleEngine, never()).evaluateRule(any(), any());
        verify(fraudDetectionResultService).saveResult(any());
    }

    @Test
    void detectFraud_RuleEvaluationException_ShouldContinueWithOtherRules() {
        // Given
        FraudRule faultyRule = FraudRule.builder()
            .id(1L)
            .ruleName("FAULTY_RULE")
            .ruleType("AMOUNT")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.3))
            .build();

        FraudRule goodRule = FraudRule.builder()
            .id(2L)
            .ruleName("GOOD_RULE")
            .ruleType("TIME_OF_DAY")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.4))
            .build();

        when(fraudRuleRepository.findAllEnabled()).thenReturn(Arrays.asList(faultyRule, goodRule));
        when(ruleEngine.supports("AMOUNT")).thenReturn(true);
        when(ruleEngine.supports("TIME_OF_DAY")).thenReturn(true);

        // First rule throws exception
        when(ruleEngine.evaluateRule(faultyRule, testTransaction))
            .thenThrow(new RuntimeException("Rule evaluation failed"));

        // Second rule works fine
        when(ruleEngine.evaluateRule(goodRule, testTransaction))
            .thenReturn(RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.4)
                .ruleName("GOOD_RULE")
                .reason("Good rule triggered")
                .build());

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isTrue(); // 0.4 > 0.3 threshold
        assertThat(result.getRiskScore()).isEqualTo(0.4);
        assertThat(result.getReason()).contains("Good rule triggered");

        verify(fraudDetectionResultService).saveResult(any());
    }

    @Test
    void detectFraud_RiskScoreCapping_ShouldNotExceedOne() {
        // Given - multiple rules with high risk scores
        FraudRule rule1 = FraudRule.builder()
            .id(1L)
            .ruleName("RULE_1")
            .ruleType("AMOUNT")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.7))
            .build();

        FraudRule rule2 = FraudRule.builder()
            .id(2L)
            .ruleName("RULE_2")
            .ruleType("TIME_OF_DAY")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.8))
            .build();

        when(fraudRuleRepository.findAllEnabled()).thenReturn(Arrays.asList(rule1, rule2));
        when(ruleEngine.supports("AMOUNT")).thenReturn(true);
        when(ruleEngine.supports("TIME_OF_DAY")).thenReturn(true);

        when(ruleEngine.evaluateRule(rule1, testTransaction))
            .thenReturn(RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.7)
                .ruleName("RULE_1")
                .reason("Rule 1 triggered")
                .build());

        when(ruleEngine.evaluateRule(rule2, testTransaction))
            .thenReturn(RuleEvaluationResult.builder()
                .triggered(true)
                .riskScore(0.8)
                .ruleName("RULE_2")
                .reason("Rule 2 triggered")
                .build());

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(testTransaction);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(1.0); // Capped at 1.0, not 1.5
        assertThat(result.getReason()).contains("Rule 1 triggered", "Rule 2 triggered");

        verify(fraudDetectionResultService).saveResult(any());
    }
} 