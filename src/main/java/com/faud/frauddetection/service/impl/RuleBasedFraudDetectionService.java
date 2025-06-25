package com.faud.frauddetection.service.impl;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.repository.FraudRuleRepository;
import com.faud.frauddetection.service.FraudDetectionResultService;
import com.faud.frauddetection.service.FraudDetectionService;
import com.faud.frauddetection.service.evaluator.RuleEngine;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.faud.frauddetection.dto.Transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based fraud detection service implementation using dynamic rule engine
 * Supports fully configurable rule management, allowing new rules to be added without code changes
 */
@Service
@Slf4j
public class RuleBasedFraudDetectionService implements FraudDetectionService {

    private final FraudRuleRepository fraudRuleRepository;
    private final RuleEngine ruleEngine;
    private final FraudDetectionResultService resultService;
    private final double fraudThreshold;

    // Constructor for Spring Boot with @Value injection
    public RuleBasedFraudDetectionService(
            FraudRuleRepository fraudRuleRepository, 
            RuleEngine ruleEngine,
            FraudDetectionResultService resultService,
            @Value("${fraud.detection.fraud-threshold:0.3}") double fraudThreshold) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.ruleEngine = ruleEngine;
        this.resultService = resultService;
        this.fraudThreshold = fraudThreshold;
    }

    // Constructor for testing without Spring context
    public RuleBasedFraudDetectionService(
            FraudRuleRepository fraudRuleRepository, 
            RuleEngine ruleEngine,
            FraudDetectionResultService resultService) {
        this(fraudRuleRepository, ruleEngine, resultService, 0.3);
    }

    @Override
    public FraudDetectionResult detectFraud(Transaction transaction) {
        log.info("Starting fraud detection for transaction ID: {}", transaction.getTransactionId());
        long startTime = System.currentTimeMillis();

        // Get all enabled rules
        List<FraudRule> activeRules = fraudRuleRepository.findAllEnabled();
        log.debug("Found {} enabled rules", activeRules.size());

        // Evaluate all rules
        List<RuleEvaluationResult> evaluationResults = new ArrayList<>();
        double totalRiskScore = 0.0;
        
        for (FraudRule rule : activeRules) {
            if (!ruleEngine.supports(rule.getRuleType())) {
                log.warn("Unsupported rule type: {}, skipping rule: {}", rule.getRuleType(), rule.getRuleName());
                continue;
            }
            
            try {
                RuleEvaluationResult result = ruleEngine.evaluateRule(rule, transaction);
                evaluationResults.add(result);
                
                if (result.isTriggered()) {
                    totalRiskScore += result.getRiskScore();
                    log.info("Rule triggered: {} - {}", rule.getRuleName(), result.getReason());
                }
            } catch (Exception e) {
                log.error("Error evaluating rule {}: {}", rule.getRuleName(), e.getMessage(), e);
                // Continue with other rules
            }
        }

        // Calculate final risk score and build result
        long processingTime = System.currentTimeMillis() - startTime;
        double finalRiskScore = Math.min(totalRiskScore, 1.0);
        boolean isFraud = finalRiskScore > fraudThreshold;

        // Build triggered rule descriptions
        List<String> triggeredReasons = evaluationResults.stream()
            .filter(RuleEvaluationResult::isTriggered)
            .map(RuleEvaluationResult::getReason)
            .toList();

        FraudDetectionResult result = new FraudDetectionResult();
        result.setTransactionId(transaction.getTransactionId());
        result.setFraud(isFraud);
        result.setRiskScore(finalRiskScore);
        result.setReason(String.join("; ", triggeredReasons));
        result.setDetectionTimestamp(LocalDateTime.now());
        result.setProcessingTime(processingTime);

        // Record detailed evaluation results for audit
        StringBuilder detailLog = new StringBuilder();
        detailLog.append(String.format("Transaction ID: %s, Is Fraud: %s, Risk Score: %.3f, Processing Time: %dms", 
            transaction.getTransactionId(), result.isFraud(), result.getRiskScore(), result.getProcessingTime()));
        
        for (RuleEvaluationResult evalResult : evaluationResults) {
            detailLog.append(String.format("\n  Rule: %s, Triggered: %s, Score: %.3f, Reason: %s", 
                evalResult.getRuleName(), evalResult.isTriggered(), 
                evalResult.getRiskScore(), evalResult.getReason()));
        }
        
        log.info("Fraud detection completed: {}", detailLog.toString());

        // Save detection result
        try {
            resultService.saveResult(result);
        } catch (Exception e) {
            log.error("Failed to save detection result: {}", e.getMessage(), e);
        }

        return result;
    }
} 