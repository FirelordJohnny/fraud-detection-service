#!/bin/bash

# Fraud Detection Service Test Runner
# This script provides different test execution modes for local development

set -e

echo "🚀 Fraud Detection Service Test Runner"
echo "======================================"

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "❌ Docker is not running. Please start Docker first."
        echo "   TestContainers require Docker to run integration tests."
        exit 1
    fi
    echo "✅ Docker is running"
}

# Function to run unit tests only
run_unit_tests() {
    echo "🧪 Running Unit Tests..."
    mvn test -Dtest='!**/*IntegrationTest,!**/Resilience*Test' -DfailIfNoTests=false
}

# Function to run integration tests with TestContainers
run_integration_tests() {
    echo "🔧 Running Integration Tests with TestContainers..."
    check_docker
    mvn test -Dtest='**/*IntegrationTest' -Dspring.profiles.active=integration -DfailIfNoTests=false
}

# Function to run resilience tests
run_resilience_tests() {
    echo "💪 Running Resilience Tests..."
    check_docker
    mvn test -Dtest='**/Resilience*Test' -Dspring.profiles.active=integration -DfailIfNoTests=false
}

# Function to run all tests
run_all_tests() {
    echo "🎯 Running All Tests..."
    check_docker
    mvn test -Dspring.profiles.active=integration
}

# Function to run tests with coverage
run_tests_with_coverage() {
    echo "📊 Running Tests with Coverage Report..."
    check_docker
    mvn clean test jacoco:report -Dspring.profiles.active=integration
    echo "📈 Coverage report available at: target/site/jacoco/index.html"
}

# Function to run lightweight tests (no containers)
run_lightweight_tests() {
    echo "⚡ Running Lightweight Tests (Unit + Mocked Integration)..."
    mvn test -Dtest='!**/Resilience*Test' -Dspring.profiles.active=test -DfailIfNoTests=false
}

# Main menu
case "${1:-menu}" in
    "unit")
        run_unit_tests
        ;;
    "integration")
        run_integration_tests
        ;;
    "resilience")
        run_resilience_tests
        ;;
    "all")
        run_all_tests
        ;;
    "coverage")
        run_tests_with_coverage
        ;;
    "lightweight")
        run_lightweight_tests
        ;;
    "menu"|*)
        echo ""
        echo "Available test modes:"
        echo "  unit         - Run unit tests only (fastest)"
        echo "  lightweight  - Run unit + mocked integration tests"
        echo "  integration  - Run integration tests with TestContainers"
        echo "  resilience   - Run resilience tests"
        echo "  all          - Run all tests"
        echo "  coverage     - Run all tests with coverage report"
        echo ""
        echo "Usage: $0 [mode]"
        echo "Example: $0 unit"
        echo ""
        echo "For CI/CD environments without Docker:"
        echo "  $0 lightweight"
        echo ""
        echo "For full local testing:"
        echo "  $0 all"
        ;;
esac 