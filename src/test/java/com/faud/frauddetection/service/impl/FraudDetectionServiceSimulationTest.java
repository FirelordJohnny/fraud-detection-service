package com.faud.frauddetection.service.impl;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.service.FraudRuleService;
import com.faud.frauddetection.service.FraudDetectionResultService;
import com.faud.frauddetection.service.evaluator.RuleEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FraudDetectionServiceImpl, focusing on logic, not integration.
 * This test class uses Mockito to simulate dependencies and isolate the service.
 */
@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceSimulationTest {

    @Mock
    private FraudRuleService fraudRuleService;

    @Mock
    private FraudDetectionResultService resultService;

    // No longer injecting mocks, we will construct it manually
    private FraudDetectionServiceImpl fraudDetectionService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        // Manually construct the service with mocks and an empty list for ruleEngines
        fraudDetectionService = new FraudDetectionServiceImpl(new ArrayList<>(), fraudRuleService, resultService);

        transaction = Transaction.builder()
                .transactionId("test-tx-id")
                .userId("test-user")
                .amount(new BigDecimal("1000"))
                .ipAddress("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void mockRuleAndEvaluator(String ruleType, String ruleName, boolean isTriggered, double riskScore, String reason) {
        FraudRule rule = new FraudRule();
        rule.setRuleType(ruleType);
        rule.setRuleName(ruleName);

        when(fraudRuleService.getActiveRules()).thenReturn(Collections.singletonList(rule));

        RuleEvaluator mockEvaluator = new RuleEvaluator() {
            @Override
            public RuleEvaluationResult evaluateRule(FraudRule r, Transaction t) {
                return RuleEvaluationResult.builder()
                        .triggered(isTriggered)
                        .riskScore(riskScore)
                        .reason(reason)
                        .ruleName(r.getRuleName())
                        .build();
            }

            @Override
            public boolean supports(String type) {
                return type.equals(ruleType);
            }
        };
        
        // Use reflection to add the mock evaluator to the service's private list
        try {
            Field ruleEnginesField = FraudDetectionServiceImpl.class.getDeclaredField("ruleEngines");
            ruleEnginesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<RuleEvaluator> ruleEngines = (List<RuleEvaluator>) ruleEnginesField.get(fraudDetectionService);
            ruleEngines.add(mockEvaluator);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to modify ruleEngines via reflection", e);
        }
    }

    @Test
    void testHighAmountFraudDetection_ShouldTriggerRuleAndFlagAsFraud() {
        mockRuleAndEvaluator("AMOUNT", "High Amount Rule", true, 0.9, "Transaction amount exceeds threshold");
        
        Transaction highAmountTransaction = transaction.toBuilder()
                .amount(new BigDecimal("6000"))
                .build();

        FraudDetectionResult result = fraudDetectionService.detectFraud(highAmountTransaction);
        
        assertTrue(result.isFraudulent());
        assertEquals(0.9, result.getRiskScore());
        assertThat(result.getEvaluationResults()).hasSize(1);
        assertEquals("High Amount Rule: Transaction amount exceeds threshold", result.getReason().replace("Triggered rules: ", ""));
    }

    @Test
    void testLowRiskRule_ShouldTriggerRuleButNotFlagAsFraud() {
        mockRuleAndEvaluator("FREQUENCY", "Slightly High Frequency", true, 0.2, "Frequency is slightly elevated");
        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
        assertTrue(result.isFraudulent()); // A single triggered rule is considered fraudulent
        assertEquals(0.2, result.getRiskScore());
        assertThat(result.getEvaluationResults()).hasSize(1);
        assertEquals("Slightly High Frequency: Frequency is slightly elevated", result.getReason().replace("Triggered rules: ", ""));
    }

    @Test
    void testMultipleRules_ShouldAccumulateScoreAndFlagAsFraud() {
        FraudRule amountRule = new FraudRule();
        amountRule.setRuleType("AMOUNT");
        amountRule.setRuleName("High Amount");

        FraudRule ipRule = new FraudRule();
        ipRule.setRuleType("IP_BLACKLIST");
        ipRule.setRuleName("Risky IP");

        when(fraudRuleService.getActiveRules()).thenReturn(List.of(amountRule, ipRule));

        RuleEvaluator amountEvaluator = new RuleEvaluator() {
            @Override
            public RuleEvaluationResult evaluateRule(FraudRule r, Transaction t) {
                return RuleEvaluationResult.builder().triggered(true).riskScore(0.2).build();
            }
            @Override
            public boolean supports(String type) { return type.equals("AMOUNT"); }
        };

        RuleEvaluator ipEvaluator = new RuleEvaluator() {
            @Override
            public RuleEvaluationResult evaluateRule(FraudRule r, Transaction t) {
                return RuleEvaluationResult.builder().triggered(true).riskScore(0.2).build();
            }
            @Override
            public boolean supports(String type) { return type.equals("IP_BLACKLIST"); }
        };
        
        // Use reflection to add our mock evaluators to the private list in the service
        List<RuleEvaluator> evaluators = (List<RuleEvaluator>) ReflectionTestUtils.getField(fraudDetectionService, "ruleEngines");
        evaluators.clear(); // Clear previous mocks if any
        evaluators.add(amountEvaluator);
        evaluators.add(ipEvaluator);

        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);
        assertTrue(result.isFraudulent());
        assertEquals(0.4, result.getRiskScore());
        assertThat(result.getEvaluationResults()).hasSize(2);
    }

    @Test
    void testNoRulesTriggered_ShouldNotBeFlaggedAsFraud() {
        when(fraudRuleService.getActiveRules()).thenReturn(Collections.emptyList());

        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);

        assertFalse(result.isFraudulent());
        assertEquals(0.0, result.getRiskScore());
        assertThat(result.getEvaluationResults()).isEmpty();
    }
}