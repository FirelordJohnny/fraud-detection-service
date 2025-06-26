package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.entity.RuleEvaluationType;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.constant.FraudRuleOperators;
import com.faud.frauddetection.constant.FraudRuleTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Method;

/**
 * Dynamic rule evaluator implementation for simple rules
 * Handles single field comparisons (>, <, =, !=, IN, NOT_IN) and basic logic combinations
 */
@Component
@Slf4j
public class DynamicEvaluator implements RuleEvaluator {
    
    private final MultiConditionEvaluator multiConditionEvaluator;
    
    public DynamicEvaluator(MultiConditionEvaluator multiConditionEvaluator) {
        this.multiConditionEvaluator = multiConditionEvaluator;
    }
    
    @Override
    public RuleEvaluationResult evaluateRule(FraudRule rule, Transaction transaction) {
        try {
            log.debug("Evaluating dynamic rule: {} for transaction: {}", rule.getRuleName(), transaction.getTransactionId());
            
            // Select different processing methods based on evaluation type
            RuleEvaluationType evaluationType = rule.getEvaluationType();
            
            return switch (evaluationType) {
                case SINGLE_CONDITION -> evaluateFieldCondition(rule, transaction);
                case MULTI_CONDITION -> multiConditionEvaluator.evaluateRule(rule, transaction);
                case INVALID -> RuleEvaluationResult.builder()
                    .triggered(false)
                    .reason("No valid rule configuration found")
                    .ruleName(rule.getRuleName())
                    .build();
            };
                
        } catch (Exception e) {
            log.error("Error evaluating dynamic rule {}: {}", rule.getRuleName(), e.getMessage(), e);
            return RuleEvaluationResult.builder()
                .triggered(false)
                .reason("Rule evaluation error: " + e.getMessage())
                .ruleName(rule.getRuleName())
                .build();
        }
    }
    
    @Override
    public boolean supports(String ruleType) {
        if (ruleType == null) {
            return false;
        }
        // This evaluator supports both single and multi-condition rule types
        return Set.of(FraudRuleTypes.SIMPLE, FraudRuleTypes.GENERIC, FraudRuleTypes.FIELD_CONDITION, 
                     FraudRuleTypes.SINGLE_AMOUNT, FraudRuleTypes.TIME_OF_DAY, FraudRuleTypes.IP_BLACKLIST, 
                     FraudRuleTypes.IP_WHITELIST, FraudRuleTypes.LOCATION, FraudRuleTypes.DEVICE,
                     FraudRuleTypes.MULTI_CONDITION, FraudRuleTypes.COMPLEX_CONDITION)
            .contains(ruleType.toUpperCase());
    }
    
    /**
     * Evaluate field-based condition rules
     */
    private RuleEvaluationResult evaluateFieldCondition(FraudRule rule, Transaction transaction) {
        try {
            Object actualValue = getTransactionFieldValue(transaction, rule.getConditionField());
            String operator = rule.getConditionOperator().toUpperCase();
            String expectedValue = rule.getConditionValue();
            
            boolean triggered = evaluateCondition(actualValue, operator, expectedValue);
            double riskScore = triggered ? (rule.getRiskWeight() != null ? rule.getRiskWeight().doubleValue() : 0.5) : 0.0;
            
            return RuleEvaluationResult.builder()
                .triggered(triggered)
                .riskScore(riskScore)
                .reason(triggered ? String.format("Field %s %s %s condition met", 
                    rule.getConditionField(), operator, expectedValue) : "Condition not met")
                .ruleName(rule.getRuleName())
                .actualValue(actualValue != null ? actualValue.toString() : "null")
                .thresholdValue(expectedValue)
                .build();
                
        } catch (Exception e) {
            log.error("Error evaluating field condition: {}", e.getMessage(), e);
            return RuleEvaluationResult.builder()
                .triggered(false)
                .reason("Field condition evaluation error: " + e.getMessage())
                .ruleName(rule.getRuleName())
                .build();
        }
    }
    
    /**
     * Get field value from transaction using getter methods
     */
    private Object getTransactionFieldValue(Transaction transaction, String fieldName) throws Exception {
        // Convert field name to getter method name
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        
        try {
            Method method = Transaction.class.getMethod(getterName);
            return method.invoke(transaction);
        } catch (NoSuchMethodException e) {
            // Fallback: try to access field directly for backward compatibility
            try {
                java.lang.reflect.Field field = Transaction.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(transaction);
            } catch (Exception fieldException) {
                log.error("Unable to access field '{}' via getter '{}' or direct field access", fieldName, getterName);
                throw new Exception("Field '" + fieldName + "' not found or not accessible");
            }
        }
    }
    
    /**
     * Evaluate condition based on operator
     */
    private boolean evaluateCondition(Object actualValue, String operator, String expectedValue) {
        if (actualValue == null) {
            return FraudRuleOperators.IS_NULL.equals(operator);
        }
        
        return switch (operator) {
            case FraudRuleOperators.GREATER_THAN, FraudRuleOperators.GREATER_THAN_SYMBOL -> 
                compareValues(actualValue, expectedValue) > 0;
            case FraudRuleOperators.LESS_THAN, FraudRuleOperators.LESS_THAN_SYMBOL -> 
                compareValues(actualValue, expectedValue) < 0;
            case FraudRuleOperators.EQUAL, FraudRuleOperators.EQUAL_SYMBOL -> 
                compareValues(actualValue, expectedValue) == 0;
            case FraudRuleOperators.NOT_EQUAL, FraudRuleOperators.NOT_EQUAL_SYMBOL -> 
                compareValues(actualValue, expectedValue) != 0;
            case FraudRuleOperators.GREATER_THAN_OR_EQUAL, FraudRuleOperators.GREATER_THAN_OR_EQUAL_SYMBOL -> 
                compareValues(actualValue, expectedValue) >= 0;
            case FraudRuleOperators.LESS_THAN_OR_EQUAL, FraudRuleOperators.LESS_THAN_OR_EQUAL_SYMBOL -> 
                compareValues(actualValue, expectedValue) <= 0;
            case FraudRuleOperators.IN -> {
                List<String> values = Arrays.asList(expectedValue.split(FraudRuleOperators.LIST_VALUE_SEPARATOR));
                yield values.stream().anyMatch(val -> val.trim().equals(actualValue.toString().trim()));
            }
            case FraudRuleOperators.NOT_IN -> {
                List<String> values = Arrays.asList(expectedValue.split(FraudRuleOperators.LIST_VALUE_SEPARATOR));
                yield values.stream().noneMatch(val -> val.trim().equals(actualValue.toString().trim()));
            }
            case FraudRuleOperators.CONTAINS -> actualValue.toString().contains(expectedValue);
            case FraudRuleOperators.TIME_IN_RANGE -> evaluateTimeInRange(actualValue, expectedValue);
            case FraudRuleOperators.TIME_NOT_IN_RANGE -> !evaluateTimeInRange(actualValue, expectedValue);
            case FraudRuleOperators.IS_NULL -> false; // already handled above
            case FraudRuleOperators.IS_NOT_NULL -> true; // already handled above
            default -> {
                log.warn("Unsupported operator: {}", operator);
                yield false;
            }
        };
    }
    
    /**
     * Compare values based on their types
     */
    private int compareValues(Object actualValue, String expectedValue) {
        if (actualValue instanceof BigDecimal) {
            BigDecimal expected = new BigDecimal(expectedValue);
            return ((BigDecimal) actualValue).compareTo(expected);
        }
        if (actualValue instanceof Number) {
            Double actual = ((Number) actualValue).doubleValue();
            Double expected = Double.parseDouble(expectedValue);
            return actual.compareTo(expected);
        }
        if (actualValue instanceof String) {
            return ((String) actualValue).compareTo(expectedValue);
        }
        // Default to string comparison
        return actualValue.toString().compareTo(expectedValue);
    }
    
    /**
     * Evaluate if a time value is within a specified range
     * Expected format: "22:00-06:00" or "09:00-17:00"
     */
    private boolean evaluateTimeInRange(Object actualValue, String timeRange) {
        try {
            // Extract time from actualValue
            LocalTime actualTime;
            if (actualValue instanceof LocalTime) {
                actualTime = (LocalTime) actualValue;
            } else if (actualValue instanceof java.time.LocalDateTime) {
                actualTime = ((java.time.LocalDateTime) actualValue).toLocalTime();
            } else {
                // Try to parse as string
                actualTime = LocalTime.parse(actualValue.toString());
            }
            
            // Parse time range (format: "22:00-06:00")
            String[] parts = timeRange.split(FraudRuleOperators.TIME_RANGE_SEPARATOR);
            if (parts.length != 2) {
                log.warn("Invalid time range format: {}. Expected format: HH:mm-HH:mm", timeRange);
                return false;
            }
            
            LocalTime startTime = LocalTime.parse(parts[0].trim());
            LocalTime endTime = LocalTime.parse(parts[1].trim());
            
            // Handle overnight ranges (e.g., 22:00-06:00)
            if (startTime.isAfter(endTime)) {
                // Overnight range: current time should be after start OR before end
                return !actualTime.isBefore(startTime) || !actualTime.isAfter(endTime);
            } else {
                // Normal range: current time should be between start and end
                return !actualTime.isBefore(startTime) && !actualTime.isAfter(endTime);
            }
            
        } catch (Exception e) {
            log.warn("Error evaluating time range {} for value {}: {}", timeRange, actualValue, e.getMessage());
            return false;
        }
    }
} 