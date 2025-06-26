package com.faud.frauddetection.constant;

/**
 * Constants for alert severity levels and thresholds
 * Centralizes severity classification logic
 */
public final class AlertSeverity {
    
    private AlertSeverity() {
        // Utility class - prevent instantiation
    }
    
    // Severity Levels
    public static final String CRITICAL = "CRITICAL";
    public static final String HIGH = "HIGH";
    public static final String MEDIUM = "MEDIUM";
    public static final String LOW = "LOW";
    
    // Risk Score Thresholds
    public static final double CRITICAL_THRESHOLD = 0.8;
    public static final double HIGH_THRESHOLD = 0.6;
    public static final double MEDIUM_THRESHOLD = 0.4;
    
    /**
     * Determine alert severity based on risk score
     * @param riskScore the risk score to evaluate
     * @return severity level string
     */
    public static String getSeverity(double riskScore) {
        if (riskScore >= CRITICAL_THRESHOLD) return CRITICAL;
        if (riskScore >= HIGH_THRESHOLD) return HIGH;
        if (riskScore >= MEDIUM_THRESHOLD) return MEDIUM;
        return LOW;
    }
} 