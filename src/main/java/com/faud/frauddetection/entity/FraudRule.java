package com.faud.frauddetection.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fraud Detection Rule Entity
 * Supports fully configurable rule definitions, allowing new rules to be added without code changes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRule {
    
    private Long id;
    
    /**
     * Rule name (unique identifier)
     */
    private String ruleName;
    
    /**
     * Rule type: AMOUNT, FREQUENCY, TIME_OF_DAY, IP_BLACKLIST, CUSTOM
     */
    private String ruleType;
    
    /**
     * Rule description
     */
    private String description;
    
    /**
     * Extended configuration (JSON format) for complex rules
     */
    private String ruleConfig;
    
    /**
     * Whether enabled
     */
    private Boolean enabled = true;
    
    /**
     * Threshold value
     */
    private BigDecimal thresholdValue;
    
    /**
     * Condition field (for generic rules)
     */
    private String conditionField;
    
    /**
     * Condition operator (>, <, =, !=, IN, NOT_IN, etc.)
     */
    private String conditionOperator;
    
    /**
     * Condition value
     */
    private String conditionValue;
    
    /**
     * Risk weight (0.0 - 1.0)
     */
    private BigDecimal riskWeight;
    
    /**
     * Rule priority
     */
    private Integer priority;
    
    /**
     * Created time
     */
    private LocalDateTime createdAt;
    
    /**
     * Updated time
     */
    private LocalDateTime updatedAt;
    
    /**
     * Set timestamps on create
     */
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        
        // Set default values if not specified
        if (enabled == null) {
            enabled = true;
        }
        if (riskWeight == null) {
            riskWeight = BigDecimal.valueOf(0.2);
        }
        if (priority == null) {
            priority = 1;
        }
    }
    
    /**
     * Set timestamps on update
     */
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}