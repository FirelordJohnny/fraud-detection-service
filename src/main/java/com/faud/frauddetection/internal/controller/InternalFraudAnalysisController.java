package com.faud.frauddetection.internal.controller;

import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import com.faud.frauddetection.service.FraudDetectionResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal Fraud Analysis Controller for UI access
 * Requires API Token authentication
 */
@RestController
@RequestMapping("/internal/fraud-analysis")
public class InternalFraudAnalysisController {

    private final FraudDetectionResultService resultService;

    public InternalFraudAnalysisController(FraudDetectionResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/results")
    public ResponseEntity<List<FraudDetectionResultEntity>> getAllResults() {
        List<FraudDetectionResultEntity> results = resultService.getAllResults();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<FraudDetectionResultEntity> getResultById(@PathVariable Long id) {
        return resultService.getResultById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Internal Fraud Analysis API is healthy");
    }
} 