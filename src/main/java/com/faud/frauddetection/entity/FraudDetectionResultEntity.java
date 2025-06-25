package com.faud.frauddetection.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FraudDetectionResultEntity {

    private Long id;
    private String transactionId;
    private boolean isFraud;
    private double riskScore;
    private String reason;
    private LocalDateTime detectionTimestamp;

}