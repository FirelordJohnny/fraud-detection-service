package com.faud.frauddetection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rule evaluation result DTO
 * Contains the result of evaluating a fraud detection rule against a transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationResult {
    
    /**
     * Whether the rule was triggered
     */
    private boolean triggered;
    
    /**
     * Risk score (0.0 - 1.0)
     */
    private double riskScore;
    
    /**
     * Reason for triggering or not triggering
     */
    private String reason;
    
    /**
     * Name of the rule that was evaluated
     */
    private String ruleName;
    
    /**
     * Actual value that was evaluated
     */
    private String actualValue;
    
    /**
     * Threshold value used for comparison
     */
    private String thresholdValue;
} 