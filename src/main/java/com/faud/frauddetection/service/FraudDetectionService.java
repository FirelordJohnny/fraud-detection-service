package com.faud.frauddetection.service;

import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.dto.FraudDetectionResult;

/**
 * Fraud detection service interface
 */
public interface FraudDetectionService {
    
    /**
     * Detect if a transaction has fraud risk
     * 
     * @param transaction the transaction to be detected
     * @return fraud detection result
     */
    FraudDetectionResult detectFraud(Transaction transaction);
} 