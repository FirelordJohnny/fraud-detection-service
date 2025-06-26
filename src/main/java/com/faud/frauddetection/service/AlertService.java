package com.faud.frauddetection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.config.FraudDetectionProperties;
import com.faud.frauddetection.constant.AlertSeverity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service to handle alerting for fraudulent transactions.
 * Sends alerts via Kafka message queue and logging services.
 */
@Service
@Slf4j
public class AlertService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final FraudDetectionProperties properties;

    public AlertService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper, FraudDetectionProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Sends an alert for a detected fraudulent transaction.
     * This method sends alerts via multiple channels: Kafka, logging, and webhook.
     * 
     * @param result The result of the fraud detection.
     */
    public void sendAlert(FraudDetectionResult result) {
        if (!properties.getAlert().isEnabled() || !result.isFraudulent()) {
            return;
        }

        try {
            // Log the alert
            log.warn("🚨 FRAUD ALERT: High-risk transaction detected! Transaction ID: {}, Risk Score: {}, Reason: {}", 
                    result.getTransactionId(), result.getRiskScore(), result.getReason());

            // Send alert to Kafka topic
            sendKafkaAlert(result);
            
            // Send webhook alert if enabled
            // Note: Webhook configuration would be added to properties if needed
            // if (properties.getAlert().isWebhookEnabled()) {
            //     sendWebhookAlert(result);
            // }
            
        } catch (Exception e) {
            log.error("Failed to send fraud alert for transaction: {}", result.getTransactionId(), e);
        }
    }

    /**
     * Send fraud alert to Kafka topic for downstream processing
     */
    private void sendKafkaAlert(FraudDetectionResult result) {
        try {
            Map<String, Object> alertData = createAlertData(result);
            String alertMessage = objectMapper.writeValueAsString(alertData);
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(properties.getAlert().getKafkaTopic(), result.getTransactionId(), alertMessage);
            
            future.whenComplete((sendResult, exception) -> {
                if (exception == null) {
                    log.info("✅ Fraud alert sent to Kafka successfully for transaction: {}", result.getTransactionId());
                } else {
                    log.error("❌ Failed to send fraud alert to Kafka for transaction: {}", result.getTransactionId(), exception);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize fraud alert for transaction: {}", result.getTransactionId(), e);
        }
    }

    /**
     * Send webhook alert (placeholder for actual webhook implementation)
     */
    private void sendWebhookAlert(FraudDetectionResult result) {
        // log.info("📡 Sending webhook alert for transaction: {}", result.getTransactionId());
        // TODO: Implement actual HTTP webhook call
        // This could use RestTemplate or WebClient to send HTTP POST request
    }

    /**
     * Create structured alert data
     */
    private Map<String, Object> createAlertData(FraudDetectionResult result) {
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("alertId", "ALERT_" + System.currentTimeMillis());
        alertData.put("timestamp", LocalDateTime.now().toString());
        alertData.put("alertType", "FRAUD_DETECTION");
        alertData.put("severity", getSeverity(result.getRiskScore()));
        alertData.put("transactionId", result.getTransactionId());
        alertData.put("riskScore", result.getRiskScore());
        alertData.put("reason", result.getReason());
        alertData.put("detectionTimestamp", result.getDetectionTime());
        alertData.put("processingTime", result.getProcessingTime());
        return alertData;
    }

    /**
     * Determine alert severity based on risk score
     */
    private String getSeverity(double riskScore) {
        return AlertSeverity.getSeverity(riskScore);
    }
} 