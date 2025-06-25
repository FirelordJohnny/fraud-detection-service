package com.faud.frauddetection.dto;

import com.faud.frauddetection.dto.TransactionStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Transaction implements Serializable {

    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String currency;
    private String merchant;
    private String country;
    private String paymentMethod;
    private TransactionStatus status;
    private String ipAddress;
    private String userAgent;

} 