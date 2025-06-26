package com.faud.frauddetection.repository.impl;

import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import com.faud.frauddetection.mapper.FraudDetectionResultMapper;
import com.faud.frauddetection.repository.FraudDetectionResultRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FraudDetectionResultRepositoryImpl implements FraudDetectionResultRepository {

    private final FraudDetectionResultMapper resultMapper;

    public FraudDetectionResultRepositoryImpl(FraudDetectionResultMapper resultMapper) {
        this.resultMapper = resultMapper;
    }

    @Override
    public FraudDetectionResultEntity save(FraudDetectionResultEntity result) {
        resultMapper.insert(result);
        return result;
    }

    @Override
    public Optional<FraudDetectionResultEntity> findById(Long id) {
        return Optional.ofNullable(resultMapper.findById(id));
    }

    @Override
    public List<FraudDetectionResultEntity> findAll() {
        return resultMapper.findAll();
    }

    // @Override
    // public void deleteById(Long id) {
    //     resultMapper.deleteById(id);
    // }
} 