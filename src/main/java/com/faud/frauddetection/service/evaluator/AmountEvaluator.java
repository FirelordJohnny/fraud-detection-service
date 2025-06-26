package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.constant.RedisKeys;
import com.faud.frauddetection.config.FraudDetectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.Map;

/**
 * Amount-based rule evaluator implementation
 * Handles cumulative amount validation within a time window
 */
@Component
@Slf4j
public class AmountEvaluator implements RuleEvaluator {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FraudDetectionProperties properties;
    
    public AmountEvaluator(StringRedisTemplate redisTemplate, FraudDetectionProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.properties = properties;
    }
    
    @Override
    public RuleEvaluationResult evaluateRule(FraudRule rule, Transaction transaction) {
        try {
            log.debug("Evaluating cumulative amount rule: {} for transaction: {}", rule.getRuleName(), transaction.getTransactionId());
            
            if (rule.getThresholdValue() == null) {
                return RuleEvaluationResult.builder()
                    .triggered(false)
                    .reason("Threshold value not configured")
                    .ruleName(rule.getRuleName())
                    .build();
            }
            
            // Get time window from rule configuration, fallback to default
            long timeWindowSeconds = getTimeWindowFromRule(rule);
            
            // AmountEvaluator only handles cumulative amount checks
            // Single amount checks should be handled by DynamicEvaluator
            return evaluateCumulativeAmount(rule, transaction, timeWindowSeconds);
                
        } catch (Exception e) {
            log.error("Error evaluating amount rule {}: {}", rule.getRuleName(), e.getMessage(), e);
            return RuleEvaluationResult.builder()
                .triggered(false)
                .reason("Rule evaluation error: " + e.getMessage())
                .ruleName(rule.getRuleName())
                .build();
        }
    }
    
    /**
     * Evaluate cumulative amount within time window
     */
    private RuleEvaluationResult evaluateCumulativeAmount(FraudRule rule, Transaction transaction, long timeWindowSeconds) {
        String key = RedisKeys.amountsKey(transaction.getUserId());
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (timeWindowSeconds * 1000);
        
        // Add current transaction amount with timestamp as score
        String transactionData = transaction.getAmount().toString();
        redisTemplate.opsForZSet().add(key, transactionData, now);
        
        // Remove old transactions outside the window
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        
        // Calculate cumulative amount within the window
        var amountEntries = redisTemplate.opsForZSet().rangeByScoreWithScores(key, windowStart, now);
        BigDecimal cumulativeAmount = BigDecimal.ZERO;
        
        if (amountEntries != null) {
            for (var entry : amountEntries) {
                try {
                    BigDecimal amount = new BigDecimal(entry.getValue());
                    cumulativeAmount = cumulativeAmount.add(amount);
                } catch (NumberFormatException e) {
                    log.warn("Invalid amount format in Redis: {}", entry.getValue());
                }
            }
        }
        
        // Set expiration time to prevent memory leaks
        redisTemplate.expire(key, timeWindowSeconds, TimeUnit.SECONDS);
        
        boolean triggered = cumulativeAmount.compareTo(rule.getThresholdValue()) > 0;
        double riskScore = triggered ? Math.min(cumulativeAmount.doubleValue() / rule.getThresholdValue().doubleValue(), 1.0) : 0.0;
        
        return RuleEvaluationResult.builder()
            .triggered(triggered)
            .riskScore(riskScore)
            .reason(triggered ? String.format("Cumulative amount %.2f in %d seconds exceeds threshold %.2f", 
                cumulativeAmount, timeWindowSeconds, rule.getThresholdValue()) : "Cumulative amount is normal")
            .ruleName(rule.getRuleName())
            .actualValue(cumulativeAmount.toString())
            .thresholdValue(rule.getThresholdValue().toString())
            .build();
    }
    
    /**
     * Extract time window from rule configuration
     * Rule config should contain: {"timeWindowSeconds": 3600}
     */
    private long getTimeWindowFromRule(FraudRule rule) {
        try {
            if (rule.getRuleConfig() != null && !rule.getRuleConfig().trim().isEmpty()) {
                Map<String, Object> config = objectMapper.readValue(rule.getRuleConfig(), 
                    new TypeReference<Map<String, Object>>() {});
                
                if (config.containsKey("timeWindowSeconds")) {
                    Object timeWindow = config.get("timeWindowSeconds");
                    if (timeWindow instanceof Number) {
                        return ((Number) timeWindow).longValue();
                    }
                    if (timeWindow instanceof String) {
                        return Long.parseLong((String) timeWindow);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse time window from rule config, using default: {}", e.getMessage());
        }
        return properties.getTimeWindow().getDefaultSeconds();
    }
    
    @Override
    public boolean supports(String ruleType) {
        return "AMOUNT".equalsIgnoreCase(ruleType);
    }
} 