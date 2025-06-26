package com.faud.frauddetection.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RuleEvaluationTypeTest {
    
    @Test
    void testEnumValues_ShouldHaveCorrectCount() {
        // When
        RuleEvaluationType[] values = RuleEvaluationType.values();
        
        // Then
        assertThat(values).hasSize(3);
    }
    
    @Test
    void testEnumValues_ShouldContainExpectedValues() {
        // When
        RuleEvaluationType[] values = RuleEvaluationType.values();
        
        // Then
        assertThat(values).contains(
            RuleEvaluationType.SINGLE_CONDITION,
            RuleEvaluationType.MULTI_CONDITION,
            RuleEvaluationType.INVALID
        );
    }
    
    @Test
    void testSingleCondition_ShouldExist() {
        // When
        RuleEvaluationType type = RuleEvaluationType.SINGLE_CONDITION;
        
        // Then
        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("SINGLE_CONDITION");
    }
    
    @Test
    void testMultiCondition_ShouldExist() {
        // When
        RuleEvaluationType type = RuleEvaluationType.MULTI_CONDITION;
        
        // Then
        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("MULTI_CONDITION");
    }
    
    @Test
    void testInvalid_ShouldExist() {
        // When
        RuleEvaluationType type = RuleEvaluationType.INVALID;
        
        // Then
        assertThat(type).isNotNull();
        assertThat(type.name()).isEqualTo("INVALID");
    }
    
    @Test
    void testValueOf_WithValidNames_ShouldReturnCorrectEnum() {
        // When & Then
        assertThat(RuleEvaluationType.valueOf("SINGLE_CONDITION"))
            .isEqualTo(RuleEvaluationType.SINGLE_CONDITION);
        assertThat(RuleEvaluationType.valueOf("MULTI_CONDITION"))
            .isEqualTo(RuleEvaluationType.MULTI_CONDITION);
        assertThat(RuleEvaluationType.valueOf("INVALID"))
            .isEqualTo(RuleEvaluationType.INVALID);
    }
    
    @Test
    void testValueOf_WithInvalidName_ShouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            RuleEvaluationType.valueOf("UNKNOWN_TYPE");
        });
    }
    
    @Test
    void testValueOf_WithNullName_ShouldThrowException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            RuleEvaluationType.valueOf(null);
        });
    }
    
    @Test
    void testOrdinal_ShouldReturnCorrectOrder() {
        // When & Then
        assertThat(RuleEvaluationType.SINGLE_CONDITION.ordinal()).isEqualTo(0);
        assertThat(RuleEvaluationType.MULTI_CONDITION.ordinal()).isEqualTo(1);
        assertThat(RuleEvaluationType.INVALID.ordinal()).isEqualTo(2);
    }
    
    @Test
    void testEquals_SameEnum_ShouldBeEqual() {
        // When & Then
        assertThat(RuleEvaluationType.SINGLE_CONDITION)
            .isEqualTo(RuleEvaluationType.SINGLE_CONDITION);
        assertThat(RuleEvaluationType.MULTI_CONDITION)
            .isEqualTo(RuleEvaluationType.MULTI_CONDITION);
        assertThat(RuleEvaluationType.INVALID)
            .isEqualTo(RuleEvaluationType.INVALID);
    }
    
    @Test
    void testEquals_DifferentEnum_ShouldNotBeEqual() {
        // When & Then
        assertThat(RuleEvaluationType.SINGLE_CONDITION)
            .isNotEqualTo(RuleEvaluationType.MULTI_CONDITION);
        assertThat(RuleEvaluationType.MULTI_CONDITION)
            .isNotEqualTo(RuleEvaluationType.INVALID);
        assertThat(RuleEvaluationType.INVALID)
            .isNotEqualTo(RuleEvaluationType.SINGLE_CONDITION);
    }
    
    @Test
    void testHashCode_SameEnum_ShouldBeSame() {
        // When & Then
        assertThat(RuleEvaluationType.SINGLE_CONDITION.hashCode())
            .isEqualTo(RuleEvaluationType.SINGLE_CONDITION.hashCode());
        assertThat(RuleEvaluationType.MULTI_CONDITION.hashCode())
            .isEqualTo(RuleEvaluationType.MULTI_CONDITION.hashCode());
        assertThat(RuleEvaluationType.INVALID.hashCode())
            .isEqualTo(RuleEvaluationType.INVALID.hashCode());
    }
    
    @Test
    void testToString_ShouldReturnEnumName() {
        // When & Then
        assertThat(RuleEvaluationType.SINGLE_CONDITION.toString())
            .isEqualTo("SINGLE_CONDITION");
        assertThat(RuleEvaluationType.MULTI_CONDITION.toString())
            .isEqualTo("MULTI_CONDITION");
        assertThat(RuleEvaluationType.INVALID.toString())
            .isEqualTo("INVALID");
    }
    
    @Test
    void testCompareTo_ShouldCompareByOrdinal() {
        // When & Then
        assertThat(RuleEvaluationType.SINGLE_CONDITION)
            .isLessThan(RuleEvaluationType.MULTI_CONDITION);
        assertThat(RuleEvaluationType.MULTI_CONDITION)
            .isLessThan(RuleEvaluationType.INVALID);
        assertThat(RuleEvaluationType.INVALID)
            .isGreaterThan(RuleEvaluationType.SINGLE_CONDITION);
    }
    
    @Test 
    void testSwitchStatement_ShouldWorkWithAllValues() {
        // Given
        for (RuleEvaluationType type : RuleEvaluationType.values()) {
            String result;
            
            // When
            switch (type) {
                case SINGLE_CONDITION:
                    result = "single";
                    break;
                case MULTI_CONDITION:
                    result = "multi";
                    break;
                case INVALID:
                    result = "invalid";
                    break;
                default:
                    result = "unknown";
                    break;
            }
            
            // Then
            assertThat(result).isNotEqualTo("unknown");
        }
    }
} 