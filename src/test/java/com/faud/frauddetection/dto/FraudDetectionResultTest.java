package com.faud.frauddetection.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FraudDetectionResultTest {

    @Test
    void testBuilder_ShouldCreateFraudDetectionResultWithAllFields() {
        // Given
        String transactionId = "TXN_123";
        boolean fraudulent = true;
        double riskScore = 0.85;
        String riskLevel = "HIGH";
        String reason = "High risk transaction";
        LocalDateTime detectionTime = LocalDateTime.now();
        long processingTime = 100L;
        List<RuleEvaluationResult> evaluationResults = Arrays.asList(
                RuleEvaluationResult.builder().ruleName("RULE1").build(),
                RuleEvaluationResult.builder().ruleName("RULE2").build()
        );

        // When
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId(transactionId)
                .isFraudulent(fraudulent)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .reason(reason)
                .detectionTime(detectionTime)
                .processingTime(processingTime)
                .evaluationResults(evaluationResults)
                .build();

        // Then
        assertThat(result.getTransactionId()).isEqualTo(transactionId);
        assertThat(result.isFraudulent()).isEqualTo(fraudulent);
        assertThat(result.isFraud()).isEqualTo(fraudulent);  // Test legacy method
        assertThat(result.getRiskScore()).isEqualTo(riskScore);
        assertThat(result.getRiskLevel()).isEqualTo(riskLevel);
        assertThat(result.getReason()).isEqualTo(reason);
        assertThat(result.getDetectionTime()).isEqualTo(detectionTime);
        assertThat(result.getDetectionTimestamp()).isEqualTo(detectionTime);  // Test legacy method
        assertThat(result.getProcessingTime()).isEqualTo(processingTime);
        assertThat(result.getEvaluationResults()).isEqualTo(evaluationResults);
    }

    @Test
    void testDefaultConstructor_ShouldCreateEmptyResult() {
        // When
        FraudDetectionResult result = new FraudDetectionResult();

        // Then
        assertThat(result.getTransactionId()).isNull();
        assertThat(result.isFraudulent()).isFalse();
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.getRiskLevel()).isNull();
        assertThat(result.getReason()).isNull();
        assertThat(result.getDetectionTime()).isNull();
        assertThat(result.getDetectionTimestamp()).isNull();
        assertThat(result.getProcessingTime()).isEqualTo(0L);
        assertThat(result.getEvaluationResults()).isNull();
        assertThat(result.getTriggeredRules()).isNotNull().isEmpty();  // Default list
    }

    @Test
    void testBuilderWithDifferentValues_ShouldCreateDifferentResults() {
        // Given & When
        FraudDetectionResult lowRisk = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .isFraudulent(false)
                .riskScore(0.3)
                .riskLevel("LOW")
                .reason("Low risk")
                .processingTime(50L)
                .build();

        FraudDetectionResult highRisk = FraudDetectionResult.builder()
                .transactionId("TXN_456")
                .isFraudulent(true)
                .riskScore(0.8)
                .riskLevel("HIGH")
                .reason("High risk")
                .processingTime(100L)
                .build();

        // Then
        assertThat(lowRisk.getTransactionId()).isEqualTo("TXN_123");
        assertThat(lowRisk.isFraudulent()).isFalse();
        assertThat(lowRisk.getRiskScore()).isEqualTo(0.3);
        assertThat(lowRisk.getRiskLevel()).isEqualTo("LOW");
        
        assertThat(highRisk.getTransactionId()).isEqualTo("TXN_456");
        assertThat(highRisk.isFraudulent()).isTrue();
        assertThat(highRisk.getRiskScore()).isEqualTo(0.8);
        assertThat(highRisk.getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    void testEquals_WithIdenticalResults_ShouldReturnTrue() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();

        FraudDetectionResult result1 = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .isFraudulent(true)
                .riskScore(0.85)
                .riskLevel("HIGH")
                .reason("High risk")
                .detectionTime(timestamp)
                .processingTime(100L)
                .build();

        FraudDetectionResult result2 = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .isFraudulent(true)
                .riskScore(0.85)
                .riskLevel("HIGH")
                .reason("High risk")
                .detectionTime(timestamp)
                .processingTime(100L)
                .build();

        // When & Then
        assertThat(result1.equals(result2)).isTrue();
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    void testEquals_WithDifferentResults_ShouldReturnFalse() {
        // Given
        FraudDetectionResult result1 = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .isFraudulent(true)
                .riskScore(0.85)
                .build();

        FraudDetectionResult result2 = FraudDetectionResult.builder()
                .transactionId("TXN_456")
                .isFraudulent(true)
                .riskScore(0.85)
                .build();

        // When & Then
        assertThat(result1.equals(result2)).isFalse();
    }

    @Test
    void testEquals_WithNull_ShouldReturnFalse() {
        // Given
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .build();

        // When & Then
        assertThat(result.equals(null)).isFalse();
    }

    @Test
    void testEquals_WithDifferentClass_ShouldReturnFalse() {
        // Given
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .build();

        // When & Then
        assertThat(result.equals("not a result")).isFalse();
    }

    @Test
    void testEquals_WithSameInstance_ShouldReturnTrue() {
        // Given
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .build();

        // When & Then
        assertThat(result.equals(result)).isTrue();
    }

    @Test
    void testHashCode_ShouldBeConsistent() {
        // Given
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .isFraudulent(true)
                .riskScore(0.85)
                .riskLevel("HIGH")
                .reason("High risk")
                .build();

        // When
        int hashCode1 = result.hashCode();
        int hashCode2 = result.hashCode();

        // Then
        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void testToString_ShouldContainAllFields() {
        // Given
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .isFraudulent(true)
                .riskScore(0.85)
                .riskLevel("HIGH")
                .reason("High risk transaction")
                .processingTime(100L)
                .evaluationStatus("COMPLETED")
                .alertStatus("PENDING")
                .build();

        // When
        String toString = result.toString();

        // Then
        assertThat(toString).contains("TXN_123");
        assertThat(toString).contains("true");
        assertThat(toString).contains("0.85");
        assertThat(toString).contains("HIGH");
        assertThat(toString).contains("High risk transaction");
        assertThat(toString).contains("100");
        assertThat(toString).contains("COMPLETED");
        assertThat(toString).contains("PENDING");
    }

    @Test
    void testSetters_ShouldUpdateFields() {
        // Given
        FraudDetectionResult result = new FraudDetectionResult();
        LocalDateTime timestamp = LocalDateTime.now();
        List<RuleEvaluationResult> evalResults = Arrays.asList(
                RuleEvaluationResult.builder().ruleName("RULE1").build()
        );

        // When
        result.setTransactionId("TXN_123");
        result.setFraud(true);
        result.setRiskScore(0.75);
        result.setRiskLevel("MEDIUM");
        result.setReason("Updated reason");
        result.setDetectionTimestamp(timestamp);
        result.setProcessingTime(150L);
        result.setEvaluationResults(evalResults);
        result.setEvaluationStatus("COMPLETED");
        result.setAlertStatus("SENT");

        // Then
        assertThat(result.getTransactionId()).isEqualTo("TXN_123");
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(0.75);
        assertThat(result.getRiskLevel()).isEqualTo("MEDIUM");
        assertThat(result.getReason()).isEqualTo("Updated reason");
        assertThat(result.getDetectionTimestamp()).isEqualTo(timestamp);
        assertThat(result.getProcessingTime()).isEqualTo(150L);
        assertThat(result.getEvaluationResults()).isEqualTo(evalResults);
        assertThat(result.getEvaluationStatus()).isEqualTo("COMPLETED");
        assertThat(result.getAlertStatus()).isEqualTo("SENT");
    }

    @Test
    void testRiskScoreEdgeCases() {
        // Test minimum risk score
        FraudDetectionResult minResult = FraudDetectionResult.builder()
                .riskScore(0.0)
                .build();
        assertThat(minResult.getRiskScore()).isEqualTo(0.0);

        // Test maximum risk score
        FraudDetectionResult maxResult = FraudDetectionResult.builder()
                .riskScore(1.0)
                .build();
        assertThat(maxResult.getRiskScore()).isEqualTo(1.0);

        // Test negative risk score
        FraudDetectionResult negativeResult = FraudDetectionResult.builder()
                .riskScore(-0.1)
                .build();
        assertThat(negativeResult.getRiskScore()).isEqualTo(-0.1);
    }

    @Test
    void testNullEvaluationResults_ShouldHandleGracefully() {
        // Given
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .evaluationResults(null)
                .build();

        // When & Then
        assertThat(result.getEvaluationResults()).isNull();
    }

    @Test
    void testEmptyEvaluationResults_ShouldHandleGracefully() {
        // Given
        List<RuleEvaluationResult> emptyResults = Arrays.asList();
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .evaluationResults(emptyResults)
                .build();

        // When & Then
        assertThat(result.getEvaluationResults()).isEmpty();
    }

    @Test
    void testTriggeredRulesDefault_ShouldBeEmptyList() {
        // When
        FraudDetectionResult result = FraudDetectionResult.builder()
                .transactionId("TXN_123")
                .build();

        // Then
        assertThat(result.getTriggeredRules()).isNotNull().isEmpty();
    }

    @Test
    void testLegacyMethods_ShouldWork() {
        // Given
        FraudDetectionResult result = new FraudDetectionResult();
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        result.setFraud(true);
        result.setDetectionTimestamp(timestamp);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.isFraudulent()).isTrue();
        assertThat(result.getDetectionTimestamp()).isEqualTo(timestamp);
        assertThat(result.getDetectionTime()).isEqualTo(timestamp);
    }
} 