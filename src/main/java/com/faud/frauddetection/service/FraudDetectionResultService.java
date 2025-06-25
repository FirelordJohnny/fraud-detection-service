package com.faud.frauddetection.service;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.entity.FraudDetectionResultEntity;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing fraud detection results.
 */
public interface FraudDetectionResultService {

    /**
     * Saves a fraud detection result.
     * @param result The result to save.
     */
    void saveResult(FraudDetectionResult resultDto);

    /**
     * Retrieves all fraud detection results.
     * @return A list of all results.
     */
    List<FraudDetectionResultEntity> getAllResults();

    /**
     * Retrieves a single fraud detection result by its ID.
     * @param id The ID of the result.
     * @return The result entity, or null if not found.
     */
    Optional<FraudDetectionResultEntity> getResultById(Long id);
} 