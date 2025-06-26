package com.faud.frauddetection.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionResultEntityTest {
    
    private FraudDetectionResultEntity entity;
    
    @BeforeEach
    void setUp() {
        entity = new FraudDetectionResultEntity();
    }
    
    @Test
    void testDefaultConstructor_ShouldCreateEmptyEntity() {
        // When
        FraudDetectionResultEntity result = new FraudDetectionResultEntity();
        
        // Then
        assertThat(result.getId()).isNull();
        assertThat(result.getTransactionId()).isNull();
        assertThat(result.isFraud()).isFalse(); // boolean default is false
        assertThat(result.getRiskScore()).isEqualTo(0.0); // double default is 0.0
        assertThat(result.getReason()).isNull();
        assertThat(result.getDetectionTimestamp()).isNull();
    }
    
    @Test
    void testSettersAndGetters_ShouldWorkCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        entity.setId(123L);
        entity.setTransactionId("tx-12345");
        entity.setFraud(true);
        entity.setRiskScore(0.85);
        entity.setReason("High amount transaction");
        entity.setDetectionTimestamp(now);
        
        // Then
        assertThat(entity.getId()).isEqualTo(123L);
        assertThat(entity.getTransactionId()).isEqualTo("tx-12345");
        assertThat(entity.isFraud()).isTrue();
        assertThat(entity.getRiskScore()).isEqualTo(0.85);
        assertThat(entity.getReason()).isEqualTo("High amount transaction");
        assertThat(entity.getDetectionTimestamp()).isEqualTo(now);
    }
    
    @Test
    void testSetId_WithNullValue_ShouldAcceptNull() {
        // When
        entity.setId(null);
        
        // Then
        assertThat(entity.getId()).isNull();
    }
    
    @Test
    void testSetId_WithValidValue_ShouldSetCorrectly() {
        // When
        entity.setId(999L);
        
        // Then
        assertThat(entity.getId()).isEqualTo(999L);
    }
    
    @Test
    void testSetTransactionId_WithNullValue_ShouldAcceptNull() {
        // When
        entity.setTransactionId(null);
        
        // Then
        assertThat(entity.getTransactionId()).isNull();
    }
    
    @Test
    void testSetTransactionId_WithEmptyString_ShouldAcceptEmptyString() {
        // When
        entity.setTransactionId("");
        
        // Then
        assertThat(entity.getTransactionId()).isEqualTo("");
    }
    
    @Test
    void testSetTransactionId_WithValidValue_ShouldSetCorrectly() {
        // When
        entity.setTransactionId("transaction-abc-123");
        
        // Then
        assertThat(entity.getTransactionId()).isEqualTo("transaction-abc-123");
    }
    
    @Test
    void testSetFraud_WithTrueValue_ShouldSetToTrue() {
        // When
        entity.setFraud(true);
        
        // Then
        assertThat(entity.isFraud()).isTrue();
    }
    
    @Test
    void testSetFraud_WithFalseValue_ShouldSetToFalse() {
        // When
        entity.setFraud(false);
        
        // Then
        assertThat(entity.isFraud()).isFalse();
    }
    
    @Test
    void testSetRiskScore_WithValidScore_ShouldSetCorrectly() {
        // When
        entity.setRiskScore(0.75);
        
        // Then
        assertThat(entity.getRiskScore()).isEqualTo(0.75);
    }
    
    @Test
    void testSetRiskScore_WithZeroScore_ShouldSetToZero() {
        // When
        entity.setRiskScore(0.0);
        
        // Then
        assertThat(entity.getRiskScore()).isEqualTo(0.0);
    }
    
    @Test
    void testSetRiskScore_WithMaxScore_ShouldSetToMax() {
        // When
        entity.setRiskScore(1.0);
        
        // Then
        assertThat(entity.getRiskScore()).isEqualTo(1.0);
    }
    
    @Test
    void testSetRiskScore_WithNegativeScore_ShouldAcceptNegative() {
        // When
        entity.setRiskScore(-0.5);
        
        // Then
        assertThat(entity.getRiskScore()).isEqualTo(-0.5);
    }
    
    @Test
    void testSetRiskScore_WithScoreAboveOne_ShouldAcceptAboveOne() {
        // When
        entity.setRiskScore(1.5);
        
        // Then
        assertThat(entity.getRiskScore()).isEqualTo(1.5);
    }
    
    @Test
    void testSetReason_WithNullValue_ShouldAcceptNull() {
        // When
        entity.setReason(null);
        
        // Then
        assertThat(entity.getReason()).isNull();
    }
    
    @Test
    void testSetReason_WithEmptyString_ShouldAcceptEmptyString() {
        // When
        entity.setReason("");
        
        // Then
        assertThat(entity.getReason()).isEqualTo("");
    }
    
    @Test
    void testSetReason_WithValidReason_ShouldSetCorrectly() {
        // When
        entity.setReason("Multiple failed login attempts");
        
        // Then
        assertThat(entity.getReason()).isEqualTo("Multiple failed login attempts");
    }
    
    @Test
    void testSetDetectionTimestamp_WithNullValue_ShouldAcceptNull() {
        // When
        entity.setDetectionTimestamp(null);
        
        // Then
        assertThat(entity.getDetectionTimestamp()).isNull();
    }
    
    @Test
    void testSetDetectionTimestamp_WithValidTimestamp_ShouldSetCorrectly() {
        // Given
        LocalDateTime timestamp = LocalDateTime.of(2023, 12, 25, 15, 30, 45);
        
        // When
        entity.setDetectionTimestamp(timestamp);
        
        // Then
        assertThat(entity.getDetectionTimestamp()).isEqualTo(timestamp);
    }
    
    @Test
    void testEquals_WithSameValues_ShouldBeEqual() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        FraudDetectionResultEntity entity1 = new FraudDetectionResultEntity();
        entity1.setId(1L);
        entity1.setTransactionId("tx-123");
        entity1.setFraud(true);
        entity1.setRiskScore(0.8);
        entity1.setReason("High risk");
        entity1.setDetectionTimestamp(now);
        
        FraudDetectionResultEntity entity2 = new FraudDetectionResultEntity();
        entity2.setId(1L);
        entity2.setTransactionId("tx-123");
        entity2.setFraud(true);
        entity2.setRiskScore(0.8);
        entity2.setReason("High risk");
        entity2.setDetectionTimestamp(now);
        
        // When & Then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }
    
    @Test
    void testEquals_WithDifferentValues_ShouldNotBeEqual() {
        // Given
        FraudDetectionResultEntity entity1 = new FraudDetectionResultEntity();
        entity1.setId(1L);
        entity1.setTransactionId("tx-123");
        
        FraudDetectionResultEntity entity2 = new FraudDetectionResultEntity();
        entity2.setId(2L);
        entity2.setTransactionId("tx-456");
        
        // When & Then
        assertThat(entity1).isNotEqualTo(entity2);
    }
    
    @Test
    void testEquals_WithNull_ShouldNotBeEqual() {
        // Given
        FraudDetectionResultEntity entity1 = new FraudDetectionResultEntity();
        entity1.setId(1L);
        
        // When & Then
        assertThat(entity1).isNotEqualTo(null);
    }
    
    @Test
    void testEquals_WithDifferentClass_ShouldNotBeEqual() {
        // Given
        FraudDetectionResultEntity entity1 = new FraudDetectionResultEntity();
        entity1.setId(1L);
        
        String otherObject = "not an entity";
        
        // When & Then
        assertThat(entity1).isNotEqualTo(otherObject);
    }
    
    @Test
    void testEquals_WithSameReference_ShouldBeEqual() {
        // Given
        FraudDetectionResultEntity entity1 = new FraudDetectionResultEntity();
        entity1.setId(1L);
        
        // When & Then
        assertThat(entity1).isEqualTo(entity1);
    }
    
    @Test
    void testToString_ShouldNotBeEmpty() {
        // Given
        entity.setId(1L);
        entity.setTransactionId("tx-123");
        entity.setFraud(true);
        entity.setRiskScore(0.9);
        entity.setReason("Suspicious activity");
        entity.setDetectionTimestamp(LocalDateTime.now());
        
        // When
        String result = entity.toString();
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).contains("FraudDetectionResultEntity");
    }
    
    @Test
    void testToString_WithNullValues_ShouldNotThrowException() {
        // Given
        entity.setId(null);
        entity.setTransactionId(null);
        entity.setReason(null);
        entity.setDetectionTimestamp(null);
        
        // When & Then
        assertDoesNotThrow(() -> entity.toString());
        assertThat(entity.toString()).isNotEmpty();
    }
    
    @Test
    void testHashCode_SameValues_ShouldProduceSameHashCode() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        FraudDetectionResultEntity entity1 = new FraudDetectionResultEntity();
        entity1.setId(1L);
        entity1.setTransactionId("tx-123");
        entity1.setFraud(true);
        entity1.setRiskScore(0.8);
        entity1.setReason("High risk");
        entity1.setDetectionTimestamp(now);
        
        FraudDetectionResultEntity entity2 = new FraudDetectionResultEntity();
        entity2.setId(1L);
        entity2.setTransactionId("tx-123");
        entity2.setFraud(true);
        entity2.setRiskScore(0.8);
        entity2.setReason("High risk");
        entity2.setDetectionTimestamp(now);
        
        // When & Then
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }
    
    @Test
    void testHashCode_WithNullValues_ShouldNotThrowException() {
        // Given
        entity.setId(null);
        entity.setTransactionId(null);
        entity.setReason(null);
        entity.setDetectionTimestamp(null);
        
        // When & Then
        assertDoesNotThrow(() -> entity.hashCode());
    }
    
    @Test
    void testBooleanFraudField_ShouldWorkWithPrimitiveBoolean() {
        // Given
        entity.setFraud(true);
        
        // When
        boolean isFraud = entity.isFraud();
        
        // Then
        assertThat(isFraud).isTrue();
    }
    
    @Test
    void testDoublePrimitiveRiskScore_ShouldWorkWithPrimitiveDouble() {
        // Given
        entity.setRiskScore(0.123456789);
        
        // When
        double riskScore = entity.getRiskScore();
        
        // Then
        assertThat(riskScore).isEqualTo(0.123456789);
    }
    
    @Test
    void testCompleteEntity_ShouldHandleAllFieldsCorrectly() {
        // Given
        LocalDateTime detectionTime = LocalDateTime.of(2023, 6, 15, 10, 30, 0);
        
        // When
        entity.setId(100L);
        entity.setTransactionId("FRAUD-TX-001");
        entity.setFraud(true);
        entity.setRiskScore(0.95);
        entity.setReason("Multiple risk factors detected: high amount, suspicious location, unusual time");
        entity.setDetectionTimestamp(detectionTime);
        
        // Then
        assertThat(entity.getId()).isEqualTo(100L);
        assertThat(entity.getTransactionId()).isEqualTo("FRAUD-TX-001");
        assertThat(entity.isFraud()).isTrue();
        assertThat(entity.getRiskScore()).isEqualTo(0.95);
        assertThat(entity.getReason()).isEqualTo("Multiple risk factors detected: high amount, suspicious location, unusual time");
        assertThat(entity.getDetectionTimestamp()).isEqualTo(detectionTime);
    }
} 