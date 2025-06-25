package com.faud.frauddetection.dto;

import java.time.LocalDateTime;

public class FraudDetectionResult {

    private String transactionId;
    private boolean isFraud;
    private double riskScore;
    private String reason;
    private LocalDateTime detectionTimestamp;
    private long processingTime;

    public FraudDetectionResult() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isFraud() {
        return isFraud;
    }

    public void setFraud(boolean fraud) {
        isFraud = fraud;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getDetectionTimestamp() {
        return detectionTimestamp;
    }

    public void setDetectionTimestamp(LocalDateTime detectionTimestamp) {
        this.detectionTimestamp = detectionTimestamp;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
} 