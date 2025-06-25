package com.faud.frauddetection.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faud.frauddetection.dto.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Transaction Message Consumer
 */
@Service
@Slf4j
public class TransactionConsumer {

    private final FraudDetectionService fraudDetectionService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TransactionConsumer(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // To handle LocalDateTime
    }

    /**
     * Consume transaction messages
     */
    @KafkaListener(topics = "transactions", groupId = "fraud-detection-group")
    public void consume(String message) {
        try {
            Transaction transaction = objectMapper.readValue(message, Transaction.class);
            log.info("Consumed transaction: {}", transaction.getTransactionId());
            fraudDetectionService.detectFraud(transaction);
        } catch (Exception e) {
            log.error("Error processing message from Kafka: {}", message, e);
        }
    }
}