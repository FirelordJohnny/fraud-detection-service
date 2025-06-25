package com.faud.frauddetection.service.evaluator.impl;

import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.service.evaluator.RuleEngine;
import com.faud.frauddetection.dto.RuleEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Dynamic rule engine implementation
 * Configuration-based rule evaluation supporting multiple rule types without code changes
 */
@Component
@Slf4j
public class DynamicRuleEngine implements RuleEngine {
    
    private static final long TIME_WINDOW_SECONDS = 3600; // 1 hour
    private static final Set<String> SUSPICIOUS_IPS = Set.of(
        "192.168.1.100", "10.0.0.1", "172.16.0.1"
    );
    
    private final StringRedisTemplate redisTemplate;
    
    @Autowired
    public DynamicRuleEngine(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public RuleEvaluationResult evaluateRule(FraudRule rule, Transaction transaction) {
        try {
            log.debug("Evaluating rule: {} for transaction: {}", rule.getRuleName(), transaction.getTransactionId());
            
            return switch (rule.getRuleType().toUpperCase()) {
                case "AMOUNT" -> evaluateAmountRule(rule, transaction);
                case "FREQUENCY" -> evaluateFrequencyRule(rule, transaction);
                case "TIME_OF_DAY" -> evaluateTimeRule(rule, transaction);
                case "IP_BLACKLIST" -> evaluateIpRule(rule, transaction);
                case "CUSTOM" -> evaluateCustomRule(rule, transaction);
                default -> {
                    log.warn("Unsupported rule type: {}", rule.getRuleType());
                    yield RuleEvaluationResult.builder()
                        .triggered(false)
                        .reason("Unsupported rule type: " + rule.getRuleType())
                        .ruleName(rule.getRuleName())
                        .build();
                }
            };
        } catch (Exception e) {
            log.error("Error evaluating rule {}: {}", rule.getRuleName(), e.getMessage(), e);
            return RuleEvaluationResult.builder()
                .triggered(false)
                .reason("Rule evaluation error: " + e.getMessage())
                .ruleName(rule.getRuleName())
                .build();
        }
    }
    
    @Override
    public boolean supports(String ruleType) {
        if (ruleType == null || ruleType.trim().isEmpty()) {
            return false;
        }
        return Set.of("AMOUNT", "FREQUENCY", "TIME_OF_DAY", "IP_BLACKLIST", "CUSTOM")
            .contains(ruleType.toUpperCase());
    }
    
    /**
     * Evaluate amount rule
     */
    private RuleEvaluationResult evaluateAmountRule(FraudRule rule, Transaction transaction) {
        if (rule.getThresholdValue() == null) {
            return RuleEvaluationResult.builder()
                .triggered(false)
                .reason("Threshold value not configured")
                .ruleName(rule.getRuleName())
                .build();
        }
        
        boolean triggered = transaction.getAmount().compareTo(rule.getThresholdValue()) > 0;
        double riskScore = triggered ? Math.min(transaction.getAmount().doubleValue() / rule.getThresholdValue().doubleValue(), 1.0) : 0.0;
        
        return RuleEvaluationResult.builder()
            .triggered(triggered)
            .riskScore(riskScore)
            .reason(triggered ? String.format("Transaction amount %.2f exceeds threshold %.2f", 
                transaction.getAmount(), rule.getThresholdValue()) : "Transaction amount is normal")
            .ruleName(rule.getRuleName())
            .actualValue(transaction.getAmount().toString())
            .thresholdValue(rule.getThresholdValue().toString())
            .build();
    }
    
    /**
     * Evaluate frequency rule
     */
    private RuleEvaluationResult evaluateFrequencyRule(FraudRule rule, Transaction transaction) {
        if (rule.getThresholdValue() == null) {
            return RuleEvaluationResult.builder()
                .triggered(false)
                .reason("Threshold value not configured")
                .ruleName(rule.getRuleName())
                .build();
        }
        
        String key = "transactions:user:" + transaction.getUserId();
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (TIME_WINDOW_SECONDS * 1000);
        
        // Add current transaction
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        // Remove old transactions outside the window
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
        // Count transactions within the window
        Long count = redisTemplate.opsForZSet().zCard(key);
        // Set expiration time to prevent memory leaks
        redisTemplate.expire(key, TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
        
        boolean triggered = count != null && count > rule.getThresholdValue().longValue();
        double riskScore = triggered ? Math.min(count.doubleValue() / rule.getThresholdValue().doubleValue(), 1.0) : 0.0;
        
        return RuleEvaluationResult.builder()
            .triggered(triggered)
            .riskScore(riskScore)
            .reason(triggered ? String.format("User has %d transactions in 1 hour, exceeds threshold %d", 
                count, rule.getThresholdValue().longValue()) : "Transaction frequency is normal")
            .ruleName(rule.getRuleName())
            .actualValue(String.valueOf(count))
            .thresholdValue(rule.getThresholdValue().toString())
            .build();
    }
    
    /**
     * Evaluate time rule
     */
    private RuleEvaluationResult evaluateTimeRule(FraudRule rule, Transaction transaction) {
        LocalTime transactionTime = transaction.getTimestamp().toLocalTime();
        // Define suspicious time period: 22:00-06:00 (inclusive boundaries)
        boolean isSuspiciousTime = !transactionTime.isBefore(LocalTime.of(22, 0)) || 
                                  transactionTime.isBefore(LocalTime.of(6, 0));
        
        double riskScore = isSuspiciousTime ? 0.3 : 0.0;
        
        return RuleEvaluationResult.builder()
            .triggered(isSuspiciousTime)
            .riskScore(riskScore)
            .reason(isSuspiciousTime ? String.format("Transaction time %s is suspicious period", transactionTime) : "Transaction time is normal")
            .ruleName(rule.getRuleName())
            .actualValue(transactionTime.toString())
            .thresholdValue("22:00-06:00")
            .build();
    }
    
    /**
     * Evaluate IP rule
     */
    private RuleEvaluationResult evaluateIpRule(FraudRule rule, Transaction transaction) {
        boolean isSuspicious = transaction.getIpAddress() != null && 
                              SUSPICIOUS_IPS.contains(transaction.getIpAddress());
        
        double riskScore = isSuspicious ? 0.8 : 0.0;
        
        return RuleEvaluationResult.builder()
            .triggered(isSuspicious)
            .riskScore(riskScore)
            .reason(isSuspicious ? String.format("IP address %s is in blacklist", transaction.getIpAddress()) : "IP address is safe")
            .ruleName(rule.getRuleName())
            .actualValue(transaction.getIpAddress())
            .thresholdValue("IP blacklist")
            .build();
    }
    
    /**
     * Evaluate custom rule - more complex logic can be implemented through configuration fields
     */
    private RuleEvaluationResult evaluateCustomRule(FraudRule rule, Transaction transaction) {
        // Here you can implement custom logic based on rule.getRuleConfig()
        // For example: parse JSON configuration, implement complex condition combinations
        log.info("Custom rule evaluation not implemented for rule: {}", rule.getRuleName());
        
        return RuleEvaluationResult.builder()
            .triggered(false)
            .reason("Custom rule evaluation not implemented")
            .ruleName(rule.getRuleName())
            .build();
    }
} 