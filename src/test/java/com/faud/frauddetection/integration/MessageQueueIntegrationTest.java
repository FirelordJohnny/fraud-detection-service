package com.faud.frauddetection.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.service.AlertService;
import com.faud.frauddetection.service.FraudDetectionService;
import com.faud.frauddetection.service.TransactionConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for message queue interactions
 * Tests Kafka producer/consumer functionality and message processing
 */
@SpringBootTest
@EmbeddedKafka(
    partitions = 1,
    topics = {"transactions", "fraud-alerts"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "fraud.alert.enabled=true",
    "fraud.alert.topic=fraud-alerts"
})
@ActiveProfiles("test")
class MessageQueueIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private AlertService alertService;

    @Autowired
    private TransactionConsumer transactionConsumer;

    @MockBean
    private FraudDetectionService fraudDetectionService;

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Set up Kafka producer
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));

        // Set up Kafka consumer for fraud-alerts topic
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("fraud-alerts");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        
        // Wait for container to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void testTransactionConsumerProcessesKafkaMessages() throws Exception {
        // Given
        Transaction transaction = createTestTransaction();
        FraudDetectionResult fraudResult = createFraudDetectionResult(transaction.getTransactionId(), true, 0.85);
        
        when(fraudDetectionService.detectFraud(any(Transaction.class))).thenReturn(fraudResult);

        // When
        String transactionJson = objectMapper.writeValueAsString(transaction);
        kafkaTemplate.send("transactions", transaction.getTransactionId(), transactionJson);

        // Then
        // Wait a bit for the message to be processed
        Thread.sleep(2000);
        
        verify(fraudDetectionService, timeout(5000)).detectFraud(any(Transaction.class));
    }

    @Test
    void testAlertServiceSendsKafkaAlert() throws Exception {
        // Given
        FraudDetectionResult fraudResult = createFraudDetectionResult("TXN_FRAUD_001", true, 0.95);

        // When
        alertService.sendAlert(fraudResult);

        // Then
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.topic()).isEqualTo("fraud-alerts");
        assertThat(received.key()).isEqualTo("TXN_FRAUD_001");

        // Verify alert message content
        String alertJson = received.value();
        Map<String, Object> alertData = objectMapper.readValue(alertJson, Map.class);
        
        assertThat(alertData).containsKey("alertId");
        assertThat(alertData).containsKey("timestamp");
        assertThat(alertData.get("alertType")).isEqualTo("FRAUD_DETECTION");
        assertThat(alertData.get("severity")).isEqualTo("CRITICAL");
        assertThat(alertData.get("transactionId")).isEqualTo("TXN_FRAUD_001");
        assertThat(alertData.get("riskScore")).isEqualTo(0.95);
    }

    @Test
    void testAlertServiceDoesNotSendAlertForNonFraudulentTransaction() throws Exception {
        // Given
        FraudDetectionResult normalResult = createFraudDetectionResult("TXN_NORMAL_001", false, 0.15);

        // When
        alertService.sendAlert(normalResult);

        // Then
        ConsumerRecord<String, String> received = records.poll(3, TimeUnit.SECONDS);
        assertThat(received).isNull(); // No alert should be sent
    }

    @Test
    void testKafkaMessageProcessingWithInvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When
        kafkaTemplate.send("transactions", "INVALID_TXN", invalidJson);

        // Then
        // The consumer should handle the error gracefully
        Thread.sleep(2000);
        verify(fraudDetectionService, never()).detectFraud(any(Transaction.class));
    }

    @Test
    void testMultipleTransactionProcessing() throws Exception {
        // Given
        Transaction[] transactions = {
            createTestTransaction("TXN_001", new BigDecimal("5000")),
            createTestTransaction("TXN_002", new BigDecimal("15000")),
            createTestTransaction("TXN_003", new BigDecimal("25000"))
        };

        FraudDetectionResult[] results = {
            createFraudDetectionResult("TXN_001", false, 0.2),
            createFraudDetectionResult("TXN_002", true, 0.7),
            createFraudDetectionResult("TXN_003", true, 0.9)
        };

        when(fraudDetectionService.detectFraud(any(Transaction.class)))
            .thenReturn(results[0], results[1], results[2]);

        // When
        for (Transaction transaction : transactions) {
            String transactionJson = objectMapper.writeValueAsString(transaction);
            kafkaTemplate.send("transactions", transaction.getTransactionId(), transactionJson);
        }

        // Then
        Thread.sleep(3000);
        verify(fraudDetectionService, times(3)).detectFraud(any(Transaction.class));

        // Verify fraud alerts were sent for fraudulent transactions
        int alertCount = 0;
        ConsumerRecord<String, String> record;
        while ((record = records.poll(1, TimeUnit.SECONDS)) != null) {
            alertCount++;
            String alertJson = record.value();
            Map<String, Object> alertData = objectMapper.readValue(alertJson, Map.class);
            assertThat(alertData.get("alertType")).isEqualTo("FRAUD_DETECTION");
        }
        
        assertThat(alertCount).isEqualTo(2); // Only 2 fraudulent transactions should generate alerts
    }

    @Test
    void testKafkaProducerFailureHandling() throws Exception {
        // Given
        FraudDetectionResult fraudResult = createFraudDetectionResult("TXN_FAIL_001", true, 0.8);

        // When - This should complete successfully even if Kafka has issues
        alertService.sendAlert(fraudResult);

        // Then - The method should not throw an exception
        // In a real scenario, you might test with a broken Kafka broker
        // For now, we just verify the method completes
        Thread.sleep(1000);
        
        // Verify that the alert was processed
        ConsumerRecord<String, String> received = records.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
    }

    private Transaction createTestTransaction() {
        return createTestTransaction("TXN_TEST_001", new BigDecimal("10000"));
    }

    private Transaction createTestTransaction(String transactionId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setUserId("USER_TEST_001");
        transaction.setAmount(amount);
        transaction.setCurrency("USD");
        transaction.setIpAddress("192.168.1.100");
        transaction.setTimestamp(LocalDateTime.now());
        return transaction;
    }

    private FraudDetectionResult createFraudDetectionResult(String transactionId, boolean isFraud, double riskScore) {
        FraudDetectionResult result = new FraudDetectionResult();
        result.setTransactionId(transactionId);
        result.setFraud(isFraud);
        result.setRiskScore(riskScore);
        result.setReason(isFraud ? "High risk transaction detected" : "Normal transaction");
        result.setDetectionTimestamp(LocalDateTime.now());
        result.setProcessingTime(100L);
        return result;
    }
} 