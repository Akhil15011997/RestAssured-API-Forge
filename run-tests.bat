@echo off
REM RestAssured API Test Execution Script for Windows
REM Usage: run-tests.bat [smoke|regression|all] [staging|prod]

setlocal enabledelayedexpansion

echo ========================================
echo RestAssured Petstore API Tests
echo ========================================

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed. Please install Maven first.
    exit /b 1
)

REM Determine test suite and environment
set TEST_SUITE=%1
if "%TEST_SUITE%"=="" set TEST_SUITE=smoke

set ENVIRONMENT=%2
if "%ENVIRONMENT%"=="" set ENVIRONMENT=staging

echo Environment: %ENVIRONMENT%

if "%TEST_SUITE%"=="smoke" (
    echo Running Smoke Tests...
    mvn clean test -Dtestng.suite=src/test/resources/testng-smoke.xml -Denvironment=%ENVIRONMENT%
) else if "%TEST_SUITE%"=="regression" (
    echo Running Regression Tests...
    mvn clean test -Dtestng.suite=src/test/resources/testng-regression.xml -Denvironment=%ENVIRONMENT%
) else if "%TEST_SUITE%"=="all" (
    echo Running All Tests...
    mvn clean test -Dtestng.suite=src/test/resources/testng-smoke.xml -Denvironment=%ENVIRONMENT%
    mvn test -Dtestng.suite=src/test/resources/testng-regression.xml -Denvironment=%ENVIRONMENT%
) else (
    echo Invalid option: %TEST_SUITE%
    echo Usage: run-tests.bat [smoke^|regression^|all] [staging^|prod]
    exit /b 1
)

if %ERRORLEVEL% equ 0 (
    echo ========================================
    echo Tests completed successfully!
    echo ========================================
    echo Generating Allure Report...
    
    REM Check if Allure is installed
    where allure >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        mvn allure:report
        echo Allure report generated!
        echo To view the report, run: allure serve target/allure-results
    ) else (
        echo Allure CLI not found. Install from: https://docs.qameta.io/allure/
        echo Or generate report with: mvn allure:report
    )
    
    echo ========================================
    echo Reports Location:
    echo   - ExtentReports: target\reports\
    echo   - Allure Results: target\allure-results\
    echo   - Logs: target\logs\
    echo ========================================
) else (
    echo ========================================
    echo Tests failed! Check logs for details.
    echo ========================================
    exit /b 1
)

endlocal
