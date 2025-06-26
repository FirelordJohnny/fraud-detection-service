package com.faud.frauddetection.repository.impl;

import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import com.faud.frauddetection.mapper.FraudDetectionResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudDetectionResultRepositoryImpl
 * Tests Repository layer implementation methods and MyBatis mapper interactions
 */
@ExtendWith(MockitoExtension.class)
class FraudDetectionResultRepositoryImplTest {

    @Mock
    private FraudDetectionResultMapper resultMapper;

    @InjectMocks
    private FraudDetectionResultRepositoryImpl resultRepository;

    private FraudDetectionResultEntity testResult;
    private List<FraudDetectionResultEntity> testResults;

    @BeforeEach
    void setUp() {
        testResult = new FraudDetectionResultEntity();
        testResult.setId(1L);
        testResult.setTransactionId("TXN_001");
        testResult.setFraud(true);
        testResult.setRiskScore(0.85);
        testResult.setReason("High amount transaction detected");
        testResult.setDetectionTimestamp(LocalDateTime.now());

        FraudDetectionResultEntity cleanResult = new FraudDetectionResultEntity();
        cleanResult.setId(2L);
        cleanResult.setTransactionId("TXN_002");
        cleanResult.setFraud(false);
        cleanResult.setRiskScore(0.15);
        cleanResult.setReason("Transaction passed all checks");
        cleanResult.setDetectionTimestamp(LocalDateTime.now().minusMinutes(10));

        testResults = Arrays.asList(testResult, cleanResult);
    }

    // Constructor test
    @Test
    void constructor_WithValidMapper_ShouldCreateInstance() {
        // Given & When
        FraudDetectionResultRepositoryImpl repository = new FraudDetectionResultRepositoryImpl(resultMapper);

        // Then
        assertThat(repository).isNotNull();
    }

    // save tests
    @Test
    void save_ValidResult_ShouldCallInsertAndReturnResult() {
        // Given
        doNothing().when(resultMapper).insert(testResult);

        // When
        FraudDetectionResultEntity result = resultRepository.save(testResult);

        // Then
        assertThat(result).isEqualTo(testResult);
        assertThat(result.getTransactionId()).isEqualTo("TXN_001");
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isEqualTo(0.85);
        verify(resultMapper).insert(testResult);
    }

    @Test
    void save_NullResult_ShouldCallInsertWithNull() {
        // Given
        doNothing().when(resultMapper).insert(null);

        // When
        FraudDetectionResultEntity result = resultRepository.save(null);

        // Then
        assertThat(result).isNull();
        verify(resultMapper).insert(null);
    }

    @Test
    void save_ResultWithoutId_ShouldCallInsert() {
        // Given
        FraudDetectionResultEntity newResult = new FraudDetectionResultEntity();
        newResult.setTransactionId("TXN_NEW");
        newResult.setFraud(false);
        newResult.setRiskScore(0.25);
        newResult.setReason("New transaction processed");
        newResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(newResult);

        // When
        FraudDetectionResultEntity result = resultRepository.save(newResult);

        // Then
        assertThat(result).isEqualTo(newResult);
        assertThat(result.getTransactionId()).isEqualTo("TXN_NEW");
        verify(resultMapper).insert(newResult);
    }

    @Test
    void save_ResultWithNullFields_ShouldCallInsert() {
        // Given
        FraudDetectionResultEntity resultWithNulls = new FraudDetectionResultEntity();
        resultWithNulls.setId(999L);
        resultWithNulls.setTransactionId("TXN_NULLS");
        // Other fields are null
        doNothing().when(resultMapper).insert(resultWithNulls);

        // When
        FraudDetectionResultEntity result = resultRepository.save(resultWithNulls);

        // Then
        assertThat(result).isEqualTo(resultWithNulls);
        assertThat(result.getTransactionId()).isEqualTo("TXN_NULLS");
        assertThat(result.getReason()).isNull();
        verify(resultMapper).insert(resultWithNulls);
    }

    // findById tests
    @Test
    void findById_ExistingId_ShouldReturnResult() {
        // Given
        when(resultMapper.findById(1L)).thenReturn(testResult);

        // When
        Optional<FraudDetectionResultEntity> result = resultRepository.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTransactionId()).isEqualTo("TXN_001");
        assertThat(result.get().isFraud()).isTrue();
        verify(resultMapper).findById(1L);
    }

    @Test
    void findById_NonExistingId_ShouldReturnEmpty() {
        // Given
        when(resultMapper.findById(999L)).thenReturn(null);

        // When
        Optional<FraudDetectionResultEntity> result = resultRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(resultMapper).findById(999L);
    }

    @Test
    void findById_NullId_ShouldReturnEmpty() {
        // Given
        when(resultMapper.findById(null)).thenReturn(null);

        // When
        Optional<FraudDetectionResultEntity> result = resultRepository.findById(null);

        // Then
        assertThat(result).isEmpty();
        verify(resultMapper).findById(null);
    }

    @Test
    void findById_ZeroId_ShouldCallMapper() {
        // Given
        when(resultMapper.findById(0L)).thenReturn(null);

        // When
        Optional<FraudDetectionResultEntity> result = resultRepository.findById(0L);

        // Then
        assertThat(result).isEmpty();
        verify(resultMapper).findById(0L);
    }

    @Test
    void findById_NegativeId_ShouldCallMapper() {
        // Given
        when(resultMapper.findById(-1L)).thenReturn(null);

        // When
        Optional<FraudDetectionResultEntity> result = resultRepository.findById(-1L);

        // Then
        assertThat(result).isEmpty();
        verify(resultMapper).findById(-1L);
    }

    // findAll tests
    @Test
    void findAll_WithExistingResults_ShouldReturnAllResults() {
        // Given
        when(resultMapper.findAll()).thenReturn(testResults);

        // When
        List<FraudDetectionResultEntity> results = resultRepository.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactlyElementsOf(testResults);
        assertThat(results.get(0).getTransactionId()).isEqualTo("TXN_001");
        assertThat(results.get(1).getTransactionId()).isEqualTo("TXN_002");
        verify(resultMapper).findAll();
    }

    @Test
    void findAll_WithNoResults_ShouldReturnEmptyList() {
        // Given
        when(resultMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<FraudDetectionResultEntity> results = resultRepository.findAll();

        // Then
        assertThat(results).isEmpty();
        verify(resultMapper).findAll();
    }

    @Test
    void findAll_WithSingleResult_ShouldReturnSingleElementList() {
        // Given
        when(resultMapper.findAll()).thenReturn(Arrays.asList(testResult));

        // When
        List<FraudDetectionResultEntity> results = resultRepository.findAll();

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(testResult);
        verify(resultMapper).findAll();
    }

    // Edge case and error handling tests
    @Test
    void save_HighRiskScore_ShouldHandleCorrectly() {
        // Given
        FraudDetectionResultEntity highRiskResult = new FraudDetectionResultEntity();
        highRiskResult.setId(100L);
        highRiskResult.setTransactionId("TXN_HIGH_RISK");
        highRiskResult.setFraud(true);
        highRiskResult.setRiskScore(1.0); // Maximum risk
        highRiskResult.setReason("Multiple fraud indicators detected");
        highRiskResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(highRiskResult);

        // When
        FraudDetectionResultEntity result = resultRepository.save(highRiskResult);

        // Then
        assertThat(result.getRiskScore()).isEqualTo(1.0);
        assertThat(result.isFraud()).isTrue();
        verify(resultMapper).insert(highRiskResult);
    }

    @Test
    void save_ZeroRiskScore_ShouldHandleCorrectly() {
        // Given
        FraudDetectionResultEntity zeroRiskResult = new FraudDetectionResultEntity();
        zeroRiskResult.setId(101L);
        zeroRiskResult.setTransactionId("TXN_ZERO_RISK");
        zeroRiskResult.setFraud(false);
        zeroRiskResult.setRiskScore(0.0); // Minimum risk
        zeroRiskResult.setReason("All checks passed");
        zeroRiskResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(zeroRiskResult);

        // When
        FraudDetectionResultEntity result = resultRepository.save(zeroRiskResult);

        // Then
        assertThat(result.getRiskScore()).isEqualTo(0.0);
        assertThat(result.isFraud()).isFalse();
        verify(resultMapper).insert(zeroRiskResult);
    }

    @Test
    void save_LongTransactionId_ShouldHandleCorrectly() {
        // Given
        FraudDetectionResultEntity longIdResult = new FraudDetectionResultEntity();
        longIdResult.setTransactionId("TXN_VERY_LONG_TRANSACTION_ID_WITH_MANY_CHARACTERS_123456789");
        longIdResult.setFraud(false);
        longIdResult.setRiskScore(0.3);
        longIdResult.setReason("Long transaction ID test");
        longIdResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(longIdResult);

        // When
        FraudDetectionResultEntity result = resultRepository.save(longIdResult);

        // Then
        assertThat(result.getTransactionId()).hasSize(59);
        verify(resultMapper).insert(longIdResult);
    }

    @Test
    void findAll_MapperThrowsException_ShouldPropagateException() {
        // Given
        when(resultMapper.findAll()).thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        try {
            resultRepository.findAll();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Database connection error");
        }
        verify(resultMapper).findAll();
    }

    @Test
    void findById_MapperThrowsException_ShouldPropagateException() {
        // Given
        when(resultMapper.findById(1L)).thenThrow(new RuntimeException("SQL syntax error"));

        // When & Then
        try {
            resultRepository.findById(1L);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("SQL syntax error");
        }
        verify(resultMapper).findById(1L);
    }

    @Test
    void save_MapperThrowsException_ShouldPropagateException() {
        // Given
        doThrow(new RuntimeException("Constraint violation")).when(resultMapper).insert(testResult);

        // When & Then
        try {
            resultRepository.save(testResult);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Constraint violation");
        }
        verify(resultMapper).insert(testResult);
    }

    // Test different fraud scenarios
    @Test
    void save_FraudResult_ShouldStoreCorrectly() {
        // Given
        FraudDetectionResultEntity fraudResult = new FraudDetectionResultEntity();
        fraudResult.setTransactionId("TXN_FRAUD");
        fraudResult.setFraud(true);
        fraudResult.setRiskScore(0.95);
        fraudResult.setReason("Amount exceeds threshold");
        fraudResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(fraudResult);

        // When
        FraudDetectionResultEntity result = resultRepository.save(fraudResult);

        // Then
        assertThat(result.isFraud()).isTrue();
        assertThat(result.getRiskScore()).isGreaterThan(0.9);
        verify(resultMapper).insert(fraudResult);
    }

    @Test
    void save_CleanResult_ShouldStoreCorrectly() {
        // Given
        FraudDetectionResultEntity cleanResult = new FraudDetectionResultEntity();
        cleanResult.setTransactionId("TXN_CLEAN");
        cleanResult.setFraud(false);
        cleanResult.setRiskScore(0.05);
        cleanResult.setReason("Transaction verified as legitimate");
        cleanResult.setDetectionTimestamp(LocalDateTime.now());
        doNothing().when(resultMapper).insert(cleanResult);

        // When
        FraudDetectionResultEntity result = resultRepository.save(cleanResult);

        // Then
        assertThat(result.isFraud()).isFalse();
        assertThat(result.getRiskScore()).isLessThan(0.1);
        verify(resultMapper).insert(cleanResult);
    }
} 