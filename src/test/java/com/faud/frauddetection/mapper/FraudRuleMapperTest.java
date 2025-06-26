package com.faud.frauddetection.mapper;

import com.faud.frauddetection.entity.FraudRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FraudRuleMapper interface
 * Tests mapper method calls and parameter passing
 */
@ExtendWith(MockitoExtension.class)
class FraudRuleMapperTest {

    @Mock
    private FraudRuleMapper fraudRuleMapper;

    private FraudRule testRule;

    @BeforeEach
    void setUp() {
        testRule = FraudRule.builder()
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
    }

    @Test
    void insert_ValidRule_ShouldCallMapper() {
        // Given
        doNothing().when(fraudRuleMapper).insert(testRule);

        // When
        fraudRuleMapper.insert(testRule);

        // Then
        verify(fraudRuleMapper).insert(testRule);
    }

    @Test
    void findById_ExistingRule_ShouldReturnRule() {
        // Given
        when(fraudRuleMapper.findById(1L)).thenReturn(Optional.of(testRule));

        // When
        Optional<FraudRule> result = fraudRuleMapper.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRuleName()).isEqualTo("TEST_AMOUNT_RULE");
        assertThat(result.get().getRuleType()).isEqualTo("AMOUNT");
        assertThat(result.get().getEnabled()).isTrue();
        verify(fraudRuleMapper).findById(1L);
    }

    @Test
    void findById_NonExistingRule_ShouldReturnEmpty() {
        // Given
        when(fraudRuleMapper.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<FraudRule> result = fraudRuleMapper.findById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(fraudRuleMapper).findById(999L);
    }

    @Test
    void findAll_WithMultipleRules_ShouldReturnAllRules() {
        // Given
        FraudRule secondRule = FraudRule.builder()
            .ruleName("TEST_FREQUENCY_RULE")
            .ruleType("FREQUENCY")
            .description("Test frequency rule")
            .enabled(false)
            .build();
        List<FraudRule> allRules = Arrays.asList(testRule, secondRule);
        when(fraudRuleMapper.findAll()).thenReturn(allRules);

        // When
        List<FraudRule> results = fraudRuleMapper.findAll();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(FraudRule::getRuleName)
            .containsExactlyInAnyOrder("TEST_AMOUNT_RULE", "TEST_FREQUENCY_RULE");
        verify(fraudRuleMapper).findAll();
    }

    @Test
    void findAllEnabled_WithEnabledRules_ShouldReturnOnlyEnabledRules() {
        // Given
        List<FraudRule> enabledRules = Arrays.asList(testRule);
        when(fraudRuleMapper.findAllEnabled()).thenReturn(enabledRules);

        // When
        List<FraudRule> results = fraudRuleMapper.findAllEnabled();

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRuleName()).isEqualTo("TEST_AMOUNT_RULE");
        assertThat(results.get(0).getEnabled()).isTrue();
        verify(fraudRuleMapper).findAllEnabled();
    }

    @Test
    void findByRuleType_WithSpecificType_ShouldReturnMatchingRules() {
        // Given
        List<FraudRule> amountRules = Arrays.asList(testRule);
        when(fraudRuleMapper.findByRuleType("AMOUNT")).thenReturn(amountRules);

        // When
        List<FraudRule> results = fraudRuleMapper.findByRuleType("AMOUNT");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRuleType()).isEqualTo("AMOUNT");
        assertThat(results.get(0).getEnabled()).isTrue();
        verify(fraudRuleMapper).findByRuleType("AMOUNT");
    }

    @Test
    void update_ExistingRule_ShouldCallMapper() {
        // Given
        when(fraudRuleMapper.update(testRule)).thenReturn(1);

        // When
        int rowsAffected = fraudRuleMapper.update(testRule);

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        verify(fraudRuleMapper).update(testRule);
    }

    @Test
    void updateStatus_ExistingRule_ShouldCallMapper() {
        // Given
        LocalDateTime updateTime = LocalDateTime.now();
        when(fraudRuleMapper.updateStatus(1L, false, updateTime)).thenReturn(1);

        // When
        int rowsAffected = fraudRuleMapper.updateStatus(1L, false, updateTime);

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        verify(fraudRuleMapper).updateStatus(1L, false, updateTime);
    }

    @Test
    void delete_ExistingRule_ShouldCallMapper() {
        // Given
        when(fraudRuleMapper.delete(1L)).thenReturn(1);

        // When
        int rowsAffected = fraudRuleMapper.delete(1L);

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        verify(fraudRuleMapper).delete(1L);
    }

    @Test
    void delete_NonExistingRule_ShouldReturnZero() {
        // Given
        when(fraudRuleMapper.delete(999L)).thenReturn(0);

        // When
        int rowsAffected = fraudRuleMapper.delete(999L);

        // Then
        assertThat(rowsAffected).isEqualTo(0);
        verify(fraudRuleMapper).delete(999L);
    }

    @Test
    void findAll_EmptyDatabase_ShouldReturnEmptyList() {
        // Given
        when(fraudRuleMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<FraudRule> results = fraudRuleMapper.findAll();

        // Then
        assertThat(results).isEmpty();
        verify(fraudRuleMapper).findAll();
    }

    @Test
    void findAllEnabled_EmptyDatabase_ShouldReturnEmptyList() {
        // Given
        when(fraudRuleMapper.findAllEnabled()).thenReturn(Collections.emptyList());

        // When
        List<FraudRule> results = fraudRuleMapper.findAllEnabled();

        // Then
        assertThat(results).isEmpty();
        verify(fraudRuleMapper).findAllEnabled();
    }

    @Test
    void findByRuleType_NonExistingType_ShouldReturnEmptyList() {
        // Given
        when(fraudRuleMapper.findByRuleType("NON_EXISTING_TYPE")).thenReturn(Collections.emptyList());

        // When
        List<FraudRule> results = fraudRuleMapper.findByRuleType("NON_EXISTING_TYPE");

        // Then
        assertThat(results).isEmpty();
        verify(fraudRuleMapper).findByRuleType("NON_EXISTING_TYPE");
    }

    @Test
    void findAllActive_ShouldCallMapper() {
        // Given
        List<FraudRule> activeRules = Arrays.asList(testRule);
        when(fraudRuleMapper.findAllActive()).thenReturn(activeRules);

        // When
        List<FraudRule> results = fraudRuleMapper.findAllActive();

        // Then
        assertThat(results).hasSize(1);
        verify(fraudRuleMapper).findAllActive();
    }
} 