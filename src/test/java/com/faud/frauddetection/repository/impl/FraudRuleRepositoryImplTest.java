package com.faud.frauddetection.repository.impl;

import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.mapper.FraudRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudRuleRepositoryImpl
 * Tests Repository layer implementation methods and MyBatis mapper interactions
 */
@ExtendWith(MockitoExtension.class)
class FraudRuleRepositoryImplTest {

    @Mock
    private FraudRuleMapper fraudRuleMapper;

    @InjectMocks
    private FraudRuleRepositoryImpl fraudRuleRepository;

    private FraudRule testRule;
    private List<FraudRule> testRules;

    @BeforeEach
    void setUp() {
        testRule = FraudRule.builder()
            .id(1L)
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Test amount rule")
            .thresholdValue(BigDecimal.valueOf(10000))
            .conditionField("amount")
            .conditionOperator("GT")
            .conditionValue("10000")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.5))
            .priority(1)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        FraudRule disabledRule = FraudRule.builder()
            .id(2L)
            .ruleName("DISABLED_RULE")
            .ruleType("FREQUENCY")
            .description("Disabled frequency rule")
            .enabled(false)
            .build();

        testRules = Arrays.asList(testRule, disabledRule);
    }

    // Constructor test
    @Test
    void constructor_WithValidMapper_ShouldCreateInstance() {
        // Given & When
        FraudRuleRepositoryImpl repository = new FraudRuleRepositoryImpl(fraudRuleMapper);

        // Then
        assertThat(repository).isNotNull();
    }

    // findById tests
    @Test
    void findById_ExistingId_ShouldReturnRule() {
        // Given
        when(fraudRuleMapper.findById(1L)).thenReturn(Optional.of(testRule));

        // When
        Optional<FraudRule> result = fraudRuleRepository.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getRuleName()).isEqualTo("TEST_AMOUNT_RULE");
        verify(fraudRuleMapper).findById(1L);
    }

    @Test
    void findById_NonExistingId_ShouldReturnEmpty() {
        // Given
        when(fraudRuleMapper.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<FraudRule> result = fraudRuleRepository.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(fraudRuleMapper).findById(999L);
    }

    @Test
    void findById_NullId_ShouldCallMapper() {
        // Given
        when(fraudRuleMapper.findById(null)).thenReturn(Optional.empty());

        // When
        Optional<FraudRule> result = fraudRuleRepository.findById(null);

        // Then
        assertThat(result).isEmpty();
        verify(fraudRuleMapper).findById(null);
    }

    // findAll tests
    @Test
    void findAll_WithExistingRules_ShouldReturnAllRules() {
        // Given
        when(fraudRuleMapper.findAll()).thenReturn(testRules);

        // When
        List<FraudRule> result = fraudRuleRepository.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(testRules);
        verify(fraudRuleMapper).findAll();
    }

    @Test
    void findAll_WithNoRules_ShouldReturnEmptyList() {
        // Given
        when(fraudRuleMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<FraudRule> result = fraudRuleRepository.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(fraudRuleMapper).findAll();
    }

    // findAllEnabled tests
    @Test
    void findAllEnabled_WithEnabledRules_ShouldReturnOnlyEnabledRules() {
        // Given
        List<FraudRule> enabledRules = Arrays.asList(testRule);
        when(fraudRuleMapper.findAllEnabled()).thenReturn(enabledRules);

        // When
        List<FraudRule> result = fraudRuleRepository.findAllEnabled();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEnabled()).isTrue();
        assertThat(result.get(0).getRuleName()).isEqualTo("TEST_AMOUNT_RULE");
        verify(fraudRuleMapper).findAllEnabled();
    }

    @Test
    void findAllEnabled_WithNoEnabledRules_ShouldReturnEmptyList() {
        // Given
        when(fraudRuleMapper.findAllEnabled()).thenReturn(Collections.emptyList());

        // When
        List<FraudRule> result = fraudRuleRepository.findAllEnabled();

        // Then
        assertThat(result).isEmpty();
        verify(fraudRuleMapper).findAllEnabled();
    }

    // save tests
    @Test
    void save_ValidRule_ShouldCallInsert() {
        // Given
        doNothing().when(fraudRuleMapper).insert(testRule);

        // When
        fraudRuleRepository.save(testRule);

        // Then
        verify(fraudRuleMapper).insert(testRule);
    }

    @Test
    void save_NullRule_ShouldCallInsertWithNull() {
        // Given
        doNothing().when(fraudRuleMapper).insert(null);

        // When
        fraudRuleRepository.save(null);

        // Then
        verify(fraudRuleMapper).insert(null);
    }

    @Test
    void save_RuleWithoutId_ShouldCallInsert() {
        // Given
        FraudRule newRule = FraudRule.builder()
            .ruleName("NEW_RULE")
            .ruleType("CUSTOM")
            .enabled(true)
            .build();
        doNothing().when(fraudRuleMapper).insert(newRule);

        // When
        fraudRuleRepository.save(newRule);

        // Then
        verify(fraudRuleMapper).insert(newRule);
    }

    // update tests
    @Test
    void update_ExistingRule_ShouldCallUpdate() {
        // Given
        FraudRule updatedRule = FraudRule.builder()
            .id(testRule.getId())
            .ruleName(testRule.getRuleName())
            .ruleType(testRule.getRuleType())
            .description("Updated description")
            .thresholdValue(testRule.getThresholdValue())
            .conditionField(testRule.getConditionField())
            .conditionOperator(testRule.getConditionOperator())
            .conditionValue(testRule.getConditionValue())
            .enabled(testRule.getEnabled())
            .riskWeight(testRule.getRiskWeight())
            .priority(testRule.getPriority())
            .createdAt(testRule.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();
        when(fraudRuleMapper.update(updatedRule)).thenReturn(1);

        // When
        fraudRuleRepository.update(updatedRule);

        // Then
        verify(fraudRuleMapper).update(updatedRule);
    }

    @Test
    void update_NullRule_ShouldCallUpdateWithNull() {
        // Given
        when(fraudRuleMapper.update(null)).thenReturn(0);

        // When
        fraudRuleRepository.update(null);

        // Then
        verify(fraudRuleMapper).update(null);
    }

    @Test
    void update_RuleWithNullFields_ShouldCallUpdate() {
        // Given
        FraudRule ruleWithNulls = FraudRule.builder()
            .id(1L)
            .ruleName("RULE_WITH_NULLS")
            .build();
        when(fraudRuleMapper.update(ruleWithNulls)).thenReturn(1);

        // When
        fraudRuleRepository.update(ruleWithNulls);

        // Then
        verify(fraudRuleMapper).update(ruleWithNulls);
    }

    // delete tests
    @Test
    void delete_ExistingId_ShouldCallDelete() {
        // Given
        when(fraudRuleMapper.delete(1L)).thenReturn(1);

        // When
        fraudRuleRepository.delete(1L);

        // Then
        verify(fraudRuleMapper).delete(1L);
    }

    @Test
    void delete_NonExistingId_ShouldCallDelete() {
        // Given
        when(fraudRuleMapper.delete(999L)).thenReturn(0);

        // When
        fraudRuleRepository.delete(999L);

        // Then
        verify(fraudRuleMapper).delete(999L);
    }

    @Test
    void delete_NullId_ShouldCallDeleteWithNull() {
        // Given
        when(fraudRuleMapper.delete(null)).thenReturn(0);

        // When
        fraudRuleRepository.delete(null);

        // Then
        verify(fraudRuleMapper).delete(null);
    }

    // Edge case tests
    @Test
    void findById_ZeroId_ShouldCallMapper() {
        // Given
        when(fraudRuleMapper.findById(0L)).thenReturn(Optional.empty());

        // When
        Optional<FraudRule> result = fraudRuleRepository.findById(0L);

        // Then
        assertThat(result).isEmpty();
        verify(fraudRuleMapper).findById(0L);
    }

    @Test
    void findById_NegativeId_ShouldCallMapper() {
        // Given
        when(fraudRuleMapper.findById(-1L)).thenReturn(Optional.empty());

        // When
        Optional<FraudRule> result = fraudRuleRepository.findById(-1L);

        // Then
        assertThat(result).isEmpty();
        verify(fraudRuleMapper).findById(-1L);
    }

    @Test
    void save_RuleWithComplexConfig_ShouldCallInsert() {
        // Given
        FraudRule complexRule = FraudRule.builder()
            .ruleName("COMPLEX_RULE")
            .ruleType("MULTI_CONDITION")
            .ruleConfig("{\"conditions\":[{\"field\":\"amount\",\"operator\":\"GT\",\"value\":\"1000\"}]}")
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.8))
            .priority(5)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        doNothing().when(fraudRuleMapper).insert(complexRule);

        // When
        fraudRuleRepository.save(complexRule);

        // Then
        verify(fraudRuleMapper).insert(complexRule);
    }

    @Test
    void update_DisableRule_ShouldCallUpdate() {
        // Given
        FraudRule disabledRule = FraudRule.builder()
            .id(testRule.getId())
            .ruleName(testRule.getRuleName())
            .ruleType(testRule.getRuleType())
            .description(testRule.getDescription())
            .thresholdValue(testRule.getThresholdValue())
            .conditionField(testRule.getConditionField())
            .conditionOperator(testRule.getConditionOperator())
            .conditionValue(testRule.getConditionValue())
            .enabled(false)
            .riskWeight(testRule.getRiskWeight())
            .priority(testRule.getPriority())
            .createdAt(testRule.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();
        when(fraudRuleMapper.update(disabledRule)).thenReturn(1);

        // When
        fraudRuleRepository.update(disabledRule);

        // Then
        verify(fraudRuleMapper).update(disabledRule);
    }

    @Test
    void findAll_MapperThrowsException_ShouldPropagateException() {
        // Given
        when(fraudRuleMapper.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        try {
            fraudRuleRepository.findAll();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Database error");
        }
        verify(fraudRuleMapper).findAll();
    }
} 