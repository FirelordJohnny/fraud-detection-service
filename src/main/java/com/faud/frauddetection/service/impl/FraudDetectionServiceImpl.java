package com.faud.frauddetection.service.impl;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.service.FraudDetectionService;
import com.faud.frauddetection.service.FraudDetectionResultService;
import com.faud.frauddetection.service.FraudRuleService;
import com.faud.frauddetection.service.evaluator.RuleEvaluator;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fraud Detection Service Implementation
 * Manages and coordinates all rule engines to evaluate transactions
 */
@Service
@Slf4j
public class FraudDetectionServiceImpl implements FraudDetectionService {
    
    private final List<RuleEvaluator> ruleEngines;
    private final FraudRuleService fraudRuleService;
    private final FraudDetectionResultService resultService;
    
    public FraudDetectionServiceImpl(
            List<RuleEvaluator> ruleEngines,
            FraudRuleService fraudRuleService,
            FraudDetectionResultService resultService) {
        this.ruleEngines = ruleEngines;
        this.fraudRuleService = fraudRuleService;
        this.resultService = resultService;
        log.info("Initialized FraudDetectionService with {} rule engines", ruleEngines.size());
        ruleEngines.forEach(engine -> log.info("Registered rule engine: {}", engine.getClass().getSimpleName()));
    }
    
    @Override
    public FraudDetectionResult detectFraud(Transaction transaction) {
        log.info("Starting fraud detection for transaction: {}", transaction.getTransactionId());
        
        try {
            // Get all active rules
            List<FraudRule> activeRules = fraudRuleService.getActiveRules();
            log.debug("Found {} active rules", activeRules.size());
            
            List<RuleEvaluationResult> evaluationResults = new ArrayList<>();
            double totalRiskScore = 0.0;
            boolean isFraudulent = false;
            
            // Evaluate each rule with the appropriate engine
            for (FraudRule rule : activeRules) {
                RuleEvaluator engine = findSupportingEngine(rule.getRuleType());
                if (engine != null) {
                    RuleEvaluationResult result = engine.evaluateRule(rule, transaction);
                    evaluationResults.add(result);
                    
                    if (result.isTriggered()) {
                        totalRiskScore += result.getRiskScore();
                        isFraudulent = true;
                        log.debug("Rule {} triggered with risk score: {}", rule.getRuleName(), result.getRiskScore());
                    }
                } else {
                    log.warn("No supporting engine found for rule type: {}", rule.getRuleType());
                }
            }
            
            // Normalize risk score (cap at 1.0)
            totalRiskScore = Math.min(totalRiskScore, 1.0);
            
            // Determine final fraud status based on risk score
            String riskLevel = determineRiskLevel(totalRiskScore);
            
            FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId(transaction.getTransactionId())
                .isFraudulent(isFraudulent)
                .riskScore(totalRiskScore)
                .riskLevel(riskLevel)
                .detectionTime(LocalDateTime.now())
                .evaluationResults(evaluationResults)
                .reason(generateSummaryReason(evaluationResults))
                .build();
            
            log.info("Fraud detection completed for transaction: {} - Result: {} (Risk: {})", 
                transaction.getTransactionId(), isFraudulent ? "FRAUD" : "NORMAL", riskLevel);
            
            // Save the result
            try {
                resultService.saveResult(result);
                log.debug("Fraud detection result saved for transaction: {}", transaction.getTransactionId());
            } catch (Exception e) {
                log.error("Failed to save fraud detection result for transaction {}: {}", 
                    transaction.getTransactionId(), e.getMessage(), e);
                // Don't fail the detection because of save error
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in fraud detection for transaction {}: {}", 
                transaction.getTransactionId(), e.getMessage(), e);
            
            // Return error result instead of throwing exception
            return FraudDetectionResult.builder()
                .transactionId(transaction.getTransactionId())
                .isFraudulent(false)
                .riskScore(0.0)
                .riskLevel("ERROR")
                .detectionTime(LocalDateTime.now())
                .reason("Detection failed: " + e.getMessage())
                .evaluationResults(new ArrayList<>())
                .build();
        }
    }
    
    /**
     * Find a rule evaluator that supports the given rule type
     */
    private RuleEvaluator findSupportingEngine(String ruleType) {
        return ruleEngines.stream()
            .filter(engine -> engine.supports(ruleType))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Determine risk level based on total risk score
     */
    private String determineRiskLevel(double riskScore) {
        if (riskScore >= 0.8) {
            return "HIGH";
        } else if (riskScore >= 0.5) {
            return "MEDIUM";
        } else if (riskScore >= 0.2) {
            return "LOW";
        } else {
            return "MINIMAL";
        }
    }
    
    /**
     * Generate a summary reason from all evaluation results
     */
    private String generateSummaryReason(List<RuleEvaluationResult> results) {
        List<String> triggeredReasons = results.stream()
            .filter(RuleEvaluationResult::isTriggered)
            .map(result -> result.getRuleName() + ": " + result.getReason())
            .collect(Collectors.toList());
        
        if (triggeredReasons.isEmpty()) {
            return "No fraud rules triggered";
        }
        
        return "Triggered rules: " + String.join("; ", triggeredReasons);
    }
    
    /**
     * Get statistics about available rule engines
     */
    public Map<String, Object> getEngineStatistics() {
        Map<String, List<String>> engineCapabilities = ruleEngines.stream()
            .collect(Collectors.toMap(
                engine -> engine.getClass().getSimpleName(),
                engine -> List.of("Check supports() method for details")
            ));
        
        return Map.of(
            "totalEngines", ruleEngines.size(),
            "engines", engineCapabilities
        );
    }
} 