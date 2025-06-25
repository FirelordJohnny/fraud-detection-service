#!/bin/bash

# Fraud Detection Service - Integration Test Runner
# This script runs comprehensive integration tests for message queuing, logging, fraud simulation, and resilience testing

set -e  # Exit on any error

echo "🚀 Starting Fraud Detection Service Integration Tests"
echo "=================================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test configuration
TEST_PROFILE="test"
MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"
TEST_TIMEOUT="300s"

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if required services are running
check_dependencies() {
    print_status "Checking dependencies..."
    
    # Check if Maven is available
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    # Check if Java is available
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    print_success "All dependencies are available"
}

# Function to start test dependencies
start_test_dependencies() {
    print_status "Starting test dependencies..."
    
    # Check if Docker is available for test containers
    if command -v docker &> /dev/null; then
        print_status "Docker is available - TestContainers will be used for integration tests"
    else
        print_warning "Docker is not available - some integration tests may use embedded services"
    fi
}

# Function to run specific test suite
run_test_suite() {
    local test_class=$1
    local test_name=$2
    
    print_status "Running $test_name..."
    
    if mvn test -Dtest="$test_class" -Dspring.profiles.active="$TEST_PROFILE" -q; then
        print_success "$test_name completed successfully"
        return 0
    else
        print_error "$test_name failed"
        return 1
    fi
}

# Function to generate test report
generate_test_report() {
    print_status "Generating test reports..."
    
    # Generate Surefire reports
    mvn surefire-report:report -q
    
    # Generate JaCoCo coverage report
    mvn jacoco:report -q
    
    if [ -f "target/site/jacoco/index.html" ]; then
        print_success "Test coverage report generated: target/site/jacoco/index.html"
    fi
    
    if [ -f "target/site/surefire-report.html" ]; then
        print_success "Test report generated: target/site/surefire-report.html"
    fi
}

# Function to cleanup after tests
cleanup() {
    print_status "Cleaning up test resources..."
    
    # Kill any remaining test processes
    pkill -f "maven" 2>/dev/null || true
    
    # Clean up temporary files
    rm -rf /tmp/fraud-detection-test-* 2>/dev/null || true
    
    print_success "Cleanup completed"
}

# Main test execution
main() {
    local failed_tests=0
    local total_tests=0
    
    echo "Starting integration test execution at $(date)"
    echo
    
    # Setup
    check_dependencies
    start_test_dependencies
    
    # Compile the project first
    print_status "Compiling project..."
    if ! mvn compile -q; then
        print_error "Project compilation failed"
        exit 1
    fi
    print_success "Project compiled successfully"
    
    # Test suites to run
    declare -A test_suites=(
        ["MessageQueueIntegrationTest"]="Message Queue Integration Tests"
        ["LoggingIntegrationTest"]="Logging Integration Tests" 
        ["FraudDetectionSimulationTest"]="Fraud Detection Simulation Tests"
        ["ResilienceIntegrationTest"]="Resilience Integration Tests"
    )
    
    # Run each test suite
    for test_class in "${!test_suites[@]}"; do
        total_tests=$((total_tests + 1))
        if ! run_test_suite "$test_class" "${test_suites[$test_class]}"; then
            failed_tests=$((failed_tests + 1))
        fi
        echo
    done
    
    # Run all integration tests together for comprehensive testing
    print_status "Running comprehensive integration test suite..."
    total_tests=$((total_tests + 1))
    if ! mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active="$TEST_PROFILE" -q; then
        print_error "Comprehensive integration test suite failed"
        failed_tests=$((failed_tests + 1))
    else
        print_success "Comprehensive integration test suite completed"
    fi
    
    # Generate reports
    generate_test_report
    
    # Summary
    echo
    echo "=================================================="
    echo "Integration Test Summary"
    echo "=================================================="
    echo "Total test suites: $total_tests"
    echo "Passed: $((total_tests - failed_tests))"
    echo "Failed: $failed_tests"
    
    if [ $failed_tests -eq 0 ]; then
        print_success "🎉 All integration tests passed!"
        echo
        echo "Test Coverage Report: target/site/jacoco/index.html"
        echo "Test Results Report: target/site/surefire-report.html"
        exit 0
    else
        print_error "❌ $failed_tests test suite(s) failed"
        echo
        echo "Check the test reports for detailed failure information:"
        echo "- Test Results: target/site/surefire-report.html"
        echo "- Logs: target/surefire-reports/"
        exit 1
    fi
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --profile)
            TEST_PROFILE="$2"
            shift 2
            ;;
        --timeout)
            TEST_TIMEOUT="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --profile PROFILE    Set Spring profile (default: test)"
            echo "  --timeout TIMEOUT    Set test timeout (default: 300s)"
            echo "  --help              Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Run main function
main 