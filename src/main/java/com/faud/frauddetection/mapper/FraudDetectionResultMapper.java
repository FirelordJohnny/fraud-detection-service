package com.faud.frauddetection.mapper;

import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * MyBatis Mapper for FraudDetectionResult.
 * All SQL queries are defined in 'resources/mapper/FraudDetectionResultMapper.xml'.
 */
@Mapper
public interface FraudDetectionResultMapper {

    /**
     * Find fraud detection result by ID
     */
    FraudDetectionResultEntity findById(Long id);

    /**
     * Find all fraud detection results
     */
    List<FraudDetectionResultEntity> findAll();

    /**
     * Insert new fraud detection result
     */
    void insert(FraudDetectionResultEntity result);

    /**
     * Delete fraud detection result by ID
     */
    void deleteById(Long id);
} 