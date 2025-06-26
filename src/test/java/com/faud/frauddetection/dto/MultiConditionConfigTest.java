package com.faud.frauddetection.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class focusing on equals and hashCode method branch coverage for MultiConditionConfig
 */
class MultiConditionConfigTest {

    // ===== MultiConditionConfig equals and hashCode tests =====
    
    @Test
    @DisplayName("Should return true when comparing identical MultiConditionConfig objects")
    void shouldReturnTrueForIdenticalObjects() {
        // Given
        MultiConditionConfig.RuleCondition condition = MultiConditionConfig.RuleCondition.builder()
                .conditionId("cond1")
                .field("amount")
                .operator("GT")
                .value("1000")
                .build();

        MultiConditionConfig.ConditionGroup group = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group1")
                .conditions(Collections.singletonList(condition))
                .intraGroupOperator("AND")
                .build();

        MultiConditionConfig config1 = MultiConditionConfig.builder()
                .conditionGroups(Collections.singletonList(group))
                .groupLogicalOperator("OR")
                .build();

        MultiConditionConfig config2 = MultiConditionConfig.builder()
                .conditionGroups(Collections.singletonList(group))
                .groupLogicalOperator("OR")
                .build();

        // Then
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should return true when comparing same object reference")
    void shouldReturnTrueForSameReference() {
        // Given
        MultiConditionConfig config = MultiConditionConfig.builder()
                .groupLogicalOperator("AND")
                .build();

        // Then
        assertEquals(config, config);
        assertEquals(config.hashCode(), config.hashCode());
    }

    @Test
    @DisplayName("Should return false when comparing with null")
    void shouldReturnFalseForNull() {
        // Given
        MultiConditionConfig config = MultiConditionConfig.builder()
                .groupLogicalOperator("AND")
                .build();

        // Then
        assertNotEquals(config, null);
    }

    @Test
    @DisplayName("Should return false when comparing with different class")
    void shouldReturnFalseForDifferentClass() {
        // Given
        MultiConditionConfig config = MultiConditionConfig.builder()
                .groupLogicalOperator("AND")
                .build();

        // Then
        assertNotEquals(config, "string");
        assertNotEquals(config, 123);
    }

    @Test
    @DisplayName("Should return false when groupLogicalOperator differs")
    void shouldReturnFalseForDifferentGroupLogicalOperator() {
        // Given
        MultiConditionConfig config1 = MultiConditionConfig.builder()
                .groupLogicalOperator("AND")
                .build();

        MultiConditionConfig config2 = MultiConditionConfig.builder()
                .groupLogicalOperator("OR")
                .build();

        // Then
        assertNotEquals(config1, config2);
        assertNotEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should handle null groupLogicalOperator in equals")
    void shouldHandleNullGroupLogicalOperator() {
        // Given
        MultiConditionConfig config1 = MultiConditionConfig.builder()
                .groupLogicalOperator(null)
                .build();

        MultiConditionConfig config2 = MultiConditionConfig.builder()
                .groupLogicalOperator(null)
                .build();

        MultiConditionConfig config3 = MultiConditionConfig.builder()
                .groupLogicalOperator("AND")
                .build();

        // Then
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
    }

    @Test
    @DisplayName("Should return false when conditionGroups differ")
    void shouldReturnFalseForDifferentConditionGroups() {
        // Given
        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group1")
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group2")
                .build();

        MultiConditionConfig config1 = MultiConditionConfig.builder()
                .conditionGroups(Collections.singletonList(group1))
                .build();

        MultiConditionConfig config2 = MultiConditionConfig.builder()
                .conditionGroups(Collections.singletonList(group2))
                .build();

        // Then
        assertNotEquals(config1, config2);
    }

    @Test
    @DisplayName("Should handle null conditionGroups in equals")
    void shouldHandleNullConditionGroups() {
        // Given
        MultiConditionConfig config1 = MultiConditionConfig.builder()
                .conditionGroups(null)
                .build();

        MultiConditionConfig config2 = MultiConditionConfig.builder()
                .conditionGroups(null)
                .build();

        MultiConditionConfig config3 = MultiConditionConfig.builder()
                .conditionGroups(Collections.emptyList())
                .build();

        // Then
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
        assertNotEquals(config1, config3);
    }

    // ===== ConditionGroup equals and hashCode tests =====

    @Test
    @DisplayName("Should return true for identical ConditionGroup objects")
    void shouldReturnTrueForIdenticalConditionGroups() {
        // Given
        MultiConditionConfig.RuleCondition condition = MultiConditionConfig.RuleCondition.builder()
                .conditionId("cond1")
                .field("amount")
                .build();

        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group1")
                .groupName("Test Group")
                .conditions(Collections.singletonList(condition))
                .intraGroupOperator("AND")
                .groupWeight(0.5)
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group1")
                .groupName("Test Group")
                .conditions(Collections.singletonList(condition))
                .intraGroupOperator("AND")
                .groupWeight(0.5)
                .build();

        // Then
        assertEquals(group1, group2);
        assertEquals(group1.hashCode(), group2.hashCode());
    }

    @Test
    @DisplayName("Should return false when ConditionGroup groupId differs")
    void shouldReturnFalseForDifferentGroupId() {
        // Given
        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group1")
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group2")
                .build();

        // Then
        assertNotEquals(group1, group2);
    }

    @Test
    @DisplayName("Should handle null groupId in ConditionGroup equals")
    void shouldHandleNullGroupId() {
        // Given
        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .groupId(null)
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .groupId(null)
                .build();

        MultiConditionConfig.ConditionGroup group3 = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group1")
                .build();

        // Then
        assertEquals(group1, group2);
        assertEquals(group1.hashCode(), group2.hashCode());
        assertNotEquals(group1, group3);
    }

    @Test
    @DisplayName("Should return false when ConditionGroup groupName differs")
    void shouldReturnFalseForDifferentGroupName() {
        // Given
        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .groupName("Group 1")
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .groupName("Group 2")
                .build();

        // Then
        assertNotEquals(group1, group2);
    }

    @Test
    @DisplayName("Should return false when ConditionGroup intraGroupOperator differs")
    void shouldReturnFalseForDifferentIntraGroupOperator() {
        // Given
        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .intraGroupOperator("AND")
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .intraGroupOperator("OR")
                .build();

        // Then
        assertNotEquals(group1, group2);
    }

    @Test
    @DisplayName("Should return false when ConditionGroup groupWeight differs")
    void shouldReturnFalseForDifferentGroupWeight() {
        // Given
        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .groupWeight(0.5)
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .groupWeight(0.8)
                .build();

        // Then
        assertNotEquals(group1, group2);
    }

    @Test
    @DisplayName("Should handle null fields in ConditionGroup equals")
    void shouldHandleNullFieldsInConditionGroup() {
        // Given
        MultiConditionConfig.ConditionGroup group1 = MultiConditionConfig.ConditionGroup.builder()
                .groupId(null)
                .groupName(null)
                .conditions(null)
                .intraGroupOperator(null)
                .groupWeight(null)
                .build();

        MultiConditionConfig.ConditionGroup group2 = MultiConditionConfig.ConditionGroup.builder()
                .groupId(null)
                .groupName(null)
                .conditions(null)
                .intraGroupOperator(null)
                .groupWeight(null)
                .build();

        // Then
        assertEquals(group1, group2);
        assertEquals(group1.hashCode(), group2.hashCode());
    }

    @Test
    @DisplayName("Should return false when comparing ConditionGroup with null")
    void shouldReturnFalseWhenComparingConditionGroupWithNull() {
        // Given
        MultiConditionConfig.ConditionGroup group = MultiConditionConfig.ConditionGroup.builder()
                .groupId("group1")
                .build();

        // Then
        assertNotEquals(group, null);
    }

    // ===== RuleCondition equals and hashCode tests =====

    @Test
    @DisplayName("Should return true for identical RuleCondition objects")
    void shouldReturnTrueForIdenticalRuleConditions() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .conditionId("cond1")
                .field("amount")
                .operator("GT")
                .value("1000")
                .weight(0.5)
                .description("Amount check")
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .conditionId("cond1")
                .field("amount")
                .operator("GT")
                .value("1000")
                .weight(0.5)
                .description("Amount check")
                .build();

        // Then
        assertEquals(condition1, condition2);
        assertEquals(condition1.hashCode(), condition2.hashCode());
    }

    @Test
    @DisplayName("Should return false when RuleCondition conditionId differs")
    void shouldReturnFalseForDifferentConditionId() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .conditionId("cond1")
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .conditionId("cond2")
                .build();

        // Then
        assertNotEquals(condition1, condition2);
    }

    @Test
    @DisplayName("Should return false when RuleCondition field differs")
    void shouldReturnFalseForDifferentField() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .field("amount")
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .field("userId")
                .build();

        // Then
        assertNotEquals(condition1, condition2);
    }

    @Test
    @DisplayName("Should return false when RuleCondition operator differs")
    void shouldReturnFalseForDifferentOperator() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .operator("GT")
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .operator("LT")
                .build();

        // Then
        assertNotEquals(condition1, condition2);
    }

    @Test
    @DisplayName("Should return false when RuleCondition value differs")
    void shouldReturnFalseForDifferentValue() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .value("1000")
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .value("2000")
                .build();

        // Then
        assertNotEquals(condition1, condition2);
    }

    @Test
    @DisplayName("Should return false when RuleCondition weight differs")
    void shouldReturnFalseForDifferentWeight() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .weight(0.5)
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .weight(0.8)
                .build();

        // Then
        assertNotEquals(condition1, condition2);
    }

    @Test
    @DisplayName("Should return false when RuleCondition description differs")
    void shouldReturnFalseForDifferentDescription() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .description("Description 1")
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .description("Description 2")
                .build();

        // Then
        assertNotEquals(condition1, condition2);
    }

    @Test
    @DisplayName("Should handle null fields in RuleCondition equals")
    void shouldHandleNullFieldsInRuleCondition() {
        // Given
        MultiConditionConfig.RuleCondition condition1 = MultiConditionConfig.RuleCondition.builder()
                .conditionId(null)
                .field(null)
                .operator(null)
                .value(null)
                .weight(null)
                .description(null)
                .build();

        MultiConditionConfig.RuleCondition condition2 = MultiConditionConfig.RuleCondition.builder()
                .conditionId(null)
                .field(null)
                .operator(null)
                .value(null)
                .weight(null)
                .description(null)
                .build();

        // Then
        assertEquals(condition1, condition2);
        assertEquals(condition1.hashCode(), condition2.hashCode());
    }

    @Test
    @DisplayName("Should return false when comparing RuleCondition with null")
    void shouldReturnFalseWhenComparingRuleConditionWithNull() {
        // Given
        MultiConditionConfig.RuleCondition condition = MultiConditionConfig.RuleCondition.builder()
                .conditionId("cond1")
                .build();

        // Then
        assertNotEquals(condition, null);
    }
} 