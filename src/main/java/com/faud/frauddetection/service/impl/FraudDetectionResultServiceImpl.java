package com.faud.frauddetection.service.impl;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import com.faud.frauddetection.repository.FraudDetectionResultRepository;
import com.faud.frauddetection.service.FraudDetectionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of Fraud Detection Result Service
 */
@Service
public class FraudDetectionResultServiceImpl implements FraudDetectionResultService {

    private final FraudDetectionResultRepository resultRepository;

    @Autowired
    public FraudDetectionResultServiceImpl(FraudDetectionResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Override
    public void saveResult(FraudDetectionResult resultDto) {
        FraudDetectionResultEntity entity = new FraudDetectionResultEntity();
        entity.setTransactionId(resultDto.getTransactionId());
        entity.setFraud(resultDto.isFraud());
        entity.setRiskScore(resultDto.getRiskScore());
        entity.setReason(resultDto.getReason());
        entity.setDetectionTimestamp(resultDto.getDetectionTimestamp());
        resultRepository.save(entity);
    }

    @Override
    public List<FraudDetectionResultEntity> getAllResults() {
        return resultRepository.findAll();
    }

    @Override
    public Optional<FraudDetectionResultEntity> getResultById(Long id) {
        return resultRepository.findById(id);
    }
} 