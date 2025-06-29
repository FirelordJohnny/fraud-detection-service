# Test Execution Guide

## Overview

This project provides multiple test execution modes to adapt to different development and deployment environments:

- **Unit Tests**: No external service dependencies, fastest execution
- **Lightweight Tests**: Using Mock and in-memory databases
- **Integration Tests**: Using TestContainers and real services
- **Resilience Tests**: Testing system recovery capabilities under failure conditions

## Quick Start

### 1. Run Unit Tests Only (Recommended for Quick Verification)
```bash
# Linux/Mac
./run-tests.sh unit

# Windows 
mvn test -Dtest="!**/*IntegrationTest,!**/Resilience*Test" -DfailIfNoTests=false
```

### 2. Run Lightweight Tests (No Docker Required)
```bash
# Linux/Mac
./run-tests.sh lightweight

# Windows
mvn test -Dtest="!**/Resilience*Test" -Dspring.profiles.active=test -DfailIfNoTests=false
```

### 3. Run Full Integration Tests (Docker Required)
```bash
# Linux/Mac
./run-tests.sh integration

# Windows
mvn test -Dtest="**/*IntegrationTest" -Dintegration.tests.enabled=true -DfailIfNoTests=false
```

## Test Environment Requirements

### Minimal Environment (Unit Tests)
- Java 17+
- Maven 3.6+

### Lightweight Test Environment
- Java 17+
- Maven 3.6+
- In-memory H2 database (auto-start)
- Embedded Redis (auto-start)
- Embedded Kafka (auto-start)

### Full Integration Test Environment
- Java 17+
- Maven 3.6+
- **Docker** (must be running)
- At least 2GB available memory

## Test Types Explained

### Unit Tests
- **File Location**: `src/test/java/**/*Test.java` (excluding `*IntegrationTest`)
- **Execution Time**: 20-30 seconds (466+ tests)
- **Dependencies**: No external dependencies
- **Purpose**: Verify business logic and individual component functionality

### Integration Tests
- **File Location**: `src/test/java/**/*IntegrationTest.java`
- **Execution Time**: 2-5 minutes (first run requires Docker image downloads)
- **Dependencies**: Docker, MySQL Container, TestContainers
- **Purpose**: Verify component collaboration and external system integration
- **Note**: Requires `-Dintegration.tests.enabled=true` system property

### Resilience Tests
- **File Location**: `src/test/java/**/Resilience*Test.java`
- **Execution Time**: 3-10 minutes
- **Dependencies**: Same as integration tests
- **Purpose**: Verify system behavior under failures, high load, and network issues
- **Note**: Requires `-Dintegration.tests.enabled=true` system property

## Detailed Command Instructions

### Using Scripts (Recommended)
```bash
# View all available options
./run-tests.sh

# Run unit tests
./run-tests.sh unit

# Run lightweight tests
./run-tests.sh lightweight

# Run integration tests
./run-tests.sh integration

# Run resilience tests
./run-tests.sh resilience

# Run all tests
./run-tests.sh all

# Run tests and generate coverage report
./run-tests.sh coverage
```

### Using Maven Directly
```bash
# Unit tests
mvn test -Dtest="!**/*IntegrationTest,!**/Resilience*Test"

# Integration tests (requires Docker and system property)
mvn test -Dtest="**/*IntegrationTest" -Dintegration.tests.enabled=true

# Resilience tests (requires Docker and system property)
mvn test -Dtest="**/Resilience*Test" -Dintegration.tests.enabled=true

# All tests
mvn test -Dintegration.tests.enabled=true

# Generate coverage report
mvn clean test jacoco:report
```

## Common Issue Resolution

### 1. Docker Related Issues

**Issue**: `Docker is not running`
```bash
# Solution: Start Docker
# Windows: Start Docker Desktop
# Linux: sudo systemctl start docker
# Mac: Start Docker Desktop
```

**Issue**: `Could not pull image mysql:8.0`
```bash
# Solution: Check network connection and pre-pull images
docker pull mysql:8.0
```

### 2. Port Conflict Issues

**Issue**: `Port 6379 already in use`
```bash
# Solution: Stop process using the port
# Windows: netstat -ano | findstr 6379
# Linux/Mac: lsof -i :6379
```

### 3. Memory Issues

**Issue**: Tests run slowly or fail
```bash
# Solution: Increase JVM memory
export MAVEN_OPTS="-Xmx2g"
mvn test
```

### 4. TestContainers Issues

**Issue**: Container startup failures
```bash
# Solution: Clean Docker resources
docker system prune -f
docker volume prune -f
```

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Tests
on: [push, pull_request]
jobs:
  unit-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run unit tests
        run: mvn test -Dtest="!**/*IntegrationTest"
      - name: Generate coverage report
        run: mvn jacoco:report
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        
  integration-tests:
    runs-on: ubuntu-latest
    services:
      docker:
        image: docker:dind
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run integration tests
        run: mvn test -Dtest="**/*IntegrationTest" -Dintegration.tests.enabled=true
```

### Jenkins Example
```groovy
pipeline {
    agent any
    stages {
        stage('Unit Tests') {
            steps {
                sh './run-tests.sh unit'
            }
        }
        stage('Integration Tests') {
            steps {
                sh './run-tests.sh integration'
            }
        }
    }
}
```

## Test Coverage

### Current Coverage Metrics
- **Instruction Coverage**: **87%** ⭐
- **Branch Coverage**: **67%** ⭐
- **Line Coverage**: **91%** ⭐
- **Method Coverage**: **91%** ⭐
- **Class Coverage**: **100%** ⭐

### Generate Coverage Report
```bash
./run-tests.sh coverage
# Or directly with Maven
mvn clean test jacoco:report
```

### Report Locations
- **HTML Report**: `target/site/jacoco/index.html` (main report)
- **XML Report**: `target/site/jacoco/jacoco.xml` (for CI/CD)
- **CSV Report**: `target/site/jacoco/jacoco.csv` (for analysis)

## Performance Benchmarks

Execution time reference on standard development machines:

- **Unit Tests**: 20-30 seconds (466+ tests)
- **Lightweight Tests**: 1-2 minutes
- **Integration Tests**: 3-5 minutes (longer on first run, requires Docker)
- **Resilience Tests**: 5-10 minutes
- **Full Test Suite**: 6-12 minutes (without integration tests)
- **Full Suite with Integration**: 10-20 minutes (first run with Docker setup)

## Best Practices

1. **Daily Development**: Use `./run-tests.sh unit` for quick verification (20-30 seconds)
2. **Feature Completion**: Use `./run-tests.sh lightweight` for comprehensive testing without Docker
3. **Before Commit**: Run `mvn clean test` to ensure all unit tests pass
4. **Periodic**: Use `./run-tests.sh coverage` to check test coverage (target: >80%)
5. **Full Validation**: Use `./run-tests.sh integration` only when Docker is available
6. **CI/CD Pipeline**: Use unit tests for fast feedback, integration tests for nightly builds

## Troubleshooting

If you encounter issues, follow these troubleshooting steps:

1. Check Java version: `java -version`
2. Check Maven version: `mvn -version`
3. Check if Docker is running: `docker info`
4. Clean and re-run: `mvn clean && ./run-tests.sh unit`
5. View detailed logs: `mvn test -X`

## Contact Support

If none of the above methods resolve the issue, please:
1. Collect error logs
2. Record system environment information
3. Contact the development team 