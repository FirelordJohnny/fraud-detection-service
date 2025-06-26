package com.faud.frauddetection.constant;

/**
 * Constants for fraud rule operators
 * Centralizes all operator string constants to avoid hardcoding throughout the codebase
 */
public final class FraudRuleOperators {
    
    private FraudRuleOperators() {
        // Utility class - prevent instantiation
    }
    
    // Comparison Operators
    public static final String GREATER_THAN = "GT";
    public static final String GREATER_THAN_SYMBOL = ">";
    public static final String LESS_THAN = "LT";
    public static final String LESS_THAN_SYMBOL = "<";
    public static final String EQUAL = "EQ";
    public static final String EQUAL_SYMBOL = "=";
    public static final String NOT_EQUAL = "NE";
    public static final String NOT_EQUAL_SYMBOL = "!=";
    public static final String GREATER_THAN_OR_EQUAL = "GTE";
    public static final String GREATER_THAN_OR_EQUAL_SYMBOL = ">=";
    public static final String LESS_THAN_OR_EQUAL = "LTE";
    public static final String LESS_THAN_OR_EQUAL_SYMBOL = "<=";
    
    // List/Collection Operators
    public static final String IN = "IN";
    public static final String NOT_IN = "NOT_IN";
    
    // String Operators
    public static final String CONTAINS = "CONTAINS";
    
    // Time Operators
    public static final String TIME_IN_RANGE = "TIME_IN_RANGE";
    public static final String TIME_NOT_IN_RANGE = "TIME_NOT_IN_RANGE";
    
    // Null Check Operators
    public static final String IS_NULL = "IS_NULL";
    public static final String IS_NOT_NULL = "IS_NOT_NULL";
    
    // Logical Operators
    public static final String AND = "AND";
    public static final String OR = "OR";
    
    // Time Range Separator
    public static final String TIME_RANGE_SEPARATOR = "-";
    
    // List Value Separator
    public static final String LIST_VALUE_SEPARATOR = ",";
} 