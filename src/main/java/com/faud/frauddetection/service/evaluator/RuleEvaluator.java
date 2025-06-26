package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.entity.FraudRule;

/**
 * Rule evaluator interface
 * Supports configuration-driven rule evaluation, allowing new rules to be added without code changes
 */
public interface RuleEvaluator {
    
    /**
     * Evaluate whether a single rule is triggered
     * @param rule rule configuration
     * @param transaction transaction data
     * @return rule evaluation result
     */
    RuleEvaluationResult evaluateRule(FraudRule rule, Transaction transaction);
    
    /**
     * Check if the rule evaluator supports a specific type of rule
     * @param ruleType rule type
     * @return whether supported
     */
    boolean supports(String ruleType);
} 