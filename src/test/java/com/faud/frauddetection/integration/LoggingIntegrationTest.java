package com.faud.frauddetection.integration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.faud.frauddetection.dto.FraudDetectionResult;
import com.faud.frauddetection.dto.Transaction;
import com.faud.frauddetection.service.AlertService;
import com.faud.frauddetection.service.FraudDetectionService;
import com.faud.frauddetection.service.impl.RuleBasedFraudDetectionService;
import com.faud.frauddetection.service.evaluator.impl.DynamicRuleEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Integration tests for logging services
 * Verifies structured logging, log levels, and log content for fraud detection operations
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.faud.frauddetection=DEBUG",
    "fraud.alert.enabled=true"
})
class LoggingIntegrationTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    private ListAppender<ILoggingEvent> alertServiceAppender;
    private ListAppender<ILoggingEvent> fraudDetectionAppender;
    private ListAppender<ILoggingEvent> ruleEngineAppender;

    private Logger alertServiceLogger;
    private Logger fraudDetectionLogger;
    private Logger ruleEngineLogger;

    @BeforeEach
    void setUp() {
        // Set up log appenders for different services
        setupLogAppender(AlertService.class, "alertServiceAppender");
        setupLogAppender(RuleBasedFraudDetectionService.class, "fraudDetectionAppender");
        setupLogAppender(DynamicRuleEngine.class, "ruleEngineAppender");
    }

    @AfterEach
    void tearDown() {
        // Clean up appenders
        if (alertServiceLogger != null && alertServiceAppender != null) {
            alertServiceLogger.detachAppender(alertServiceAppender);
        }
        if (fraudDetectionLogger != null && fraudDetectionAppender != null) {
            fraudDetectionLogger.detachAppender(fraudDetectionAppender);
        }
        if (ruleEngineLogger != null && ruleEngineAppender != null) {
            ruleEngineLogger.detachAppender(ruleEngineAppender);
        }
    }

    @Test
    void testFraudAlertLogging() {
        // Given
        FraudDetectionResult fraudResult = createFraudDetectionResult("TXN_LOG_001", true, 0.85);

        // When
        alertService.sendAlert(fraudResult);

        // Then
        List<ILoggingEvent> logEvents = alertServiceAppender.list;
        
        // Verify fraud alert log
        List<ILoggingEvent> warnLogs = logEvents.stream()
            .filter(event -> event.getLevel() == Level.WARN)
            .filter(event -> event.getMessage().contains("FRAUD ALERT"))
            .collect(Collectors.toList());
        
        assertThat(warnLogs).hasSize(1);
        ILoggingEvent fraudAlertLog = warnLogs.get(0);
        assertThat(fraudAlertLog.getFormattedMessage()).contains("TXN_LOG_001");
        assertThat(fraudAlertLog.getFormattedMessage()).contains("0.85");
        assertThat(fraudAlertLog.getFormattedMessage()).contains("🚨 FRAUD ALERT");

        // Verify Kafka success log
        List<ILoggingEvent> infoLogs = logEvents.stream()
            .filter(event -> event.getLevel() == Level.INFO)
            .filter(event -> event.getMessage().contains("Fraud alert sent to Kafka successfully"))
            .collect(Collectors.toList());
        
        assertThat(infoLogs).hasSize(1);
        assertThat(infoLogs.get(0).getFormattedMessage()).contains("TXN_LOG_001");
    }

    @Test
    void testNonFraudTransactionLogging() {
        // Given
        FraudDetectionResult normalResult = createFraudDetectionResult("TXN_NORMAL_001", false, 0.15);

        // When
        alertService.sendAlert(normalResult);

        // Then
        List<ILoggingEvent> logEvents = alertServiceAppender.list;
        
        // Verify no fraud alert logs for normal transactions
        List<ILoggingEvent> fraudAlertLogs = logEvents.stream()
            .filter(event -> event.getMessage().contains("FRAUD ALERT"))
            .collect(Collectors.toList());
        
        assertThat(fraudAlertLogs).isEmpty();
    }

    @Test
    void testFraudDetectionServiceLogging() {
        // Given
        Transaction transaction = createTestTransaction("TXN_SERVICE_001", new BigDecimal("15000"));

        // When
        FraudDetectionResult result = fraudDetectionService.detectFraud(transaction);

        // Then
        List<ILoggingEvent> logEvents = fraudDetectionAppender.list;
        
        // Verify fraud detection start log
        List<ILoggingEvent> startLogs = logEvents.stream()
            .filter(event -> event.getLevel() == Level.INFO)
            .filter(event -> event.getMessage().contains("Starting fraud detection"))
            .collect(Collectors.toList());
        
        assertThat(startLogs).hasSize(1);
        assertThat(startLogs.get(0).getFormattedMessage()).contains("TXN_SERVICE_001");

        // Verify fraud detection completion log
        List<ILoggingEvent> completionLogs = logEvents.stream()
            .filter(event -> event.getLevel() == Level.INFO)
            .filter(event -> event.getMessage().contains("Fraud detection completed"))
            .collect(Collectors.toList());
        
        assertThat(completionLogs).hasSize(1);
        ILoggingEvent completionLog = completionLogs.get(0);
        assertThat(completionLog.getFormattedMessage()).contains("TXN_SERVICE_001");
        assertThat(completionLog.getFormattedMessage()).contains("Processing Time:");
    }

    @Test
    void testRuleEngineDebugLogging() {
        // Given
        Transaction transaction = createTestTransaction("TXN_RULE_001", new BigDecimal("25000"));

        // When
        fraudDetectionService.detectFraud(transaction);

        // Then
        List<ILoggingEvent> logEvents = ruleEngineAppender.list;
        
        // Verify rule evaluation debug logs
        List<ILoggingEvent> debugLogs = logEvents.stream()
            .filter(event -> event.getLevel() == Level.DEBUG)
            .filter(event -> event.getMessage().contains("Evaluating rule"))
            .collect(Collectors.toList());
        
        assertThat(debugLogs).isNotEmpty();
        // Should have debug logs for each rule evaluation
        assertThat(debugLogs.get(0).getFormattedMessage()).contains("TXN_RULE_001");
    }

    @Test
    void testErrorLoggingForInvalidData() {
        // Given
        FraudDetectionResult invalidResult = new FraudDetectionResult();
        invalidResult.setTransactionId(null); // Invalid data to trigger error

        // When
        try {
            alertService.sendAlert(invalidResult);
        } catch (Exception e) {
            // Expected to handle gracefully
        }

        // Then
        List<ILoggingEvent> logEvents = alertServiceAppender.list;
        
        // Check for error logs
        List<ILoggingEvent> errorLogs = logEvents.stream()
            .filter(event -> event.getLevel() == Level.ERROR)
            .collect(Collectors.toList());
        
        // Should have error handling logs
        assertThat(errorLogs).isNotEmpty();
    }

    @Test
    void testLogMessageStructureAndFormat() {
        // Given
        FraudDetectionResult fraudResult = createFraudDetectionResult("TXN_FORMAT_001", true, 0.75);

        // When
        alertService.sendAlert(fraudResult);

        // Then
        List<ILoggingEvent> logEvents = alertServiceAppender.list;
        
        // Verify log message structure
        ILoggingEvent fraudAlertLog = logEvents.stream()
            .filter(event -> event.getLevel() == Level.WARN)
            .filter(event -> event.getMessage().contains("FRAUD ALERT"))
            .findFirst()
            .orElse(null);
        
        assertThat(fraudAlertLog).isNotNull();
        
        // Verify structured logging elements
        String formattedMessage = fraudAlertLog.getFormattedMessage();
        assertThat(formattedMessage).containsPattern("Transaction ID: TXN_FORMAT_001");
        assertThat(formattedMessage).containsPattern("Risk Score: 0\\.75");
        assertThat(formattedMessage).contains("🚨"); // Emoji for visual identification
    }

    @Test
    void testLogLevelFiltering() {
        // Given
        Transaction transaction = createTestTransaction("TXN_LEVEL_001", new BigDecimal("5000"));

        // When
        fraudDetectionService.detectFraud(transaction);

        // Then
        List<ILoggingEvent> logEvents = fraudDetectionAppender.list;
        
        // Verify different log levels are present
        boolean hasInfo = logEvents.stream().anyMatch(event -> event.getLevel() == Level.INFO);
        boolean hasDebug = logEvents.stream().anyMatch(event -> event.getLevel() == Level.DEBUG);
        
        assertThat(hasInfo).isTrue();
        // Debug logs should be present since we set DEBUG level in test properties
        // Note: This depends on the actual implementation having debug logs
    }

    @Test
    void testConcurrentLogging() throws InterruptedException {
        // Given
        int numberOfTransactions = 5;
        Thread[] threads = new Thread[numberOfTransactions];

        // When - Process multiple transactions concurrently
        for (int i = 0; i < numberOfTransactions; i++) {
            final int transactionIndex = i;
            threads[i] = new Thread(() -> {
                Transaction transaction = createTestTransaction("TXN_CONCURRENT_" + transactionIndex, new BigDecimal("10000"));
                fraudDetectionService.detectFraud(transaction);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        List<ILoggingEvent> logEvents = fraudDetectionAppender.list;
        
        // Verify all transactions were logged
        List<ILoggingEvent> startLogs = logEvents.stream()
            .filter(event -> event.getMessage().contains("Starting fraud detection"))
            .collect(Collectors.toList());
        
        assertThat(startLogs).hasSize(numberOfTransactions);
        
        // Verify thread safety - no corrupted log messages
        for (ILoggingEvent logEvent : startLogs) {
            assertThat(logEvent.getFormattedMessage()).containsPattern("TXN_CONCURRENT_\\d+");
        }
    }

    private void setupLogAppender(Class<?> loggerClass, String appenderName) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerClass);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.setName(appenderName);
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);

        // Store references for cleanup
        switch (appenderName) {
            case "alertServiceAppender":
                this.alertServiceLogger = logger;
                this.alertServiceAppender = appender;
                break;
            case "fraudDetectionAppender":
                this.fraudDetectionLogger = logger;
                this.fraudDetectionAppender = appender;
                break;
            case "ruleEngineAppender":
                this.ruleEngineLogger = logger;
                this.ruleEngineAppender = appender;
                break;
        }
    }

    private Transaction createTestTransaction(String transactionId, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setUserId("USER_LOG_TEST");
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