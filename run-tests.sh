#!/bin/bash

# RestAssured API Test Execution Script
# Usage: ./run-tests.sh [smoke|regression|all] [staging|prod]

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}RestAssured Petstore API Tests${NC}"
echo -e "${GREEN}========================================${NC}"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed. Please install Maven first.${NC}"
    exit 1
fi

# Determine test suite and environment
TEST_SUITE=${1:-smoke}
ENVIRONMENT=${2:-staging}

echo -e "${YELLOW}Environment: ${ENVIRONMENT}${NC}"

case $TEST_SUITE in
    smoke)
        echo -e "${YELLOW}Running Smoke Tests...${NC}"
        mvn clean test -Dtestng.suite=src/test/resources/testng-smoke.xml -Denvironment=$ENVIRONMENT
        ;;
    regression)
        echo -e "${YELLOW}Running Regression Tests...${NC}"
        mvn clean test -Dtestng.suite=src/test/resources/testng-regression.xml -Denvironment=$ENVIRONMENT
        ;;
    all)
        echo -e "${YELLOW}Running All Tests...${NC}"
        mvn clean test -Dtestng.suite=src/test/resources/testng-smoke.xml -Denvironment=$ENVIRONMENT
        mvn test -Dtestng.suite=src/test/resources/testng-regression.xml -Denvironment=$ENVIRONMENT
        ;;
    *)
        echo -e "${RED}Invalid option: $TEST_SUITE${NC}"
        echo -e "Usage: ./run-tests.sh [smoke|regression|all] [staging|prod]"
        exit 1
        ;;
esac

# Generate Allure Report
if [ $? -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Tests completed successfully!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "${YELLOW}Generating Allure Report...${NC}"
    
    # Check if Allure is installed
    if command -v allure &> /dev/null; then
        mvn allure:report
        echo -e "${GREEN}Allure report generated!${NC}"
        echo -e "${YELLOW}To view the report, run: allure serve target/allure-results${NC}"
    else
        echo -e "${YELLOW}Allure CLI not found. Install with: brew install allure (macOS)${NC}"
        echo -e "${YELLOW}Or generate report with: mvn allure:report${NC}"
    fi
    
    # Show report locations
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Reports Location:${NC}"
    echo -e "  - ExtentReports: ${YELLOW}target/reports/${NC}"
    echo -e "  - Allure Results: ${YELLOW}target/allure-results/${NC}"
    echo -e "  - Logs: ${YELLOW}target/logs/${NC}"
    echo -e "${GREEN}========================================${NC}"
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}Tests failed! Check logs for details.${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi
