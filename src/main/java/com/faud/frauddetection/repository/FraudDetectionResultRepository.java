package com.faud.frauddetection.repository;

import com.faud.frauddetection.entity.FraudDetectionResultEntity;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing FraudDetectionResult entities.
 */
public interface FraudDetectionResultRepository {

    FraudDetectionResultEntity save(FraudDetectionResultEntity result);

    Optional<FraudDetectionResultEntity> findById(Long id);

    List<FraudDetectionResultEntity> findAll();

} 