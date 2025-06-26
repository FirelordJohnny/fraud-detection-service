package com.faud.frauddetection.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudDetectionResult {

    private String transactionId;
    private boolean isFraudulent;
    private double riskScore;
    private String riskLevel;
    private String reason;
    private LocalDateTime detectionTime;
    private long processingTime;
    private List<RuleEvaluationResult> evaluationResults;
    
    // Convenience fields for triggered rules
    @Builder.Default
    private List<RuleEvaluationResult> triggeredRules = new java.util.ArrayList<>();
    
    // Additional validation/metadata fields
    private String evaluationStatus;
    private String alertStatus;
    
    // Legacy method names for backward compatibility
    public boolean isFraud() {
        return isFraudulent;
    }
    
    public void setFraud(boolean fraud) {
        this.isFraudulent = fraud;
    }
    
    public LocalDateTime getDetectionTimestamp() {
        return detectionTime;
    }
    
    public void setDetectionTimestamp(LocalDateTime timestamp) {
        this.detectionTime = timestamp;
    }
    
    public List<RuleEvaluationResult> getTriggeredRules() {
        return triggeredRules;
    }
} 