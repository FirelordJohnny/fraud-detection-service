package com.faud.frauddetection.dto;

import lombok.Getter;
import lombok.ToString;
import lombok.Builder;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
public final class Transaction {

    private final String transactionId;
    private final String userId;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;
    private final String ipAddress;
    private final String deviceId;
    private final String userAgent;
    private final String country;
    private final String currency;
    private final String merchant;
    private final String paymentMethod;
    private final TransactionStatus status;

    public Transaction() {
        this.transactionId = UUID.randomUUID().toString();
        this.userId = null;
        this.amount = null;
        this.timestamp = LocalDateTime.now();
        this.ipAddress = null;
        this.deviceId = null;
        this.userAgent = null;
        this.country = null;
        this.currency = null;
        this.merchant = null;
        this.paymentMethod = null;
        this.status = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(deviceId, that.deviceId) &&
                Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(country, that.country) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(merchant, that.merchant) &&
                Objects.equals(paymentMethod, that.paymentMethod) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, userId, amount, timestamp, ipAddress, deviceId, userAgent, country, currency, merchant, paymentMethod, status);
    }
} 