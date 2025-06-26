package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.MultiConditionConfig;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.constant.FraudRuleOperators;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/**
 * Multi-condition evaluator
 * Handles non-nested condition grouping logic
 */
@Component
@Slf4j
public class MultiConditionEvaluator implements RuleEvaluator {
    
    private final ObjectMapper objectMapper;
    
    public MultiConditionEvaluator() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Evaluate multi-condition rules
     */
    @Override
    public RuleEvaluationResult evaluateRule(FraudRule rule, Transaction transaction) {
        try {
            // Parse JSON configuration
            MultiConditionConfig config = objectMapper.readValue(
                rule.getRuleConfig(), 
                new TypeReference<MultiConditionConfig>() {}
            );
            
            if (config.getConditionGroups() == null || config.getConditionGroups().isEmpty()) {
                return createFailureResult(rule, "No condition groups configured");
            }
            
            // Evaluate all condition groups
            boolean overallResult = evaluateConditionGroups(config, transaction);
            
            // Calculate risk score
            double riskScore = overallResult ? 
                (rule.getRiskWeight() != null ? rule.getRiskWeight().doubleValue() : 0.5) : 0.0;
            
            String reason = overallResult ? 
                "Multi-condition rule triggered" : "Multi-condition rule not triggered";
            
            return RuleEvaluationResult.builder()
                .triggered(overallResult)
                .riskScore(riskScore)
                .reason(reason)
                .ruleName(rule.getRuleName())
                .build();
                
        } catch (Exception e) {
            log.error("Error evaluating multi-condition rule {}: {}", rule.getRuleName(), e.getMessage(), e);
            return createFailureResult(rule, "Multi-condition evaluation error: " + e.getMessage());
        }
    }
    
    /**
     * Evaluate all condition groups
     */
    private boolean evaluateConditionGroups(MultiConditionConfig config, Transaction transaction) {
        List<MultiConditionConfig.ConditionGroup> groups = config.getConditionGroups();
        String groupOperator = config.getGroupLogicalOperator();
        
        if (groups.size() == 1) {
            return evaluateConditionGroup(groups.get(0), transaction);
        }
        
        // Handle logical operations between multiple groups
        boolean result = evaluateConditionGroup(groups.get(0), transaction);
        
        for (int i = 1; i < groups.size(); i++) {
            boolean groupResult = evaluateConditionGroup(groups.get(i), transaction);
            
            if ("AND".equalsIgnoreCase(groupOperator)) {
                result = result && groupResult;
                // Short-circuit evaluation: if already false, no need to continue
                if (!result) {
                    break;
                }
            } else if ("OR".equalsIgnoreCase(groupOperator)) {
                result = result || groupResult;
                // Short-circuit evaluation: if already true, no need to continue
                if (result) {
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Evaluate single condition group
     */
    private boolean evaluateConditionGroup(MultiConditionConfig.ConditionGroup group, Transaction transaction) {
        List<MultiConditionConfig.RuleCondition> conditions = group.getConditions();
        String intraOperator = group.getIntraGroupOperator();
        
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }
        
        if (conditions.size() == 1) {
            return evaluateCondition(conditions.get(0), transaction);
        }
        
        // Handle logical operations between multiple conditions within group
        boolean result = evaluateCondition(conditions.get(0), transaction);
        
        for (int i = 1; i < conditions.size(); i++) {
            boolean conditionResult = evaluateCondition(conditions.get(i), transaction);
            
            if (FraudRuleOperators.AND.equalsIgnoreCase(intraOperator)) {
                result = result && conditionResult;
                // Short-circuit evaluation
                if (!result) {
                    break;
                }
            } else if (FraudRuleOperators.OR.equalsIgnoreCase(intraOperator)) {
                result = result || conditionResult;
                // Short-circuit evaluation
                if (result) {
                    break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Evaluate single condition
     */
    private boolean evaluateCondition(MultiConditionConfig.RuleCondition condition, Transaction transaction) {
        try {
            Object actualValue = getTransactionFieldValue(transaction, condition.getField());
            String operator = condition.getOperator().toUpperCase();
            String expectedValue = condition.getValue();
            
            return evaluateConditionLogic(actualValue, operator, expectedValue);
            
        } catch (Exception e) {
            log.warn("Error evaluating condition {}: {}", condition.getField(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Get transaction field value
     */
    private Object getTransactionFieldValue(Transaction transaction, String fieldName) throws Exception {
        Field field = Transaction.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(transaction);
    }
    
    /**
     * Evaluate condition logic (reuses DynamicEvaluator logic)
     */
    private boolean evaluateConditionLogic(Object actualValue, String operator, String expectedValue) {
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
     * Compare values
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
        return actualValue.toString().compareTo(expectedValue);
    }
    
    /**
     * Evaluate time range
     */
    private boolean evaluateTimeInRange(Object actualValue, String timeRange) {
        try {
            LocalTime actualTime;
            if (actualValue instanceof LocalTime) {
                actualTime = (LocalTime) actualValue;
            } else if (actualValue instanceof java.time.LocalDateTime) {
                actualTime = ((java.time.LocalDateTime) actualValue).toLocalTime();
            } else {
                actualTime = LocalTime.parse(actualValue.toString());
            }
            
            String[] parts = timeRange.split(FraudRuleOperators.TIME_RANGE_SEPARATOR);
            if (parts.length != 2) {
                log.warn("Invalid time range format: {}", timeRange);
                return false;
            }
            
            LocalTime startTime = LocalTime.parse(parts[0].trim());
            LocalTime endTime = LocalTime.parse(parts[1].trim());
            
            if (startTime.isAfter(endTime)) {
                return !actualTime.isBefore(startTime) || !actualTime.isAfter(endTime);
            } else {
                return !actualTime.isBefore(startTime) && !actualTime.isAfter(endTime);
            }
            
        } catch (Exception e) {
            log.warn("Error evaluating time range {} for value {}: {}", timeRange, actualValue, e.getMessage());
            return false;
        }
    }
    
    /**
     * Create failure result
     */
    private RuleEvaluationResult createFailureResult(FraudRule rule, String reason) {
        return RuleEvaluationResult.builder()
            .triggered(false)
            .reason(reason)
            .ruleName(rule.getRuleName())
            .build();
    }
    
    @Override
    public boolean supports(String ruleType) {
        // This evaluator is used internally by DynamicEvaluator, not directly by the service
        return false;
    }
} 