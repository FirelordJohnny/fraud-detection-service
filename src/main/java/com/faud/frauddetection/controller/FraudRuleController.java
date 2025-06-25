package com.faud.frauddetection.controller;

import com.faud.frauddetection.dto.FraudRuleDto;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.service.FraudRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * Fraud Rule Management Controller
 * Provides dynamic rule management functionality with CRUD operations
 */
@RestController
@RequestMapping("/api/v1/fraud-rules")
@Slf4j
@Validated
public class FraudRuleController {

    private final FraudRuleService fraudRuleService;

    @Autowired
    public FraudRuleController(FraudRuleService fraudRuleService) {
        this.fraudRuleService = fraudRuleService;
    }

    /**
     * Get all fraud rules
     */
    @GetMapping
    public ResponseEntity<List<FraudRule>> getAllRules() {
        log.info("Getting all fraud rules");
        List<FraudRule> rules = fraudRuleService.getAllFraudRules();
        log.info("Returning {} rules", rules.size());
        return ResponseEntity.ok(rules);
    }

    /**
     * Get rule by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FraudRule> getRuleById(@PathVariable Long id) {
        log.info("Getting rule ID: {}", id);
        return fraudRuleService.getFraudRuleById(id)
                .map(rule -> {
                    log.info("Found rule: {}", rule.getRuleName());
                    return ResponseEntity.ok(rule);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new rule
     */
    @PostMapping
    public ResponseEntity<FraudRule> createRule(@Valid @RequestBody FraudRuleDto ruleDto) {
        log.info("Creating new rule: {}", ruleDto.getRuleName());
        
        FraudRule rule = convertToEntity(ruleDto);
        FraudRule savedRule = fraudRuleService.createFraudRule(rule);
        
        log.info("Successfully created rule with ID: {}", savedRule.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
    }

    /**
     * Update rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<FraudRule> updateRule(@PathVariable Long id, @Valid @RequestBody FraudRuleDto ruleDto) {
        log.info("Updating rule ID: {}", id);
        
        FraudRule rule = convertToEntity(ruleDto);
        FraudRule updatedRule = fraudRuleService.updateFraudRule(id, rule);
        
        log.info("Successfully updated rule: {}", updatedRule.getRuleName());
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * Delete rule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        log.info("Deleting rule ID: {}", id);
        fraudRuleService.deleteFraudRule(id);
        log.info("Successfully deleted rule ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle rule status
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<FraudRule> toggleRule(@PathVariable Long id) {
        log.info("Toggling rule status, ID: {}", id);
        
        return fraudRuleService.getFraudRuleById(id)
                .map(existingRule -> {
                    existingRule.setEnabled(!existingRule.getEnabled());
                    FraudRule updatedRule = fraudRuleService.updateFraudRule(id, existingRule);
                    log.info("Rule {} status toggled to: {}", updatedRule.getRuleName(), updatedRule.getEnabled());
                    return ResponseEntity.ok(updatedRule);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Quick create common rules
     */
    @PostMapping("/quick-create")
    public ResponseEntity<FraudRule> quickCreateRule(
            @RequestParam String ruleType,
            @RequestParam String ruleName,
            @RequestParam BigDecimal threshold,
            @RequestParam(required = false) String description) {
        
        log.info("Quick creating rule: {} (type: {})", ruleName, ruleType);
        
        FraudRule rule = FraudRule.builder()
                .ruleName(ruleName)
                .ruleType(ruleType.toUpperCase())
                .description(description != null ? description : "Quick created " + ruleType + " rule")
                .thresholdValue(threshold)
                .enabled(true)
                .riskWeight(getDefaultRiskWeight(ruleType))
                .priority(1)
                .build();
        
        FraudRule savedRule = fraudRuleService.createFraudRule(rule);
        log.info("Quick create rule successful, ID: {}", savedRule.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
    }

    /**
     * Convert DTO to entity
     */
    private FraudRule convertToEntity(FraudRuleDto dto) {
        return FraudRule.builder()
                .ruleName(dto.getRuleName())
                .ruleType(dto.getRuleType())
                .description(dto.getDescription())
                .ruleConfig(dto.getRuleConfig())
                .enabled(dto.getEnabled())
                .thresholdValue(dto.getThresholdValue())
                .conditionField(dto.getConditionField())
                .conditionOperator(dto.getConditionOperator())
                .conditionValue(dto.getConditionValue())
                .riskWeight(dto.getRiskWeight() != null ? dto.getRiskWeight() : getDefaultRiskWeight(dto.getRuleType()))
                .priority(dto.getPriority() != null ? dto.getPriority() : 1)
                .build();
    }

    /**
     * Get default risk weight for rule type
     */
    private BigDecimal getDefaultRiskWeight(String ruleType) {
        return switch (ruleType.toUpperCase()) {
            case "AMOUNT" -> BigDecimal.valueOf(0.30);
            case "FREQUENCY" -> BigDecimal.valueOf(0.25);
            case "TIME_OF_DAY" -> BigDecimal.valueOf(0.15);
            case "IP_BLACKLIST" -> BigDecimal.valueOf(0.40);
            case "CUSTOM" -> BigDecimal.valueOf(0.20);
            default -> BigDecimal.valueOf(0.20);
        };
    }
} 