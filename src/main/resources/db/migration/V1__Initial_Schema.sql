-- V1__Initial_Schema.sql
-- This file contains the complete initial database schema for the fraud detection service.

-- Create fraud_rules table
CREATE TABLE fraud_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    rule_type VARCHAR(100) NOT NULL,
    threshold_value DECIMAL(19, 2),
    condition_field VARCHAR(255),
    condition_operator VARCHAR(50),
    condition_value VARCHAR(255),
    rule_config TEXT,
    risk_weight DECIMAL(5, 2) DEFAULT 0.20,
    priority INT DEFAULT 1,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled (enabled),
    INDEX idx_fraud_rules_type_enabled (rule_type, enabled),
    INDEX idx_fraud_rules_priority (priority DESC)
);

-- Insert default fraud rules
INSERT INTO fraud_rules (rule_name, description, rule_type, threshold_value, enabled, risk_weight, priority) VALUES
('LARGE_AMOUNT_RULE', 'Detects transactions exceeding a specific amount.', 'AMOUNT', 10000.00, TRUE, 0.30, 1),
('HIGH_FREQUENCY_RULE', 'Detects if a user makes too many transactions in a short period.', 'FREQUENCY', 5.00, TRUE, 0.25, 2),
('UNUSUAL_TIME_RULE', 'Detects transactions occurring at unusual times (e.g., late at night).', 'TIME_OF_DAY', 0.00, TRUE, 0.15, 3),
('IP_BLACKLIST_RULE', 'Checks if the transaction IP is on a known blacklist.', 'IP_BLACKLIST', 0.00, TRUE, 0.40, 4),
('CUMULATIVE_AMOUNT_RULE', 'Detects users cumulative transaction amount exceeding threshold within 24 hours', 'CUSTOM', 50000.00, TRUE, 0.35, 5),
('MERCHANT_BLACKLIST_RULE', 'Detects if merchant is in blacklist', 'CUSTOM', 0.00, TRUE, 0.50, 6),
('VELOCITY_RULE', 'Detects abnormal transaction frequency (more than 3 in 10 minutes)', 'FREQUENCY', 3.00, TRUE, 0.30, 7);

-- Create fraud_detection_results table
CREATE TABLE fraud_detection_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    is_fraud BOOLEAN NOT NULL,
    risk_score DECIMAL(5, 2),
    risk_level VARCHAR(50),
    reason TEXT,
    triggered_rules TEXT,
    ip_address VARCHAR(45),
    transaction_amount DECIMAL(19, 2),
    currency VARCHAR(10),
    transaction_time TIMESTAMP,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    review_status VARCHAR(50) DEFAULT 'PENDING',
    reviewed_by VARCHAR(255),
    review_notes TEXT,
    reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_results_user_id (user_id),
    INDEX idx_results_detected_at (detected_at),
    INDEX idx_results_review_status (review_status)
);

-- Create rule configurations table for complex rules
CREATE TABLE rule_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES fraud_rules(id) ON DELETE CASCADE,
    UNIQUE KEY unique_rule_config (rule_id, config_key)
);

-- Insert configuration examples
INSERT INTO rule_configurations (rule_id, config_key, config_value, description) VALUES
((SELECT id FROM fraud_rules WHERE rule_name = 'CUMULATIVE_AMOUNT_RULE'), 'time_window_hours', '24', 'Time window in hours'),
((SELECT id FROM fraud_rules WHERE rule_name = 'MERCHANT_BLACKLIST_RULE'), 'blacklist_merchants', '["MERCHANT_001", "MERCHANT_999"]', 'Blacklist merchant list'),
((SELECT id FROM fraud_rules WHERE rule_name = 'VELOCITY_RULE'), 'time_window_minutes', '10', 'Detection time window in minutes'); 