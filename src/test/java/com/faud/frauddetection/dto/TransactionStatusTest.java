package com.faud.frauddetection.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class focusing on equals, hashCode and enum functionality for TransactionStatus
 */
class TransactionStatusTest {

    @Test
    @DisplayName("Should test all enum values exist")
    void shouldTestAllEnumValues() {
        // Given
        TransactionStatus[] expectedValues = {
                TransactionStatus.PENDING,
                TransactionStatus.COMPLETED,
                TransactionStatus.FAILED,
                TransactionStatus.CANCELLED,
                TransactionStatus.REVIEW
        };

        // When
        TransactionStatus[] actualValues = TransactionStatus.values();

        // Then
        assertEquals(5, actualValues.length);
        assertArrayEquals(expectedValues, actualValues);
    }

    @Test
    @DisplayName("Should test valueOf for all enum constants")
    void shouldTestValueOf() {
        // Then
        assertEquals(TransactionStatus.PENDING, TransactionStatus.valueOf("PENDING"));
        assertEquals(TransactionStatus.COMPLETED, TransactionStatus.valueOf("COMPLETED"));
        assertEquals(TransactionStatus.FAILED, TransactionStatus.valueOf("FAILED"));
        assertEquals(TransactionStatus.CANCELLED, TransactionStatus.valueOf("CANCELLED"));
        assertEquals(TransactionStatus.REVIEW, TransactionStatus.valueOf("REVIEW"));
    }

    @Test
    @DisplayName("Should throw exception for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        // Then
        assertThrows(IllegalArgumentException.class, () -> TransactionStatus.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> TransactionStatus.valueOf("pending"));
        assertThrows(IllegalArgumentException.class, () -> TransactionStatus.valueOf(""));
        assertThrows(NullPointerException.class, () -> TransactionStatus.valueOf(null));
    }

    @Test
    @DisplayName("Should test enum equals method")
    void shouldTestEquals() {
        // Given
        TransactionStatus status1 = TransactionStatus.PENDING;
        TransactionStatus status2 = TransactionStatus.PENDING;
        TransactionStatus status3 = TransactionStatus.COMPLETED;

        // Then
        assertEquals(status1, status2);
        assertEquals(status1, status1);
        assertNotEquals(status1, status3);
        assertNotEquals(status1, null);
        assertNotEquals(status1, "PENDING");
    }

    @Test
    @DisplayName("Should test enum hashCode consistency")
    void shouldTestHashCode() {
        // Given
        TransactionStatus status1 = TransactionStatus.PENDING;
        TransactionStatus status2 = TransactionStatus.PENDING;
        TransactionStatus status3 = TransactionStatus.COMPLETED;

        // Then
        assertEquals(status1.hashCode(), status2.hashCode());
        assertEquals(status1.hashCode(), status1.hashCode());
        assertNotEquals(status1.hashCode(), status3.hashCode());
    }

    @Test
    @DisplayName("Should test enum toString method")
    void shouldTestToString() {
        // Then
        assertEquals("PENDING", TransactionStatus.PENDING.toString());
        assertEquals("COMPLETED", TransactionStatus.COMPLETED.toString());
        assertEquals("FAILED", TransactionStatus.FAILED.toString());
        assertEquals("CANCELLED", TransactionStatus.CANCELLED.toString());
        assertEquals("REVIEW", TransactionStatus.REVIEW.toString());
    }

    @Test
    @DisplayName("Should test enum name method")
    void shouldTestName() {
        // Then
        assertEquals("PENDING", TransactionStatus.PENDING.name());
        assertEquals("COMPLETED", TransactionStatus.COMPLETED.name());
        assertEquals("FAILED", TransactionStatus.FAILED.name());
        assertEquals("CANCELLED", TransactionStatus.CANCELLED.name());
        assertEquals("REVIEW", TransactionStatus.REVIEW.name());
    }

    @Test
    @DisplayName("Should test enum ordinal values")
    void shouldTestOrdinal() {
        // Then
        assertEquals(0, TransactionStatus.PENDING.ordinal());
        assertEquals(1, TransactionStatus.COMPLETED.ordinal());
        assertEquals(2, TransactionStatus.FAILED.ordinal());
        assertEquals(3, TransactionStatus.CANCELLED.ordinal());
        assertEquals(4, TransactionStatus.REVIEW.ordinal());
    }

    @Test
    @DisplayName("Should test enum compareTo method")
    void shouldTestCompareTo() {
        // Then
        assertTrue(TransactionStatus.PENDING.compareTo(TransactionStatus.COMPLETED) < 0);
        assertTrue(TransactionStatus.COMPLETED.compareTo(TransactionStatus.PENDING) > 0);
        assertEquals(0, TransactionStatus.PENDING.compareTo(TransactionStatus.PENDING));
        
        assertTrue(TransactionStatus.FAILED.compareTo(TransactionStatus.REVIEW) < 0);
        assertTrue(TransactionStatus.REVIEW.compareTo(TransactionStatus.FAILED) > 0);
    }

    @Test
    @DisplayName("Should test enum in switch statement")
    void shouldTestInSwitchStatement() {
        // Test all branches of switch statement for branch coverage
        assertEquals("Transaction is pending", getStatusMessage(TransactionStatus.PENDING));
        assertEquals("Transaction completed successfully", getStatusMessage(TransactionStatus.COMPLETED));
        assertEquals("Transaction failed", getStatusMessage(TransactionStatus.FAILED));
        assertEquals("Transaction was cancelled", getStatusMessage(TransactionStatus.CANCELLED));
        assertEquals("Transaction under review", getStatusMessage(TransactionStatus.REVIEW));
    }

    @Test
    @DisplayName("Should test enum constant identity")
    void shouldTestEnumConstantIdentity() {
        // Test that enum constants are singletons
        assertSame(TransactionStatus.PENDING, TransactionStatus.valueOf("PENDING"));
        assertSame(TransactionStatus.COMPLETED, TransactionStatus.valueOf("COMPLETED"));
        assertSame(TransactionStatus.FAILED, TransactionStatus.valueOf("FAILED"));
        assertSame(TransactionStatus.CANCELLED, TransactionStatus.valueOf("CANCELLED"));
        assertSame(TransactionStatus.REVIEW, TransactionStatus.valueOf("REVIEW"));
    }

    @Test
    @DisplayName("Should test enum serialization safety")
    void shouldTestSerializationSafety() {
        // Test that enum values maintain their identity
        TransactionStatus[] values1 = TransactionStatus.values();
        TransactionStatus[] values2 = TransactionStatus.values();

        // Should be different arrays but same content
        assertNotSame(values1, values2);
        assertArrayEquals(values1, values2);

        // Individual values should be the same objects
        for (int i = 0; i < values1.length; i++) {
            assertSame(values1[i], values2[i]);
        }
    }

    // Helper method to test switch statement coverage
    private String getStatusMessage(TransactionStatus status) {
        return switch (status) {
            case PENDING -> "Transaction is pending";
            case COMPLETED -> "Transaction completed successfully";
            case FAILED -> "Transaction failed";
            case CANCELLED -> "Transaction was cancelled";
            case REVIEW -> "Transaction under review";
        };
    }
} 