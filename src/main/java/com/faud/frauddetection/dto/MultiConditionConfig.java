package com.faud.frauddetection.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * Multi-condition configuration DTO - supports condition grouping
 * Used for frontend UI generation and backend parsing of multi-condition rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiConditionConfig {
    
    /**
     * List of condition groups
     */
    private List<ConditionGroup> conditionGroups;
    
    /**
     * Logical operator between condition groups
     * Values: "AND" or "OR"
     */
    private String groupLogicalOperator;
    
    /**
     * Condition group
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionGroup {
        /**
         * Group ID (for frontend management)
         */
        private String groupId;
        
        /**
         * Group name (optional, for display)
         */
        private String groupName;
        
        /**
         * List of conditions within the group
         */
        private List<RuleCondition> conditions;
        
        /**
         * Logical operator between conditions within the group
         * Values: "AND" or "OR"
         * Note: All conditions within the same group use the same logical operator
         */
        private String intraGroupOperator;
        
        /**
         * Group weight (optional)
         */
        private Double groupWeight;
    }
    
    /**
     * Single rule condition
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCondition {
        /**
         * Condition ID (for frontend management)
         */
        private String conditionId;
        
        /**
         * Field name
         */
        private String field;
        
        /**
         * Operator
         */
        private String operator;
        
        /**
         * Value
         */
        private String value;
        
        /**
         * Condition weight (optional)
         */
        private Double weight;
        
        /**
         * Condition description (optional, for UI display)
         */
        private String description;
    }
} 