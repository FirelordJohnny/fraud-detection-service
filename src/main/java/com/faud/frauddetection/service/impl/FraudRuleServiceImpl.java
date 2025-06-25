package com.faud.frauddetection.service.impl;

import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.repository.FraudRuleRepository;
import com.faud.frauddetection.service.FraudRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of Fraud Rule Service
 */
@Service
public class FraudRuleServiceImpl implements FraudRuleService {

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    @Override
    public FraudRule createFraudRule(FraudRule fraudRule) {
        fraudRule.setCreatedAt(LocalDateTime.now());
        fraudRule.setUpdatedAt(LocalDateTime.now());
        fraudRuleRepository.save(fraudRule);
        return fraudRule;
    }

    @Override
    public Optional<FraudRule> getFraudRuleById(Long id) {
        return fraudRuleRepository.findById(id);
    }

    @Override
    public List<FraudRule> getAllFraudRules() {
        return fraudRuleRepository.findAll();
    }

    @Override
    public FraudRule updateFraudRule(Long id, FraudRule fraudRuleDetails) {
        FraudRule existingRule = fraudRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fraud rule with id " + id + " not found"));
        // Update fields
        existingRule.setRuleName(fraudRuleDetails.getRuleName());
        existingRule.setRuleType(fraudRuleDetails.getRuleType());
        existingRule.setDescription(fraudRuleDetails.getDescription());
        existingRule.setRuleConfig(fraudRuleDetails.getRuleConfig());
        existingRule.setEnabled(fraudRuleDetails.getEnabled());
        existingRule.setThresholdValue(fraudRuleDetails.getThresholdValue());
        existingRule.setUpdatedAt(LocalDateTime.now());

        fraudRuleRepository.update(existingRule);
        return existingRule;
    }

    @Override
    public void deleteFraudRule(Long id) {
        fraudRuleRepository.delete(id);
    }
} 