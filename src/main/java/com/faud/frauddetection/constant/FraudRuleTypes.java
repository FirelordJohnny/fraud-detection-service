package com.faud.frauddetection.constant;

/**
 * Constants for fraud rule types
 * Centralizes all rule type string constants
 */
public final class FraudRuleTypes {
    
    private FraudRuleTypes() {
        // Utility class - prevent instantiation
    }
    
    // Rule Types
    public static final String AMOUNT = "AMOUNT";
    public static final String FREQUENCY = "FREQUENCY";
    public static final String TIME_OF_DAY = "TIME_OF_DAY";
    public static final String IP_BLACKLIST = "IP_BLACKLIST";
    public static final String IP_WHITELIST = "IP_WHITELIST";
    public static final String LOCATION = "LOCATION";
    public static final String DEVICE = "DEVICE";
    public static final String SIMPLE = "SIMPLE";
    public static final String GENERIC = "GENERIC";
    public static final String FIELD_CONDITION = "FIELD_CONDITION";
    public static final String SINGLE_AMOUNT = "SINGLE_AMOUNT";
    public static final String MULTI_CONDITION = "MULTI_CONDITION";
    public static final String COMPLEX_CONDITION = "COMPLEX_CONDITION";
    public static final String DYNAMIC = "DYNAMIC";
    public static final String CUSTOM = "CUSTOM";
} 