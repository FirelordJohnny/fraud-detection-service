package com.faud.frauddetection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Fraud Rule Data Transfer Object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRuleDto {
    
    @NotBlank(message = "Rule name cannot be empty")
    private String ruleName;
    
    @NotBlank(message = "Rule type cannot be empty")
    private String ruleType;
    
    private String description;
    
    /**
     * Extended configuration (JSON format)
     */
    private String ruleConfig;
    
    @NotNull(message = "Enabled status cannot be null")
    private Boolean enabled;
    
    private BigDecimal thresholdValue;
    
    /**
     * Condition field
     */
    private String conditionField;
    
    /**
     * Condition operator
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
} 