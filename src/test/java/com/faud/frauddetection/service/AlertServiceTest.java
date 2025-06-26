package com.faud.frauddetection.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faud.frauddetection.config.FraudDetectionProperties;
import com.faud.frauddetection.dto.FraudDetectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FraudDetectionProperties properties;

    @Mock
    private FraudDetectionProperties.Alert alertProperties;

    @InjectMocks
    private AlertService alertService;

    private FraudDetectionResult fraudulentResult;
    private FraudDetectionResult nonFraudulentResult;

    @BeforeEach
    void setUp() {
        fraudulentResult = FraudDetectionResult.builder()
                .transactionId("txn-123-fraud")
                .isFraudulent(true)
                .riskScore(95.0)
                .reason("High risk score")
                .build();

        nonFraudulentResult = FraudDetectionResult.builder()
                .transactionId("txn-456-ok")
                .isFraudulent(false)
                .riskScore(10.0)
                .reason("Low risk")
                .build();
        
        // Mock the nested properties
        when(properties.getAlert()).thenReturn(alertProperties);
    }

    @Test
    void sendAlert_shouldDoNothing_whenAlertsAreDisabled() {
        when(alertProperties.isEnabled()).thenReturn(false);

        alertService.sendAlert(fraudulentResult);

        verifyNoInteractions(kafkaTemplate, objectMapper);
    }

    @Test
    void sendAlert_shouldDoNothing_forNonFraudulentResult() {
        when(alertProperties.isEnabled()).thenReturn(true);

        alertService.sendAlert(nonFraudulentResult);

        verifyNoInteractions(kafkaTemplate, objectMapper);
    }

    @Test
    void sendAlert_shouldSendKafkaAlert_forFraudulentResult() throws Exception {
        when(alertProperties.isEnabled()).thenReturn(true);
        when(alertProperties.getKafkaTopic()).thenReturn("fraud-alerts-topic");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"alert\":\"test\"}");

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(null); // Simulate successful send
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        alertService.sendAlert(fraudulentResult);

        verify(kafkaTemplate, times(1)).send("fraud-alerts-topic", "txn-123-fraud", "{\"alert\":\"test\"}");
        verify(objectMapper, times(1)).writeValueAsString(any());
    }
    
    @Test
    void sendAlert_shouldHandleKafkaSendFailure() throws Exception {
        when(alertProperties.isEnabled()).thenReturn(true);
        when(alertProperties.getKafkaTopic()).thenReturn("fraud-alerts-topic");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"alert\":\"test\"}");

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka is down"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        alertService.sendAlert(fraudulentResult);

        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
        // In a real scenario, we would capture logs to verify the error was logged.
        // For this test, we just ensure the exception doesn't propagate.
    }

    @Test
    void sendAlert_shouldHandleJsonSerializationFailure() throws Exception {
        when(alertProperties.isEnabled()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {});

        alertService.sendAlert(fraudulentResult);

        verify(objectMapper, times(1)).writeValueAsString(any());
        verifyNoInteractions(kafkaTemplate);
    }
    
    @Test
    void sendAlert_shouldHandleKafkaSendExceptionGracefully() throws Exception {
        // This test ensures the outer try-catch block in sendAlert works
        when(alertProperties.isEnabled()).thenReturn(true);
        when(alertProperties.getKafkaTopic()).thenReturn("fraud-alerts-topic");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"alert\":\"test\"}");

        // Simulate kafkaTemplate throwing an exception on send
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Kafka connection failed"));

        // The exception should be caught by the outer try-catch in sendAlert, and not propagated
        assertDoesNotThrow(() -> alertService.sendAlert(fraudulentResult));

        // Verify that the send was attempted
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
    }
} 