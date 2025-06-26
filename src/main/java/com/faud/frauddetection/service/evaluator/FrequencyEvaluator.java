package com.faud.frauddetection.service.evaluator;

import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import com.faud.frauddetection.constant.RedisKeys;
import com.faud.frauddetection.config.FraudDetectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Frequency-based rule evaluator implementation
 * Handles transaction frequency validation within a time window
 */
@Component
@Slf4j
public class FrequencyEvaluator implements RuleEvaluator {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FraudDetectionProperties properties;
    
    public FrequencyEvaluator(StringRedisTemplate redisTemplate, FraudDetectionProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.properties = properties;
    }
    
    @Override
    public RuleEvaluationResult evaluateRule(FraudRule rule, Transaction transaction) {
        try {
            log.debug("Evaluating frequency rule: {} for transaction: {}", rule.getRuleName(), transaction.getTransactionId());
            
            if (rule.getThresholdValue() == null) {
                return RuleEvaluationResult.builder()
                    .triggered(false)
                    .reason("Threshold value not configured")
                    .ruleName(rule.getRuleName())
                    .build();
            }
            
            // Get time window from rule configuration, fallback to default
            long timeWindowSeconds = getTimeWindowFromRule(rule);
            
            String key = RedisKeys.transactionsKey(transaction.getUserId());
            long now = Instant.now().toEpochMilli();
            long windowStart = now - (timeWindowSeconds * 1000);
            
            // Add current transaction
            redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
            // Remove old transactions outside the window
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
            // Count transactions within the window
            Long count = redisTemplate.opsForZSet().zCard(key);
            // Set expiration time to prevent memory leaks
            redisTemplate.expire(key, timeWindowSeconds, TimeUnit.SECONDS);
            
            boolean triggered = count != null && count > rule.getThresholdValue().longValue();
            double riskScore = triggered ? Math.min(count.doubleValue() / rule.getThresholdValue().doubleValue(), 1.0) : 0.0;
            
            return RuleEvaluationResult.builder()
                .triggered(triggered)
                .riskScore(riskScore)
                .reason(triggered ? String.format("User has %d transactions in %d seconds, exceeds threshold %d", 
                    count, timeWindowSeconds, rule.getThresholdValue().longValue()) : "Transaction frequency is normal")
                .ruleName(rule.getRuleName())
                .actualValue(String.valueOf(count))
                .thresholdValue(rule.getThresholdValue().toString())
                .build();
                
        } catch (Exception e) {
            log.error("Error evaluating frequency rule {}: {}", rule.getRuleName(), e.getMessage(), e);
            return RuleEvaluationResult.builder()
                .triggered(false)
                .reason("Rule evaluation error: " + e.getMessage())
                .ruleName(rule.getRuleName())
                .build();
        }
    }
    
    @Override
    public boolean supports(String ruleType) {
        return "FREQUENCY".equalsIgnoreCase(ruleType);
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
} 