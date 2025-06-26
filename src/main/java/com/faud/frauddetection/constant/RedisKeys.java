package com.faud.frauddetection.constant;

/**
 * Constants for Redis key patterns
 * Centralizes all Redis key patterns used in the fraud detection system
 */
public final class RedisKeys {
    
    private RedisKeys() {
        // Utility class - prevent instantiation
    }
    
    // Key Prefixes
    public static final String FRAUD_PREFIX = "fraud";
    public static final String FREQUENCY_PREFIX = "frequency";
    public static final String AMOUNTS_PREFIX = "amounts";
    public static final String TRANSACTIONS_PREFIX = "transactions";
    public static final String USER_PREFIX = "user";
    
    // Key Separators
    public static final String KEY_SEPARATOR = ":";
    
    // Complete Key Patterns
    public static final String FRAUD_FREQUENCY_USER_PATTERN = FRAUD_PREFIX + KEY_SEPARATOR + FREQUENCY_PREFIX + KEY_SEPARATOR + USER_PREFIX + KEY_SEPARATOR + "%s";
    public static final String AMOUNTS_USER_PATTERN = AMOUNTS_PREFIX + KEY_SEPARATOR + USER_PREFIX + KEY_SEPARATOR + "%s";
    public static final String TRANSACTIONS_USER_PATTERN = TRANSACTIONS_PREFIX + KEY_SEPARATOR + USER_PREFIX + KEY_SEPARATOR + "%s";
    
    /**
     * Generate fraud frequency key for user
     * @param userId user identifier
     * @return formatted Redis key
     */
    public static String fraudFrequencyKey(String userId) {
        return String.format(FRAUD_FREQUENCY_USER_PATTERN, userId);
    }
    
    /**
     * Generate amounts key for user
     * @param userId user identifier
     * @return formatted Redis key
     */
    public static String amountsKey(String userId) {
        return String.format(AMOUNTS_USER_PATTERN, userId);
    }
    
    /**
     * Generate transactions key for user
     * @param userId user identifier
     * @return formatted Redis key
     */
    public static String transactionsKey(String userId) {
        return String.format(TRANSACTIONS_USER_PATTERN, userId);
    }
} 