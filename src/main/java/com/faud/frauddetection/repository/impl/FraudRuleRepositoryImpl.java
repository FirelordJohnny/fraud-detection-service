package com.faud.frauddetection.repository.impl;

import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.mapper.FraudRuleMapper;
import com.faud.frauddetection.repository.FraudRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FraudRuleRepositoryImpl implements FraudRuleRepository {

    private final FraudRuleMapper fraudRuleMapper;

    @Autowired
    public FraudRuleRepositoryImpl(FraudRuleMapper fraudRuleMapper) {
        this.fraudRuleMapper = fraudRuleMapper;
    }

    @Override
    public Optional<FraudRule> findById(Long id) {
        return fraudRuleMapper.findById(id);
    }

    @Override
    public List<FraudRule> findAll() {
        return fraudRuleMapper.findAll();
    }

    @Override
    public List<FraudRule> findAllEnabled() {
        return fraudRuleMapper.findAllEnabled();
    }

    @Override
    public void save(FraudRule fraudRule) {
        fraudRuleMapper.insert(fraudRule);
    }

    @Override
    public void update(FraudRule fraudRule) {
        fraudRuleMapper.update(fraudRule);
    }

    @Override
    public void delete(Long id) {
        fraudRuleMapper.delete(id);
    }
} 