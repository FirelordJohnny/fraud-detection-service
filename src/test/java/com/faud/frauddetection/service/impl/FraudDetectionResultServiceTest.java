package com.faud.frauddetection.service.impl;

import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import com.faud.frauddetection.repository.FraudDetectionResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudDetectionResultService
 */
@ExtendWith(MockitoExtension.class)
class FraudDetectionResultServiceTest {

    @Mock
    private FraudDetectionResultRepository resultRepository;

    @InjectMocks
    private FraudDetectionResultServiceImpl fraudDetectionResultService;

    private FraudDetectionResult testResultDto;
    private FraudDetectionResultEntity testResultEntity;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testResultDto = FraudDetectionResult.builder()
            .transactionId("TXN-001")
            .isFraudulent(true)
            .riskScore(85.5)
            .riskLevel("HIGH")
            .reason("Amount exceeds threshold")
            .detectionTime(now)
            .processingTime(150L)
            .build();

        testResultEntity = new FraudDetectionResultEntity();
        testResultEntity.setId(1L);
        testResultEntity.setTransactionId("TXN-001");
        testResultEntity.setFraud(true);
        testResultEntity.setRiskScore(85.5);
        testResultEntity.setReason("Amount exceeds threshold");
        testResultEntity.setDetectionTimestamp(now);
    }

    // CREATE tests
    @Test
    void saveResult_ValidResult_ShouldSaveSuccessfully() {
        // Given
        when(resultRepository.save(any(FraudDetectionResultEntity.class))).thenReturn(testResultEntity);

        // When
        fraudDetectionResultService.saveResult(testResultDto);

        // Then
        ArgumentCaptor<FraudDetectionResultEntity> entityCaptor = ArgumentCaptor.forClass(FraudDetectionResultEntity.class);
        verify(resultRepository).save(entityCaptor.capture());
        
        FraudDetectionResultEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getTransactionId()).isEqualTo("TXN-001");
        assertThat(savedEntity.isFraud()).isTrue();
        assertThat(savedEntity.getRiskScore()).isEqualTo(85.5);
        assertThat(savedEntity.getReason()).isEqualTo("Amount exceeds threshold");
        assertThat(savedEntity.getDetectionTimestamp()).isEqualTo(testResultDto.getDetectionTime());
    }

    @Test
    void saveResult_NullResult_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> fraudDetectionResultService.saveResult(null))
            .isInstanceOf(NullPointerException.class);

        verify(resultRepository, never()).save(any());
    }

    @Test
    void saveResult_FraudulentTransaction_ShouldMapCorrectly() {
        // Given
        FraudDetectionResult fraudResult = FraudDetectionResult.builder()
            .transactionId("TXN-FRAUD")
            .isFraudulent(true)
            .riskScore(95.0)
            .reason("Multiple risk factors detected")
            .detectionTime(LocalDateTime.now())
            .build();

        when(resultRepository.save(any(FraudDetectionResultEntity.class))).thenReturn(new FraudDetectionResultEntity());

        // When
        fraudDetectionResultService.saveResult(fraudResult);

        // Then
        ArgumentCaptor<FraudDetectionResultEntity> entityCaptor = ArgumentCaptor.forClass(FraudDetectionResultEntity.class);
        verify(resultRepository).save(entityCaptor.capture());
        
        FraudDetectionResultEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getTransactionId()).isEqualTo("TXN-FRAUD");
        assertThat(savedEntity.isFraud()).isTrue();
        assertThat(savedEntity.getRiskScore()).isEqualTo(95.0);
        assertThat(savedEntity.getReason()).isEqualTo("Multiple risk factors detected");
    }

    @Test
    void saveResult_NonFraudulentTransaction_ShouldMapCorrectly() {
        // Given
        FraudDetectionResult normalResult = FraudDetectionResult.builder()
            .transactionId("TXN-NORMAL")
            .isFraudulent(false)
            .riskScore(25.0)
            .reason("All checks passed")
            .detectionTime(LocalDateTime.now())
            .build();

        when(resultRepository.save(any(FraudDetectionResultEntity.class))).thenReturn(new FraudDetectionResultEntity());

        // When
        fraudDetectionResultService.saveResult(normalResult);

        // Then
        ArgumentCaptor<FraudDetectionResultEntity> entityCaptor = ArgumentCaptor.forClass(FraudDetectionResultEntity.class);
        verify(resultRepository).save(entityCaptor.capture());
        
        FraudDetectionResultEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getTransactionId()).isEqualTo("TXN-NORMAL");
        assertThat(savedEntity.isFraud()).isFalse();
        assertThat(savedEntity.getRiskScore()).isEqualTo(25.0);
        assertThat(savedEntity.getReason()).isEqualTo("All checks passed");
    }

    // READ tests
    @Test
    void getAllResults_ShouldReturnAllResults() {
        // Given
        FraudDetectionResultEntity entity2 = new FraudDetectionResultEntity();
        entity2.setId(2L);
        entity2.setTransactionId("TXN-002");
        entity2.setFraud(false);
        entity2.setRiskScore(30.0);
        entity2.setReason("Normal transaction");

        List<FraudDetectionResultEntity> entities = Arrays.asList(testResultEntity, entity2);
        when(resultRepository.findAll()).thenReturn(entities);

        // When
        List<FraudDetectionResultEntity> results = fraudDetectionResultService.getAllResults();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(0).getTransactionId()).isEqualTo("TXN-001");
        assertThat(results.get(1).getId()).isEqualTo(2L);
        assertThat(results.get(1).getTransactionId()).isEqualTo("TXN-002");
        verify(resultRepository).findAll();
    }

    @Test
    void getAllResults_EmptyRepository_ShouldReturnEmptyList() {
        // Given
        when(resultRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<FraudDetectionResultEntity> results = fraudDetectionResultService.getAllResults();

        // Then
        assertThat(results).isEmpty();
        verify(resultRepository).findAll();
    }

    @Test
    void getResultById_ExistingResult_ShouldReturnResult() {
        // Given
        when(resultRepository.findById(1L)).thenReturn(Optional.of(testResultEntity));

        // When
        Optional<FraudDetectionResultEntity> result = fraudDetectionResultService.getResultById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getTransactionId()).isEqualTo("TXN-001");
        assertThat(result.get().isFraud()).isTrue();
        assertThat(result.get().getRiskScore()).isEqualTo(85.5);
        verify(resultRepository).findById(1L);
    }

    @Test
    void getResultById_NonExistentResult_ShouldReturnEmpty() {
        // Given
        when(resultRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<FraudDetectionResultEntity> result = fraudDetectionResultService.getResultById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(resultRepository).findById(999L);
    }

    @Test
    void getResultById_NullId_ShouldHandleGracefully() {
        // Given
        when(resultRepository.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<FraudDetectionResultEntity> result = fraudDetectionResultService.getResultById(null);

        // Then
        assertThat(result).isEmpty();
        verify(resultRepository).findById(null);
    }

    // Edge case tests
    @Test
    void saveResult_WithMissingFields_ShouldHandleGracefully() {
        // Given
        FraudDetectionResult incompleteResult = FraudDetectionResult.builder()
            .transactionId("TXN-INCOMPLETE")
            .isFraudulent(true)
            // Missing riskScore, reason, detectionTime
            .build();

        when(resultRepository.save(any(FraudDetectionResultEntity.class))).thenReturn(new FraudDetectionResultEntity());

        // When
        fraudDetectionResultService.saveResult(incompleteResult);

        // Then
        ArgumentCaptor<FraudDetectionResultEntity> entityCaptor = ArgumentCaptor.forClass(FraudDetectionResultEntity.class);
        verify(resultRepository).save(entityCaptor.capture());
        
        FraudDetectionResultEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getTransactionId()).isEqualTo("TXN-INCOMPLETE");
        assertThat(savedEntity.isFraud()).isTrue();
        assertThat(savedEntity.getRiskScore()).isEqualTo(0.0); // default double value
        assertThat(savedEntity.getReason()).isNull();
        assertThat(savedEntity.getDetectionTimestamp()).isNull();
    }

    @Test
    void saveResult_WithBackwardCompatibilityMethods_ShouldWork() {
        // Given
        FraudDetectionResult result = new FraudDetectionResult();
        result.setTransactionId("TXN-COMPAT");
        result.setFraud(true); // Using backward compatibility method
        result.setRiskScore(75.0);
        result.setReason("Backward compatibility test");
        result.setDetectionTimestamp(LocalDateTime.now()); // Using backward compatibility method

        when(resultRepository.save(any(FraudDetectionResultEntity.class))).thenReturn(new FraudDetectionResultEntity());

        // When
        fraudDetectionResultService.saveResult(result);

        // Then
        ArgumentCaptor<FraudDetectionResultEntity> entityCaptor = ArgumentCaptor.forClass(FraudDetectionResultEntity.class);
        verify(resultRepository).save(entityCaptor.capture());
        
        FraudDetectionResultEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getTransactionId()).isEqualTo("TXN-COMPAT");
        assertThat(savedEntity.isFraud()).isTrue();
        assertThat(savedEntity.getRiskScore()).isEqualTo(75.0);
        assertThat(savedEntity.getReason()).isEqualTo("Backward compatibility test");
        assertThat(savedEntity.getDetectionTimestamp()).isNotNull();
    }

    @Test
    void saveResult_MultipleResults_ShouldSaveAll() {
        // Given
        FraudDetectionResult result1 = FraudDetectionResult.builder()
            .transactionId("TXN-BATCH-1")
            .isFraudulent(true)
            .riskScore(90.0)
            .build();

        FraudDetectionResult result2 = FraudDetectionResult.builder()
            .transactionId("TXN-BATCH-2")
            .isFraudulent(false)
            .riskScore(20.0)
            .build();

        when(resultRepository.save(any(FraudDetectionResultEntity.class))).thenReturn(new FraudDetectionResultEntity());

        // When
        fraudDetectionResultService.saveResult(result1);
        fraudDetectionResultService.saveResult(result2);

        // Then
        verify(resultRepository, times(2)).save(any(FraudDetectionResultEntity.class));
    }
} 