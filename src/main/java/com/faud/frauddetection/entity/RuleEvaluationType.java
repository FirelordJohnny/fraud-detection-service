package com.faud.frauddetection.entity;

/**
 * Rule evaluation type enumeration
 * Used to distinguish different rule configurations and evaluation methods
 */
public enum RuleEvaluationType {
    /**
     * Single condition evaluation - uses conditionField, conditionOperator, conditionValue
     */
    SINGLE_CONDITION,
    
    /**
     * Multi-condition group evaluation - uses ruleConfig field to store MultiConditionConfig
     */
    MULTI_CONDITION,
    
    /**
     * Invalid configuration
     */
    INVALID
} 