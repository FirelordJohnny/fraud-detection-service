package com.faud.frauddetection.service;

import com.faud.frauddetection.entity.FraudRule;

import java.util.List;
import java.util.Optional;

public interface FraudRuleService {
    FraudRule createFraudRule(FraudRule fraudRule);
    Optional<FraudRule> getFraudRuleById(Long id);
    List<FraudRule> getAllFraudRules();
    FraudRule updateFraudRule(Long id, FraudRule fraudRule);
    void deleteFraudRule(Long id);
}