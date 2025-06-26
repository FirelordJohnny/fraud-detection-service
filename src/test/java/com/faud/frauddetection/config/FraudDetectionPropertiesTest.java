package com.faud.frauddetection.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "fraud.detection.enabled=true",
    "fraud.detection.fraud-threshold=0.7",
    "fraud.detection.async-processing=true",
    "fraud.detection.batch-size=150",
    "fraud.detection.thread-pool-size=25",
    "fraud.detection.time-window.default-seconds=5400",
    "fraud.detection.time-window.max-seconds=86400",
    "fraud.detection.rule-evaluation.default-risk-weight=0.4",
    "fraud.detection.rule-evaluation.max-risk-score=0.8",
    "fraud.detection.alert.enabled=true",
    "fraud.detection.alert.kafka-topic=fraud-alerts-topic",
    "fraud.detection.alert.severity-thresholds.critical=0.85",
    "fraud.detection.alert.severity-thresholds.high=0.65",
    "fraud.detection.alert.severity-thresholds.medium=0.35"
})
class FraudDetectionPropertiesTest {

    @Autowired
    private FraudDetectionProperties properties;

    @Test
    void properties_shouldBeLoadedAndMappedCorrectly() {
        // Test main properties
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getFraudThreshold()).isEqualTo(new BigDecimal("0.7"));
        assertThat(properties.isAsyncProcessing()).isTrue();
        assertThat(properties.getBatchSize()).isEqualTo(150);
        assertThat(properties.getThreadPoolSize()).isEqualTo(25);

        // Test time window properties
        FraudDetectionProperties.TimeWindow timeWindow = properties.getTimeWindow();
        assertThat(timeWindow).isNotNull();
        assertThat(timeWindow.getDefaultSeconds()).isEqualTo(5400L);
        assertThat(timeWindow.getMaxSeconds()).isEqualTo(86400L);

        // Test rule evaluation properties
        FraudDetectionProperties.RuleEvaluation ruleEvaluation = properties.getRuleEvaluation();
        assertThat(ruleEvaluation).isNotNull();
        assertThat(ruleEvaluation.getDefaultRiskWeight()).isEqualTo(new BigDecimal("0.4"));
        assertThat(ruleEvaluation.getMaxRiskScore()).isEqualTo(new BigDecimal("0.8"));

        // Test alert properties
        FraudDetectionProperties.Alert alert = properties.getAlert();
        assertThat(alert).isNotNull();
        assertThat(alert.isEnabled()).isTrue();
        assertThat(alert.getKafkaTopic()).isEqualTo("fraud-alerts-topic");

        // Test severity thresholds
        FraudDetectionProperties.Alert.SeverityThresholds thresholds = alert.getSeverityThresholds();
        assertThat(thresholds).isNotNull();
        assertThat(thresholds.getCritical()).isEqualTo(0.85);
        assertThat(thresholds.getHigh()).isEqualTo(0.65);
        assertThat(thresholds.getMedium()).isEqualTo(0.35);
    }

    @Test
    void testSetters_ShouldUpdateValues() {
        // Given
        FraudDetectionProperties testProperties = new FraudDetectionProperties();
        
        // When
        testProperties.setEnabled(false);
        testProperties.setFraudThreshold(new BigDecimal("0.5"));
        testProperties.setAsyncProcessing(false);
        testProperties.setBatchSize(200);
        testProperties.setThreadPoolSize(30);

        // Then
        assertThat(testProperties.isEnabled()).isFalse();
        assertThat(testProperties.getFraudThreshold()).isEqualTo(new BigDecimal("0.5"));
        assertThat(testProperties.isAsyncProcessing()).isFalse();
        assertThat(testProperties.getBatchSize()).isEqualTo(200);
        assertThat(testProperties.getThreadPoolSize()).isEqualTo(30);
    }

    @Test
    void testTimeWindowSetters() {
        // Given
        FraudDetectionProperties.TimeWindow timeWindow = new FraudDetectionProperties.TimeWindow();
        
        // When
        timeWindow.setDefaultSeconds(3600L);
        timeWindow.setMaxSeconds(7200L);

        // Then
        assertThat(timeWindow.getDefaultSeconds()).isEqualTo(3600L);
        assertThat(timeWindow.getMaxSeconds()).isEqualTo(7200L);
    }

    @Test
    void testRuleEvaluationSetters() {
        // Given
        FraudDetectionProperties.RuleEvaluation ruleEvaluation = new FraudDetectionProperties.RuleEvaluation();
        
        // When
        ruleEvaluation.setDefaultRiskWeight(new BigDecimal("0.6"));
        ruleEvaluation.setMaxRiskScore(new BigDecimal("0.9"));

        // Then
        assertThat(ruleEvaluation.getDefaultRiskWeight()).isEqualTo(new BigDecimal("0.6"));
        assertThat(ruleEvaluation.getMaxRiskScore()).isEqualTo(new BigDecimal("0.9"));
    }

    @Test
    void testAlertSetters() {
        // Given
        FraudDetectionProperties.Alert alert = new FraudDetectionProperties.Alert();
        
        // When
        alert.setEnabled(false);
        alert.setKafkaTopic("test-topic");

        // Then
        assertThat(alert.isEnabled()).isFalse();
        assertThat(alert.getKafkaTopic()).isEqualTo("test-topic");
    }

    @Test
    void testSeverityThresholdsSetters() {
        // Given
        FraudDetectionProperties.Alert.SeverityThresholds thresholds = 
                new FraudDetectionProperties.Alert.SeverityThresholds();
        
        // When
        thresholds.setCritical(0.9);
        thresholds.setHigh(0.7);
        thresholds.setMedium(0.4);

        // Then
        assertThat(thresholds.getCritical()).isEqualTo(0.9);
        assertThat(thresholds.getHigh()).isEqualTo(0.7);
        assertThat(thresholds.getMedium()).isEqualTo(0.4);
    }

    @Test
    void testNestedObjectInitialization() {
        // Given
        FraudDetectionProperties testProperties = new FraudDetectionProperties();
        
        // When
        FraudDetectionProperties.TimeWindow timeWindow = new FraudDetectionProperties.TimeWindow();
        FraudDetectionProperties.RuleEvaluation ruleEvaluation = new FraudDetectionProperties.RuleEvaluation();
        FraudDetectionProperties.Alert alert = new FraudDetectionProperties.Alert();
        
        testProperties.setTimeWindow(timeWindow);
        testProperties.setRuleEvaluation(ruleEvaluation);
        testProperties.setAlert(alert);

        // Then
        assertThat(testProperties.getTimeWindow()).isSameAs(timeWindow);
        assertThat(testProperties.getRuleEvaluation()).isSameAs(ruleEvaluation);
        assertThat(testProperties.getAlert()).isSameAs(alert);
    }

    @Test
    void testToString_ShouldNotBeEmpty() {
        // When
        String mainToString = properties.toString();
        String timeWindowToString = properties.getTimeWindow().toString();
        String ruleEvaluationToString = properties.getRuleEvaluation().toString();
        String alertToString = properties.getAlert().toString();
        String thresholdsToString = properties.getAlert().getSeverityThresholds().toString();

        // Then
        assertThat(mainToString).isNotEmpty();
        assertThat(timeWindowToString).isNotEmpty();
        assertThat(ruleEvaluationToString).isNotEmpty();
        assertThat(alertToString).isNotEmpty();
        assertThat(thresholdsToString).isNotEmpty();
    }

    @Test
    void testEquals_ShouldWorkCorrectly() {
        // Given
        FraudDetectionProperties props1 = new FraudDetectionProperties();
        FraudDetectionProperties props2 = new FraudDetectionProperties();
        
        props1.setEnabled(true);
        props1.setFraudThreshold(new BigDecimal("0.5"));
        
        props2.setEnabled(true);
        props2.setFraudThreshold(new BigDecimal("0.5"));

        // When & Then
        assertThat(props1.equals(props2)).isTrue();
        assertThat(props1.hashCode()).isEqualTo(props2.hashCode());
    }

    @Test
    void testEquals_WithDifferentValues_ShouldReturnFalse() {
        // Given
        FraudDetectionProperties props1 = new FraudDetectionProperties();
        FraudDetectionProperties props2 = new FraudDetectionProperties();
        
        props1.setEnabled(true);
        props2.setEnabled(false);

        // When & Then
        assertThat(props1.equals(props2)).isFalse();
    }
}


@SpringBootTest
@ActiveProfiles("test")
class FraudDetectionPropertiesDefaultTest {

    @Autowired
    private FraudDetectionProperties properties;

    @Test
    void properties_shouldHaveDefaultValues_whenNotOverridden() {
        assertNotNull(properties);

        // Assert a few default values
        assertTrue(properties.isEnabled());
        assertEquals(0, BigDecimal.valueOf(0.3).compareTo(properties.getFraudThreshold()));
        assertEquals(3600L, properties.getTimeWindow().getDefaultSeconds());
        assertEquals("fraud-alerts", properties.getAlert().getKafkaTopic());
        assertEquals(0.8, properties.getAlert().getSeverityThresholds().getCritical());
    }
} 