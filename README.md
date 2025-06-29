# Real-Time Fraud Detection Service

A high-performance, real-time fraud detection system built with Java Spring Boot, featuring a **dynamic rule engine** that allows rule management without code changes.

## 🚀 Key Features

### Dynamic Rule Engine
- **Zero-Code Rule Management**: Add, modify, or delete fraud detection rules without code changes
- **Real-Time Configuration**: Rules take effect immediately without service restart
- **Multiple Rule Types**: Support for amount, frequency, time-based, IP, and custom rules
- **Configurable Risk Scoring**: Flexible risk weight and priority management

### High Performance
- **Real-Time Processing**: Sub-millisecond fraud detection
- **Scalable Architecture**: Horizontal scaling with Kubernetes
- **Redis Caching**: High-speed state management and rule caching
- **Async Processing**: Non-blocking fraud detection pipeline

### Production Ready
- **Comprehensive Testing**: 87% instruction coverage, 67% branch coverage
- **Monitoring & Metrics**: Prometheus metrics and health checks
- **Security**: JWT authentication and API token validation
- **Cloud Native**: Kubernetes deployment with auto-scaling

## 🏗️ System Design

### Use Case Diagram
Shows the main actors and their interactions with the fraud detection system:
```mermaid
graph LR
    subgraph "External Actors"
        Admin[System Administrator]
        Client[API Client]
        Internal[Internal Service]
    end
    
    subgraph "Use Cases"
        UC1[Manage Fraud Rules]
        UC2[Analyze Transaction]
        UC3[View Detection Results]
        UC4[Toggle Rule Status]
        UC5[Monitor System Health]
        UC6[Generate Reports]
        UC7[Configure Alerts]
    end
    
    Admin --> UC1
    Admin --> UC4
    Admin --> UC5
    Admin --> UC6
    Admin --> UC7
    
    Client --> UC1
    Client --> UC2
    Client --> UC3
    
    Internal --> UC3
    Internal --> UC5
    
    UC1 --> |includes| UC4
    UC2 --> |includes| UC7
```

### System Architecture
High-level overview of the fraud detection system components and their relationships:
```mermaid
graph TB
    A[Transaction Input] --> B[Fraud Detection Service]
    B --> C[Dynamic Rule Engine]
    C --> D[Rule Repository]
    C --> E[Redis Cache]
    B --> F[Risk Scoring]
    F --> G[Alert Service]
    G --> H[Notification System]
    
    subgraph "Rule Types"
        I[Amount Rules]
        J[Frequency Rules]
        K[Time Rules]
        L[IP Rules]
        M[Custom Rules]
    end
    
    C --> I
    C --> J
    C --> K
    C --> L
    C --> M
```

### System Flow Diagram
Detailed workflow of fraud detection process from transaction input to result output:
```mermaid
flowchart TD
    A[Transaction Input] --> B{Transaction Validation}
    B -->|Valid| C[Load Active Rules]
    B -->|Invalid| Z[Return Error]
    
    C --> D[Rule Engine Evaluation]
    D --> E[Amount Evaluator]
    D --> F[Frequency Evaluator]
    D --> G[Time Evaluator]
    D --> H[IP Evaluator]
    D --> I[Custom Evaluator]
    
    E --> J[Risk Score Calculation]
    F --> J
    G --> J
    H --> J
    I --> J
    
    J --> K{Risk Score >= Threshold?}
    K -->|Yes| L[Mark as Fraud]
    K -->|No| M[Mark as Normal]
    
    L --> N[Generate Alert]
    M --> O[Save Result]
    N --> P[Send Notification]
    
    P --> Q[Kafka Message]
    P --> R[Webhook Call]
    P --> S[Email Alert]
    
    O --> T[Database Storage]
    Q --> T
    R --> T
    S --> T
    
    T --> U[Return Detection Result]
    
    subgraph "Caching Layer"
        V[Redis Cache<br/>- Rules Cache<br/>- User State<br/>- Frequency Data]
    end
    
    C -.-> V
    F -.-> V
    J -.-> V
```

### Entity Relationship Diagram
Database schema showing the relationships between core entities in the fraud detection system:
```mermaid
erDiagram
    FRAUD_RULES {
        bigint id PK
        varchar rule_name
        varchar rule_type
        text description
        text rule_config
        boolean enabled
        decimal threshold_value
        varchar condition_field
        varchar condition_operator
        varchar condition_value
        decimal risk_weight
        int priority
        timestamp created_at
        timestamp updated_at
    }
    
    FRAUD_DETECTION_RESULTS {
        bigint id PK
        varchar transaction_id
        varchar user_id
        decimal amount
        varchar currency
        varchar ip_address
        timestamp transaction_timestamp
        decimal risk_score
        boolean is_fraud
        text detection_details
        text triggered_rules
        varchar alert_severity
        timestamp created_at
    }
    
    RULE_EVALUATION_TYPES {
        bigint id PK
        varchar evaluation_type
        varchar description
        boolean active
        timestamp created_at
    }
    
    TRANSACTIONS {
        varchar transaction_id PK
        varchar user_id
        decimal amount
        varchar currency
        varchar merchant_id
        varchar ip_address
        varchar device_id
        varchar payment_method
        timestamp timestamp
        varchar status
    }
    
    USERS {
        varchar user_id PK
        varchar email
        varchar phone
        varchar country
        timestamp registration_date
        varchar risk_profile
        boolean is_active
    }
    
    ALERT_HISTORY {
        bigint id PK
        bigint detection_result_id FK
        varchar alert_type
        varchar severity
        text message
        varchar status
        timestamp sent_at
        timestamp acknowledged_at
    }
    
    FRAUD_DETECTION_RESULTS ||--o{ ALERT_HISTORY : generates
    FRAUD_RULES ||--o{ FRAUD_DETECTION_RESULTS : triggers
    USERS ||--o{ FRAUD_DETECTION_RESULTS : belongs_to
    USERS ||--o{ TRANSACTIONS : performs
    RULE_EVALUATION_TYPES ||--o{ FRAUD_RULES : categorizes
```

## 🏛️ DDD (Domain-Driven Design) Architecture

### Context Map
Strategic design overview showing bounded contexts and their relationships:
```mermaid
graph TB
    subgraph "Fraud Detection System"
        FDS[Fraud Detection<br/>Context]
        RM[Rule Management<br/>Context]  
        AM[Alert Management<br/>Context]
        RC[Reporting<br/>Context]
    end
    
    subgraph "External Systems"
        PS[Payment System<br/>Context]
        UM[User Management<br/>Context]
        NS[Notification Service<br/>Context]
        RS[Risk Management<br/>Context]
        AU[Audit System<br/>Context]
    end
    
    subgraph "Infrastructure"
        DB[(Database)]
        REDIS[(Redis Cache)]
        KAFKA[Kafka Message Bus]
    end
    
    %% Upstream/Downstream relationships
    PS -->|Customer/Supplier| FDS
    UM -->|Customer/Supplier| FDS
    FDS -->|Customer/Supplier| NS
    FDS -->|Customer/Supplier| AU
    
    %% Shared Kernel
    FDS -.->|Shared Kernel| RS
    
    %% Anti-Corruption Layer
    FDS -->|ACL| PS
    
    %% Conformist
    AM -->|Conformist| FDS
    RC -->|Conformist| FDS
    RM -->|Partnership| FDS
    
    %% Infrastructure dependencies
    FDS --> DB
    FDS --> REDIS
    FDS --> KAFKA
    AM --> KAFKA
    NS --> KAFKA
```

### Domain Model
Core business domain objects and their relationships:
```mermaid
classDiagram
    class Transaction {
        +TransactionId id
        +UserId userId
        +Money amount
        +Currency currency
        +IPAddress ipAddress
        +DateTime timestamp
        +MerchantId merchantId
        +PaymentMethod paymentMethod
        +TransactionStatus status
        +evaluateRisk(rules) RiskAssessment
        +markAsFraud() void
        +markAsNormal() void
    }
    
    class FraudRule {
        +RuleId id
        +RuleName name
        +RuleType type
        +RuleConfig config
        +boolean enabled
        +Priority priority
        +RiskWeight weight
        +evaluate(transaction) RuleResult
        +toggle() void
    }
    
    class RiskAssessment {
        +AssessmentId id
        +TransactionId transactionId
        +RiskScore score
        +List~RuleResult~ ruleResults
        +AlertSeverity severity
        +calculateTotalRisk() RiskScore
        +shouldTriggerAlert() boolean
    }
    
    class RuleResult {
        +RuleId ruleId
        +boolean triggered
        +RiskScore contribution
        +string details
    }
    
    class Alert {
        +AlertId id
        +AssessmentId assessmentId
        +AlertType type
        +AlertSeverity severity
        +AlertStatus status
        +DateTime createdAt
        +send() void
        +acknowledge() void
    }
    
    class User {
        +UserId id
        +Email email
        +RiskProfile profile
        +DateTime registrationDate
        +List~Transaction~ transactions
        +calculateRiskProfile() RiskProfile
    }
    
    class RuleEvaluator {
        <<Domain Service>>
        +evaluateTransaction(transaction, rules) RiskAssessment
        +calculateRiskScore(results) RiskScore
    }
    
    class AlertService {
        <<Domain Service>>
        +createAlert(assessment) Alert
        +sendAlert(alert) void
    }
    
    %% Relationships
    Transaction "1" --> "1" RiskAssessment : creates
    RiskAssessment "1" --> "*" RuleResult : contains
    RuleResult "*" --> "1" FraudRule : evaluated by
    RiskAssessment "1" --> "0..1" Alert : triggers
    User "1" --> "*" Transaction : performs
    RuleEvaluator ..> Transaction : uses
    RuleEvaluator ..> FraudRule : uses
    AlertService ..> Alert : creates
```

### Bounded Contexts
Business domain boundaries and their internal structure:
```mermaid
graph TB
    subgraph "Fraud Detection Bounded Context"
        direction TB
        FD_AGG[Transaction Aggregate]
        FD_SRV[Risk Evaluation Service]
        FD_REPO[Transaction Repository]
        FD_DOM[Fraud Detection Domain]
        
        FD_AGG --> FD_SRV
        FD_SRV --> FD_REPO
        FD_SRV --> FD_DOM
    end
    
    subgraph "Rule Management Bounded Context"
        direction TB
        RM_AGG[Rule Aggregate]
        RM_SRV[Rule Service]
        RM_REPO[Rule Repository] 
        RM_DOM[Rule Domain]
        
        RM_AGG --> RM_SRV
        RM_SRV --> RM_REPO
        RM_SRV --> RM_DOM
    end
    
    subgraph "Alert Management Bounded Context"
        direction TB
        AM_AGG[Alert Aggregate]
        AM_SRV[Alert Service]
        AM_REPO[Alert Repository]
        AM_DOM[Alert Domain]
        
        AM_AGG --> AM_SRV
        AM_SRV --> AM_REPO
        AM_SRV --> AM_DOM
    end
    
    subgraph "Reporting Bounded Context"
        direction TB
        RPT_AGG[Report Aggregate]
        RPT_SRV[Report Service]
        RPT_REPO[Report Repository]
        RPT_DOM[Report Domain]
        
        RPT_AGG --> RPT_SRV
        RPT_SRV --> RPT_REPO
        RPT_SRV --> RPT_DOM
    end
    
    subgraph "Shared Kernel"
        SK_VO[Common Value Objects]
        SK_EVT[Domain Events]
        SK_EXC[Domain Exceptions]
    end
    
    %% Context interactions
    FD_DOM -.->|Uses| SK_VO
    FD_DOM -.->|Publishes| SK_EVT
    
    RM_DOM -.->|Uses| SK_VO
    RM_DOM -.->|Publishes| SK_EVT
    
    AM_DOM -.->|Uses| SK_VO
    AM_DOM -.->|Subscribes| SK_EVT
    
    RPT_DOM -.->|Uses| SK_VO
    RPT_DOM -.->|Subscribes| SK_EVT
    
    %% Cross-context communication
    FD_DOM -->|Domain Events| AM_DOM
    FD_DOM -->|Domain Events| RPT_DOM
    RM_DOM -->|Domain Events| FD_DOM
```

### Aggregate Design
Detailed design of aggregates, entities, and value objects:
```mermaid
graph TB
    subgraph "Transaction Aggregate"
        direction TB
        T_ROOT[Transaction<br/><<Aggregate Root>>]
        T_VO1[Money<br/><<Value Object>>]
        T_VO2[IPAddress<br/><<Value Object>>]
        T_VO3[UserId<br/><<Value Object>>]
        T_ENT[RiskAssessment<br/><<Entity>>]
        T_VO4[RiskScore<br/><<Value Object>>]
        
        T_ROOT --> T_VO1
        T_ROOT --> T_VO2
        T_ROOT --> T_VO3
        T_ROOT --> T_ENT
        T_ENT --> T_VO4
    end
    
    subgraph "FraudRule Aggregate"
        direction TB
        FR_ROOT[FraudRule<br/><<Aggregate Root>>]
        FR_VO1[RuleConfig<br/><<Value Object>>]
        FR_VO2[RiskWeight<br/><<Value Object>>]
        FR_VO3[Priority<br/><<Value Object>>]
        FR_ENT[RuleEvaluation<br/><<Entity>>]
        
        FR_ROOT --> FR_VO1
        FR_ROOT --> FR_VO2
        FR_ROOT --> FR_VO3
        FR_ROOT --> FR_ENT
    end
    
    subgraph "Alert Aggregate"
        direction TB
        A_ROOT[Alert<br/><<Aggregate Root>>]
        A_VO1[AlertSeverity<br/><<Value Object>>]
        A_VO2[AlertStatus<br/><<Value Object>>]
        A_ENT[NotificationHistory<br/><<Entity>>]
        A_VO3[NotificationChannel<br/><<Value Object>>]
        
        A_ROOT --> A_VO1
        A_ROOT --> A_VO2
        A_ROOT --> A_ENT
        A_ENT --> A_VO3
    end
    
    subgraph "User Aggregate"
        direction TB
        U_ROOT[User<br/><<Aggregate Root>>]
        U_VO1[Email<br/><<Value Object>>]
        U_VO2[RiskProfile<br/><<Value Object>>]
        U_ENT[TransactionHistory<br/><<Entity>>]
        
        U_ROOT --> U_VO1
        U_ROOT --> U_VO2
        U_ROOT --> U_ENT
    end
    
    %% Aggregate relationships (via Domain Events)
    T_ROOT -.->|RiskAssessed Event| A_ROOT
    FR_ROOT -.->|RuleUpdated Event| T_ROOT
    U_ROOT -.->|ProfileUpdated Event| T_ROOT
    
    %% Repository boundaries
    T_ROOT -.->|Persisted via| TR[Transaction Repository]
    FR_ROOT -.->|Persisted via| FRR[FraudRule Repository]
    A_ROOT -.->|Persisted via| AR[Alert Repository]
    U_ROOT -.->|Persisted via| UR[User Repository]
```

### Hexagonal Architecture
Ports and adapters pattern showing clear separation of concerns:
```mermaid
graph TB
    subgraph "Primary Adapters (Driving)"
        REST[REST API Controller]
        WEB[Web Interface]
        CLI[CLI Interface]
        SCHED[Scheduler]
    end
    
    subgraph "Application Core"
        subgraph "Application Layer"
            APP_SRV[Application Services]
            CMD_HDL[Command Handlers]
            QRY_HDL[Query Handlers]
            EVENT_HDL[Event Handlers]
        end
        
        subgraph "Domain Layer"
            DOM_SRV[Domain Services]
            DOM_AGG[Domain Aggregates]
            DOM_VO[Value Objects]
            DOM_EVT[Domain Events]
            DOM_REPO[Repository Interfaces]
        end
        
        subgraph "Ports (Interfaces)"
            IN_PORT[Inbound Ports]
            OUT_PORT[Outbound Ports]
        end
    end
    
    subgraph "Secondary Adapters (Driven)"
        DB_ADAPTER[Database Adapter]
        REDIS_ADAPTER[Redis Adapter]
        KAFKA_ADAPTER[Kafka Adapter]
        HTTP_ADAPTER[HTTP Client Adapter]
        EMAIL_ADAPTER[Email Adapter]
        WEBHOOK_ADAPTER[Webhook Adapter]
    end
    
    subgraph "External Systems"
        MYSQL[(MySQL Database)]
        REDIS_DB[(Redis Cache)]
        KAFKA_BROKER[Kafka Broker]
        EXT_API[External APIs]
        EMAIL_SRV[Email Service]
        WEBHOOK_SRV[Webhook Endpoints]
    end
    
    %% Primary flow (left to right)
    REST --> IN_PORT
    WEB --> IN_PORT
    CLI --> IN_PORT
    SCHED --> IN_PORT
    
    IN_PORT --> APP_SRV
    APP_SRV --> CMD_HDL
    APP_SRV --> QRY_HDL
    APP_SRV --> EVENT_HDL
    
    CMD_HDL --> DOM_SRV
    QRY_HDL --> DOM_SRV
    EVENT_HDL --> DOM_SRV
    
    DOM_SRV --> DOM_AGG
    DOM_AGG --> DOM_VO
    DOM_AGG --> DOM_EVT
    
    %% Secondary flow (right to left)
    DOM_SRV --> OUT_PORT
    OUT_PORT --> DB_ADAPTER
    OUT_PORT --> REDIS_ADAPTER
    OUT_PORT --> KAFKA_ADAPTER
    OUT_PORT --> HTTP_ADAPTER
    OUT_PORT --> EMAIL_ADAPTER
    OUT_PORT --> WEBHOOK_ADAPTER
    
    DB_ADAPTER --> MYSQL
    REDIS_ADAPTER --> REDIS_DB
    KAFKA_ADAPTER --> KAFKA_BROKER
    HTTP_ADAPTER --> EXT_API
    EMAIL_ADAPTER --> EMAIL_SRV
    WEBHOOK_ADAPTER --> WEBHOOK_SRV
    
    %% Repository pattern
    DOM_REPO -.-> OUT_PORT
    DOM_REPO -.-> DB_ADAPTER
```

### Event Flow
Domain events and their flow through the system:
```mermaid
sequenceDiagram
    participant Client as API Client
    participant App as Application Service
    participant Domain as Domain Service
    participant TxnAgg as Transaction Aggregate
    participant RuleAgg as Rule Aggregate
    participant AlertAgg as Alert Aggregate
    participant EventBus as Event Bus
    participant NotificationSvc as Notification Service
    
    Note over Client, NotificationSvc: Fraud Detection Event Flow
    
    Client->>App: Analyze Transaction Request
    App->>Domain: Evaluate Transaction
    
    Domain->>TxnAgg: Create Transaction
    TxnAgg->>EventBus: Publish TransactionCreated Event
    
    Domain->>RuleAgg: Load Active Rules
    RuleAgg->>Domain: Return Rules
    
    loop For Each Rule
        Domain->>RuleAgg: Evaluate Rule
        RuleAgg->>Domain: Return Rule Result
        alt Rule Triggered
            RuleAgg->>EventBus: Publish RuleTriggered Event
        end
    end
    
    Domain->>TxnAgg: Calculate Risk Score
    TxnAgg->>EventBus: Publish RiskScoreCalculated Event
    
    alt Risk Score > Threshold
        TxnAgg->>EventBus: Publish FraudDetected Event
        EventBus->>AlertAgg: Handle FraudDetected Event
        AlertAgg->>EventBus: Publish AlertCreated Event
        
        EventBus->>NotificationSvc: Handle AlertCreated Event
        NotificationSvc->>EventBus: Publish NotificationSent Event
        
        par Notification Channels
            NotificationSvc->>NotificationSvc: Send Email Alert
        and
            NotificationSvc->>NotificationSvc: Send Webhook Alert
        and
            NotificationSvc->>NotificationSvc: Send Kafka Message
        end
    else Risk Score <= Threshold
        TxnAgg->>EventBus: Publish TransactionApproved Event
    end
    
    Domain->>TxnAgg: Save Assessment Result
    TxnAgg->>EventBus: Publish AssessmentCompleted Event
    
    App->>Client: Return Detection Result
    
    Note over EventBus: Asynchronous Event Processing
    EventBus->>EventBus: Process Audit Events
    EventBus->>EventBus: Update Statistics
    EventBus->>EventBus: Generate Reports
```

## 🔧 Dynamic Rule System

### Supported Rule Types

| Rule Type | Description | Configuration | Default Risk Weight |
|-----------|-------------|---------------|-------------------|
| `AMOUNT` | Transaction amount thresholds | `thresholdValue`: Maximum allowed amount | 0.30 |
| `FREQUENCY` | Transaction frequency limits | `thresholdValue`: Max transactions per hour | 0.25 |
| `TIME_OF_DAY` | Time-based restrictions | Suspicious hours: 22:00-06:00 | 0.15 |
| `IP_BLACKLIST` | IP address filtering | Configurable blacklist | 0.40 |
| `CUSTOM` | Complex business rules | JSON configuration support | 0.20 |

### Rule Management APIs

#### Quick Rule Creation
```bash
# Create amount threshold rule
curl -X POST http://localhost:8080/api/v1/fraud-rules/quick-create \
  -d "ruleType=AMOUNT&ruleName=HIGH_VALUE_RULE&threshold=25000&description=Detect high value transactions"

# Create frequency rule
curl -X POST http://localhost:8080/api/v1/fraud-rules/quick-create \
  -d "ruleType=FREQUENCY&ruleName=BURST_DETECTION&threshold=10&description=Detect transaction bursts"
```

#### Advanced Rule Creation
```bash
curl -X POST http://localhost:8080/api/v1/fraud-rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleName": "WEEKEND_HIGH_AMOUNT",
    "ruleType": "CUSTOM",
    "description": "Weekend high amount detection",
    "thresholdValue": 20000,
    "riskWeight": 0.4,
    "priority": 2,
    "enabled": true,
    "ruleConfig": "{\"timePattern\": \"weekend\", \"multiplier\": 1.5}"
  }'
```

#### Rule Management
```bash
# List all rules
curl http://localhost:8080/api/v1/fraud-rules

# Get specific rule
curl http://localhost:8080/api/v1/fraud-rules/1

# Toggle rule status
curl -X PATCH http://localhost:8080/api/v1/fraud-rules/1/toggle

# Update rule
curl -X PUT http://localhost:8080/api/v1/fraud-rules/1 \
  -H "Content-Type: application/json" \
  -d '{...}'

# Delete rule
curl -X DELETE http://localhost:8080/api/v1/fraud-rules/1
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Kafka 2.8+ (optional)

### Local Development

1. **Clone Repository**
```bash
git clone <repository-url>
cd fraud-detection-service
```

2. **Setup Database**
```bash
# Start MySQL and create database
mysql -u root -p
CREATE DATABASE frauddb;
CREATE USER 'frauduser'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON frauddb.* TO 'frauduser'@'localhost';
```

3. **Start Dependencies**
```bash
# Redis
docker run -d -p 6379:6379 redis:alpine

# Kafka (optional)
docker run -d -p 9092:9092 --name kafka \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  confluentinc/cp-kafka:latest
```

4. **Run Application**
```bash
mvn spring-boot:run
```

5. **Test Fraud Detection**
```bash
# Test transaction analysis
curl -X POST http://localhost:8080/api/v1/fraud-detection/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN_001",
    "userId": "USER_123",
    "amount": 15000,
    "currency": "USD",
    "ipAddress": "192.168.1.100",
    "timestamp": "2024-01-15T14:30:00"
  }'
```

## 🐳 Docker Deployment

### Build Image
```bash
docker build -t fraud-detection-service .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

## ☸️ Kubernetes Deployment

### Deploy to Kubernetes
```bash
# Apply all manifests
kubectl apply -f k8s/

# Or use all-in-one
kubectl apply -f k8s/all-in-one.yaml
```

### Cloud Platform Specific

#### AWS EKS
```bash
# Create EKS cluster
eksctl create cluster --name fraud-detection --region us-west-2

# Deploy application
kubectl apply -f k8s/
```

#### Google GKE
```bash
# Create GKE cluster
gcloud container clusters create fraud-detection \
  --zone us-central1-a \
  --num-nodes 3

# Deploy application
kubectl apply -f k8s/
```

#### Alibaba ACK
```bash
# Create ACK cluster via console or CLI
# Deploy application
kubectl apply -f k8s/
```

## 📊 Configuration

### Application Configuration

Key configuration options in `application.yml`:

```yaml
fraud:
  detection:
    enabled: true
    fraud-threshold: 0.3  # Risk score threshold for fraud classification
    async-processing: true
    thread-pool-size: 10
  
  rules:
    refresh-interval: 300  # Rule cache refresh interval (seconds)
    cache-enabled: true
    cache-ttl: 600
  
  alerts:
    webhook:
      enabled: true
      url: http://localhost:8080/webhook/fraud-alert

# Security Configuration
api:
  token:
    internal: internal-api-token-2024

# Redis Configuration
redis:
  host: localhost
  port: 6379
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database name | `frauddb` |
| `REDIS_HOST` | Redis host | `localhost` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka servers | `localhost:9092` |
| `FRAUD_THRESHOLD` | Risk threshold | `0.3` |
| `JWT_SECRET` | JWT signing secret | `auto-generated` |
| `INTERNAL_API_TOKEN` | Internal API token | `internal-api-token-2024` |

## 🧪 Testing

### Test Coverage
The project maintains **comprehensive test coverage**:
- **Instruction Coverage**: **87%** ⭐
- **Branch Coverage**: **67%** ⭐
- **Line Coverage**: **91%** ⭐
- **Method Coverage**: **91%** ⭐
- **Class Coverage**: **100%** ⭐

### Run Tests
```bash
# Unit tests only (fast execution)
mvn test -Dtest='!**/*IntegrationTest'

# Integration tests (requires Docker)
mvn test -Dtest='**/*IntegrationTest' -Dspring.profiles.active=integration

# Generate test coverage report
mvn clean test jacoco:report
```

### Test Categories
- **Unit Tests**: Service layer, rule engine, controllers (466+ tests)
- **Integration Tests**: End-to-end API testing with TestContainers
- **Resilience Tests**: Load testing, failure scenarios

### Test Reports
After running tests, view coverage reports:
- **HTML Report**: `target/site/jacoco/index.html`
- **XML Report**: `target/site/jacoco/jacoco.xml` 
- **CSV Report**: `target/site/jacoco/jacoco.csv`

### Demo Script
```bash
# Run the demo script to see dynamic rules in action
chmod +x demo-dynamic-rules.sh
./demo-dynamic-rules.sh
```

## 📈 Monitoring

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Internal API health
curl http://localhost:8080/internal/fraud-analysis/health \
  -H "Authorization: Bearer internal-api-token-2024"
```

### Metrics
```bash
# Prometheus metrics
curl http://localhost:8080/actuator/prometheus

# Custom fraud detection metrics
curl http://localhost:8080/actuator/metrics/fraud.detection.processing.time
```

### Logging
Structured logging with configurable levels:
```yaml
logging:
  level:
    com.faud.frauddetection: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/fraud-detection.log
```

## 🔒 Security

### Authentication
- **JWT Authentication**: Secure API access
- **API Token Authentication**: Internal service communication (`internal-api-token-2024`)
- **Role-based Access**: Different access levels

### Security Configuration
```yaml
security:
  permitted-paths:
    - /actuator/**
    - /health
    - /fraud-rules/health
    - /internal/fraud-analysis/health
```

## 🚀 Performance Optimization

### Caching Strategy
- **Rule Caching**: Redis-based rule caching with 600s TTL
- **State Management**: User transaction state in Redis
- **Query Optimization**: MyBatis optimized queries with connection pooling

### Scaling
- **Horizontal Scaling**: Stateless application design
- **Auto Scaling**: Kubernetes HPA configuration
- **Load Balancing**: Multi-instance deployment
- **Connection Pooling**: HikariCP with up to 50 connections in production

## 📝 API Documentation

### Core Endpoints

#### Fraud Rule Management
- `GET /api/v1/fraud-rules` - List all rules
- `POST /api/v1/fraud-rules` - Create new rule
- `PUT /api/v1/fraud-rules/{id}` - Update rule
- `DELETE /api/v1/fraud-rules/{id}` - Delete rule
- `PATCH /api/v1/fraud-rules/{id}/toggle` - Toggle rule status
- `POST /api/v1/fraud-rules/quick-create` - Quick create common rules

#### Internal Fraud Analysis (API Token Required)
- `GET /internal/fraud-analysis/results` - Get all detection results
- `GET /internal/fraud-analysis/results/{id}` - Get specific result
- `GET /internal/fraud-analysis/health` - Health check

#### Health & Monitoring
- `GET /actuator/health` - Application health
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

### Development Guidelines
- Maintain test coverage above 80%
- Follow existing code patterns
- Add comprehensive tests for new features
- Update documentation for API changes

## 📞 Support

- **Documentation**: Check this README and TEST_GUIDE.md
- **Issues**: Report bugs via GitHub Issues
- **Test Coverage**: View latest coverage at `target/site/jacoco/index.html`

---