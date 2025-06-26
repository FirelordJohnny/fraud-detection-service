package com.faud.frauddetection.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test class for Transaction DTO
 * Tests builder pattern, equals/hashCode methods, and field access
 */
class TransactionTest {

    @Test
    void testBuilder_WithAllFields_ShouldCreateTransaction() {
        // Given
        String transactionId = "TXN123";
        String userId = "USER456";
        BigDecimal amount = new BigDecimal("100.50");
        LocalDateTime timestamp = LocalDateTime.now();
        String ipAddress = "192.168.1.1";
        String deviceId = "DEVICE789";
        String userAgent = "Mozilla/5.0";
        String country = "US";
        String currency = "USD";
        String merchant = "Amazon";
        String paymentMethod = "CREDIT_CARD";
        TransactionStatus status = TransactionStatus.COMPLETED;

        // When
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .userId(userId)
                .amount(amount)
                .timestamp(timestamp)
                .ipAddress(ipAddress)
                .deviceId(deviceId)
                .userAgent(userAgent)
                .country(country)
                .currency(currency)
                .merchant(merchant)
                .paymentMethod(paymentMethod)
                .status(status)
                .build();

        // Then
        assertThat(transaction.getTransactionId()).isEqualTo(transactionId);
        assertThat(transaction.getUserId()).isEqualTo(userId);
        assertThat(transaction.getAmount()).isEqualTo(amount);
        assertThat(transaction.getTimestamp()).isEqualTo(timestamp);
        assertThat(transaction.getIpAddress()).isEqualTo(ipAddress);
        assertThat(transaction.getDeviceId()).isEqualTo(deviceId);
        assertThat(transaction.getUserAgent()).isEqualTo(userAgent);
        assertThat(transaction.getCountry()).isEqualTo(country);
        assertThat(transaction.getCurrency()).isEqualTo(currency);
        assertThat(transaction.getMerchant()).isEqualTo(merchant);
        assertThat(transaction.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(transaction.getStatus()).isEqualTo(status);
    }

    @Test
    void testDefaultConstructor_ShouldCreateTransactionWithDefaults() {
        // When
        Transaction transaction = new Transaction();

        // Then
        assertThat(transaction.getTransactionId()).isNotNull();
        assertThat(transaction.getUserId()).isNull();
        assertThat(transaction.getAmount()).isNull();
        assertThat(transaction.getTimestamp()).isNotNull();
        assertThat(transaction.getIpAddress()).isNull();
        assertThat(transaction.getDeviceId()).isNull();
        assertThat(transaction.getUserAgent()).isNull();
        assertThat(transaction.getCountry()).isNull();
        assertThat(transaction.getCurrency()).isNull();
        assertThat(transaction.getMerchant()).isNull();
        assertThat(transaction.getPaymentMethod()).isNull();
        assertThat(transaction.getStatus()).isNull();
    }

    @Test
    void testToBuilder_ShouldCreateCopyWithModifications() {
        // Given
        Transaction original = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .currency("USD")
                .build();

        // When
        Transaction modified = original.toBuilder()
                .amount(new BigDecimal("200.75"))
                .currency("EUR")
                .build();

        // Then
        assertThat(modified.getTransactionId()).isEqualTo(original.getTransactionId());
        assertThat(modified.getUserId()).isEqualTo(original.getUserId());
        assertThat(modified.getAmount()).isEqualTo(new BigDecimal("200.75"));
        assertThat(modified.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void testEquals_SameObject_ShouldReturnTrue() {
        // Given
        Transaction transaction = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .build();

        // When & Then
        assertThat(transaction.equals(transaction)).isTrue();
        assertThat(transaction.hashCode()).isEqualTo(transaction.hashCode());
    }

    @Test
    void testEquals_IdenticalTransactions_ShouldReturnTrue() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .timestamp(timestamp)
                .currency("USD")
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .timestamp(timestamp)
                .currency("USD")
                .build();

        // When & Then
        assertThat(transaction1.equals(transaction2)).isTrue();
        assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
    }

    @Test
    void testEquals_DifferentTransactionId_ShouldReturnFalse() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN456")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .build();

        // When & Then
        assertThat(transaction1.equals(transaction2)).isFalse();
    }

    @Test
    void testEquals_DifferentUserId_ShouldReturnFalse() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER789")
                .amount(new BigDecimal("100.50"))
                .build();

        // When & Then
        assertThat(transaction1.equals(transaction2)).isFalse();
    }

    @Test
    void testEquals_DifferentAmount_ShouldReturnFalse() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("200.75"))
                .build();

        // When & Then
        assertThat(transaction1.equals(transaction2)).isFalse();
    }

    @Test
    void testEquals_WithNullAmount_ShouldHandleCorrectly() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .build();  // No amount set, so it will be null

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .build();  // No amount set, so it will be null

        // When & Then
        assertThat(transaction1.equals(transaction2)).isTrue();
        assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
    }

    @Test
    void testEquals_WithNullAndNonNullAmount_ShouldReturnFalse() {
        // Given
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .build();  // No amount set, so it will be null

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .build();

        // When & Then
        assertThat(transaction1.equals(transaction2)).isFalse();
    }

    @Test
    void testEquals_WithNull_ShouldReturnFalse() {
        // Given
        Transaction transaction = Transaction.builder()
                .transactionId("TXN123")
                .build();

        // When & Then
        assertThat(transaction.equals(null)).isFalse();
    }

    @Test
    void testEquals_WithDifferentClass_ShouldReturnFalse() {
        // Given
        Transaction transaction = Transaction.builder()
                .transactionId("TXN123")
                .build();

        // When & Then
        assertThat(transaction.equals("NotATransaction")).isFalse();
    }

    @Test
    void testToString_ShouldContainAllFields() {
        // Given
        Transaction transaction = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .currency("USD")
                .build();

        // When
        String toString = transaction.toString();

        // Then
        assertThat(toString).contains("TXN123");
        assertThat(toString).contains("USER456");
        assertThat(toString).contains("100.50");
        assertThat(toString).contains("USD");
    }

    @Test
    void testHashCode_ConsistentWithEquals() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        Transaction transaction1 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .timestamp(timestamp)
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionId("TXN123")
                .userId("USER456")
                .amount(new BigDecimal("100.50"))
                .timestamp(timestamp)
                .build();

        // When & Then
        if (transaction1.equals(transaction2)) {
            assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode());
        }
    }
} 