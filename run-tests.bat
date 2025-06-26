@echo off
setlocal enabledelayedexpansion

REM Fraud Detection Service Test Runner for Windows
REM This script provides different test execution modes for local development

echo 🚀 Fraud Detection Service Test Runner
echo ======================================

REM Function to check if Docker is running
:check_docker
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running. Please start Docker first.
    echo    TestContainers require Docker to run integration tests.
    exit /b 1
)
echo ✅ Docker is running
goto :eof

REM Function to run unit tests only
:run_unit_tests
echo 🧪 Running Unit Tests...
mvn test -Dtest="!**/*IntegrationTest,!**/Resilience*Test" -DfailIfNoTests=false
goto :eof

REM Function to run integration tests with TestContainers
:run_integration_tests
echo 🔧 Running Integration Tests with TestContainers...
call :check_docker
if errorlevel 1 exit /b 1
mvn test -Dtest="**/*IntegrationTest" -Dspring.profiles.active=integration -DfailIfNoTests=false
goto :eof

REM Function to run resilience tests
:run_resilience_tests
echo 💪 Running Resilience Tests...
call :check_docker
if errorlevel 1 exit /b 1
mvn test -Dtest="**/Resilience*Test" -Dspring.profiles.active=integration -DfailIfNoTests=false
goto :eof

REM Function to run all tests
:run_all_tests
echo 🎯 Running All Tests...
call :check_docker
if errorlevel 1 exit /b 1
mvn test -Dspring.profiles.active=integration
goto :eof

REM Function to run tests with coverage
:run_tests_with_coverage
echo 📊 Running Tests with Coverage Report...
call :check_docker
if errorlevel 1 exit /b 1
mvn clean test jacoco:report -Dspring.profiles.active=integration
echo 📈 Coverage report available at: target\site\jacoco\index.html
goto :eof

REM Function to run lightweight tests (no containers)
:run_lightweight_tests
echo ⚡ Running Lightweight Tests (Unit + Mocked Integration)...
mvn test -Dtest="!**/Resilience*Test" -Dspring.profiles.active=test -DfailIfNoTests=false
goto :eof

REM Main menu
if "%1"=="unit" (
    call :run_unit_tests
) else if "%1"=="lightweight" (
    call :run_lightweight_tests
) else if "%1"=="integration" (
    call :run_integration_tests
) else if "%1"=="resilience" (
    call :run_resilience_tests
) else if "%1"=="all" (
    call :run_all_tests
) else if "%1"=="coverage" (
    call :run_tests_with_coverage
) else (
    echo.
    echo Available test modes:
    echo   unit         - Run unit tests only (fastest)
    echo   lightweight  - Run unit + mocked integration tests
    echo   integration  - Run integration tests with TestContainers
    echo   resilience   - Run resilience tests
    echo   all          - Run all tests
    echo   coverage     - Run all tests with coverage report
    echo.
    echo Usage: %0 [mode]
    echo Example: %0 unit
    echo.
    echo For CI/CD environments without Docker:
    echo   %0 lightweight
    echo.
    echo For full local testing:
    echo   %0 all
) 