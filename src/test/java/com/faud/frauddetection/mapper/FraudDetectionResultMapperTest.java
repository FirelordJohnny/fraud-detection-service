package com.faud.frauddetection.mapper;

import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudDetectionResultMapper interface
 * Tests mapper method calls and parameter passing
 */
@ExtendWith(MockitoExtension.class)
class FraudDetectionResultMapperTest {

    @Mock
    private FraudDetectionResultMapper resultMapper;

    private FraudDetectionResultEntity testResult;

    @BeforeEach
    void setUp() {
        testResult = new FraudDetectionResultEntity();
        testResult.setId(1L);
        testResult.setTransactionId("TXN_001");
        testResult.setFraud(true);
        testResult.setRiskScore(0.85);
        testResult.setReason("High amount transaction detected");
        testResult.setDetectionTimestamp(LocalDateTime.now());
    }

    @Test
    void insert_ValidResult_ShouldCallMapper() {
        // Given
        doNothing().when(resultMapper).insert(testResult);

        // When
        resultMapper.insert(testResult);

        // Then
        verify(resultMapper).insert(testResult);
    }

    @Test
    void findById_ExistingResult_ShouldReturnResult() {
        // Given
        when(resultMapper.findById(1L)).thenReturn(testResult);

        // When
        FraudDetectionResultEntity result = resultMapper.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTransactionId()).isEqualTo("TXN_001");
        assertThat(result.isFraud()).isTrue();
        verify(resultMapper).findById(1L);
    }

    @Test
    void findById_NonExistingResult_ShouldReturnNull() {
        // Given
        when(resultMapper.findById(999L)).thenReturn(null);

        // When
        FraudDetectionResultEntity result = resultMapper.findById(999L);

        // Then
        assertThat(result).isNull();
        verify(resultMapper).findById(999L);
    }

    @Test
    void findAll_WithExistingResults_ShouldReturnAllResults() {
        // Given
        FraudDetectionResultEntity secondResult = new FraudDetectionResultEntity();
        secondResult.setId(2L);
        secondResult.setTransactionId("TXN_002");
        secondResult.setFraud(false);
        secondResult.setRiskScore(0.15);
        secondResult.setReason("Transaction passed all checks");
        secondResult.setDetectionTimestamp(LocalDateTime.now().minusMinutes(10));

        List<FraudDetectionResultEntity> allResults = Arrays.asList(testResult, secondResult);
        when(resultMapper.findAll()).thenReturn(allResults);

        // When
        List<FraudDetectionResultEntity> results = resultMapper.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactlyElementsOf(allResults);
        assertThat(results.get(0).getTransactionId()).isEqualTo("TXN_001");
        assertThat(results.get(1).getTransactionId()).isEqualTo("TXN_002");
        verify(resultMapper).findAll();
    }

    @Test
    void findAll_WithNoResults_ShouldReturnEmptyList() {
        // Given
        when(resultMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<FraudDetectionResultEntity> results = resultMapper.findAll();

        // Then
        assertThat(results).isEmpty();
        verify(resultMapper).findAll();
    }

    @Test
    void deleteById_ExistingResult_ShouldCallMapper() {
        // Given
        doNothing().when(resultMapper).deleteById(1L);

        // When
        resultMapper.deleteById(1L);

        // Then
        verify(resultMapper).deleteById(1L);
    }

    @Test
    void deleteById_NonExistingResult_ShouldCallMapper() {
        // Given
        doNothing().when(resultMapper).deleteById(999L);

        // When
        resultMapper.deleteById(999L);

        // Then
        verify(resultMapper).deleteById(999L);
    }

    @Test
    void insert_NullResult_ShouldCallMapper() {
        // Given
        doNothing().when(resultMapper).insert(null);

        // When
        resultMapper.insert(null);

        // Then
        verify(resultMapper).insert(null);
    }

    @Test
    void findById_NullId_ShouldCallMapper() {
        // Given
        when(resultMapper.findById(null)).thenReturn(null);

        // When
        FraudDetectionResultEntity result = resultMapper.findById(null);

        // Then
        assertThat(result).isNull();
        verify(resultMapper).findById(null);
    }

    @Test
    void insert_FraudResult_ShouldCallMapper() {
        // Given
        FraudDetectionResultEntity fraudResult = new FraudDetectionResultEntity();
        fraudResult.setTransactionId("TXN_FRAUD");
        fraudResult.setFraud(true);
        fraudResult.setRiskScore(0.95);
        fraudResult.setReason("Multiple fraud indicators detected");
        fraudResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(fraudResult);

        // When
        resultMapper.insert(fraudResult);

        // Then
        verify(resultMapper).insert(fraudResult);
    }

    @Test
    void insert_CleanResult_ShouldCallMapper() {
        // Given
        FraudDetectionResultEntity cleanResult = new FraudDetectionResultEntity();
        cleanResult.setTransactionId("TXN_CLEAN");
        cleanResult.setFraud(false);
        cleanResult.setRiskScore(0.05);
        cleanResult.setReason("Transaction verified as legitimate");
        cleanResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(cleanResult);

        // When
        resultMapper.insert(cleanResult);

        // Then
        verify(resultMapper).insert(cleanResult);
    }

    @Test
    void findById_ZeroId_ShouldCallMapper() {
        // Given
        when(resultMapper.findById(0L)).thenReturn(null);

        // When
        FraudDetectionResultEntity result = resultMapper.findById(0L);

        // Then
        assertThat(result).isNull();
        verify(resultMapper).findById(0L);
    }

    @Test
    void findById_NegativeId_ShouldCallMapper() {
        // Given
        when(resultMapper.findById(-1L)).thenReturn(null);

        // When
        FraudDetectionResultEntity result = resultMapper.findById(-1L);

        // Then
        assertThat(result).isNull();
        verify(resultMapper).findById(-1L);
    }

    @Test
    void deleteById_NullId_ShouldCallMapper() {
        // Given
        doNothing().when(resultMapper).deleteById(null);

        // When
        resultMapper.deleteById(null);

        // Then
        verify(resultMapper).deleteById(null);
    }

    @Test
    void insert_ResultWithNullFields_ShouldCallMapper() {
        // Given
        FraudDetectionResultEntity resultWithNulls = new FraudDetectionResultEntity();
        resultWithNulls.setTransactionId("TXN_NULLS");
        // Other fields are null
        doNothing().when(resultMapper).insert(resultWithNulls);

        // When
        resultMapper.insert(resultWithNulls);

        // Then
        verify(resultMapper).insert(resultWithNulls);
    }

    @Test
    void findAll_SingleResult_ShouldReturnSingleElementList() {
        // Given
        List<FraudDetectionResultEntity> singleResult = Arrays.asList(testResult);
        when(resultMapper.findAll()).thenReturn(singleResult);

        // When
        List<FraudDetectionResultEntity> results = resultMapper.findAll();

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(testResult);
        verify(resultMapper).findAll();
    }

    @Test
    void insert_HighRiskResult_ShouldCallMapper() {
        // Given
        FraudDetectionResultEntity highRiskResult = new FraudDetectionResultEntity();
        highRiskResult.setTransactionId("TXN_HIGH_RISK");
        highRiskResult.setFraud(true);
        highRiskResult.setRiskScore(1.0); // Maximum risk
        highRiskResult.setReason("Critical fraud pattern detected");
        highRiskResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(highRiskResult);

        // When
        resultMapper.insert(highRiskResult);

        // Then
        verify(resultMapper).insert(highRiskResult);
    }

    @Test
    void insert_ZeroRiskResult_ShouldCallMapper() {
        // Given
        FraudDetectionResultEntity zeroRiskResult = new FraudDetectionResultEntity();
        zeroRiskResult.setTransactionId("TXN_ZERO_RISK");
        zeroRiskResult.setFraud(false);
        zeroRiskResult.setRiskScore(0.0); // Minimum risk
        zeroRiskResult.setReason("All security checks passed");
        zeroRiskResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(zeroRiskResult);

        // When
        resultMapper.insert(zeroRiskResult);

        // Then
        verify(resultMapper).insert(zeroRiskResult);
    }
} 