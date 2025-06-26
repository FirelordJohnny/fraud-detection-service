package com.faud.frauddetection.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Configuration properties for fraud detection system
 * Allows external configuration of business rules and thresholds
 */
@Data
@Component
@ConfigurationProperties(prefix = "fraud.detection")
public class FraudDetectionProperties {
    
    /**
     * Whether fraud detection is enabled
     */
    private boolean enabled = true;
    
    /**
     * Risk score threshold for fraud classification
     */
    private BigDecimal fraudThreshold = BigDecimal.valueOf(0.3);
    
    /**
     * Whether to enable async processing
     */
    private boolean asyncProcessing = true;
    
    /**
     * Batch size for processing
     */
    private int batchSize = 100;
    
    /**
     * Thread pool size for async processing
     */
    private int threadPoolSize = 10;
    
    /**
     * Time window configurations
     */
    private TimeWindow timeWindow = new TimeWindow();
    
    /**
     * Rule evaluation configurations
     */
    private RuleEvaluation ruleEvaluation = new RuleEvaluation();
    
    /**
     * Alert configurations
     */
    private Alert alert = new Alert();
    
    @Data
    public static class TimeWindow {
        /**
         * Default time window in seconds for frequency and amount evaluations
         */
        private long defaultSeconds = 3600L; // 1 hour
        
        /**
         * Maximum allowed time window in seconds
         */
        private long maxSeconds = 86400L; // 24 hours
        
        /**
         * Minimum allowed time window in seconds
         */
        private long minSeconds = 300L; // 5 minutes
    }
    
    @Data
    public static class RuleEvaluation {
        /**
         * Default risk weight for rules without specified weight
         */
        private BigDecimal defaultRiskWeight = BigDecimal.valueOf(0.2);
        
        /**
         * Maximum risk score (sum of all triggered rules cannot exceed this)
         */
        private BigDecimal maxRiskScore = BigDecimal.valueOf(1.0);
        
        /**
         * Minimum risk score
         */
        private BigDecimal minRiskScore = BigDecimal.valueOf(0.0);
        
        /**
         * Default priority for rules without specified priority
         */
        private int defaultPriority = 1;
        
        /**
         * Timeout for rule evaluation in milliseconds
         */
        private long evaluationTimeoutMs = 5000L;
    }
    
    @Data
    public static class Alert {
        /**
         * Whether alerts are enabled
         */
        private boolean enabled = true;
        
        /**
         * Kafka topic for fraud alerts
         */
        private String kafkaTopic = "fraud-alerts";
        
        /**
         * Alert timeout in milliseconds
         */
        private long timeoutMs = 5000L;
        
        /**
         * Whether to retry failed alerts
         */
        private boolean retryEnabled = true;
        
        /**
         * Maximum retry attempts
         */
        private int maxRetries = 3;
        
        /**
         * Severity threshold configurations
         */
        private SeverityThresholds severityThresholds = new SeverityThresholds();
        
        @Data
        public static class SeverityThresholds {
            private double critical = 0.8;
            private double high = 0.6;
            private double medium = 0.4;
        }
    }
} 