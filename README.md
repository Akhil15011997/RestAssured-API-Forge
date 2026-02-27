# RestAssured API Testing Framework - Swagger Petstore

A comprehensive REST API automation testing framework built with RestAssured, TestNG, and Maven for testing the [Swagger Petstore API](https://petstore.swagger.io). This project demonstrates industry best practices for API testing including data-driven testing, parallel execution, multiple reporting mechanisms, and CI/CD integration.

## 📋 Overview

This framework provides **20+ automated tests** covering:
- **CRUD Operations**: Create, Read, Update, Delete operations on Pet resources
- **Smoke Tests**: Critical path validation for rapid feedback (5 tests)
- **Regression Tests**: Boundary conditions, edge cases, concurrent operations (8 tests)
- **Negative Tests**: Invalid inputs, missing authentication, malformed payloads (8 tests)
- **Validation**: Response status codes, JSON schema validation, data integrity checks

**Key Features:**
- RestAssured 5.5.0 for fluent API testing
- TestNG 7.10.2 for test organization and parallel execution
- Dual reporting: ExtentReports (HTML) + Allure (interactive dashboards)
- Log4j2 for comprehensive logging
- Jackson for JSON serialization/deserialization
- POJOs with Lombok for clean data models
- GitHub Actions CI/CD pipeline

## 🛠️ Prerequisites

Before running this project, ensure you have:

- **Java 17** or higher ([Download JDK](https://adoptium.net/))
- **Apache Maven 3.9+** ([Download Maven](https://maven.apache.org/download.cgi))
- **IDE**: IntelliJ IDEA, VSCode, or Windsurf with Maven plugin
- **Allure CLI** (optional, for local report viewing): `brew install allure` (macOS) or [download](https://docs.qameta.io/allure/#_installing_a_commandline)
- **Git** for version control

Verify installations:
```bash
java -version    # Should show Java 17+
mvn -version     # Should show Maven 3.9+
```

## 🚀 Setup Instructions

1. **Clone the repository:**
```bash
git clone <repository-url>
cd RestAssured-API-Forge
```

2. **Install dependencies:**
```bash
mvn clean install
```

3. **Configure API settings** (optional):
   - Edit `src/test/resources/config.properties` to modify base URL or API key
   - Default configuration works with public Petstore API

## ▶️ Running Tests

### Quick Start with Helper Scripts

**Linux/macOS:**
```bash
chmod +x run-tests.sh          # Make executable (first time only)
./run-tests.sh smoke           # Run smoke tests (default: staging)
./run-tests.sh regression      # Run regression tests (default: staging)
./run-tests.sh all             # Run all tests (default: staging)

# Run tests in specific environment
./run-tests.sh smoke staging   # Run smoke tests in staging
./run-tests.sh smoke prod      # Run smoke tests in production
./run-tests.sh all prod        # Run all tests in production
```

**Windows:**
```cmd
run-tests.bat smoke            # Run smoke tests (default: staging)
run-tests.bat regression       # Run regression tests (default: staging)
run-tests.bat all              # Run all tests (default: staging)

REM Run tests in specific environment
run-tests.bat smoke staging    # Run smoke tests in staging
run-tests.bat smoke prod       # Run smoke tests in production
run-tests.bat all prod         # Run all tests in production
```

### Environment-Specific Testing

The framework supports multiple environments. Configure URLs in `config.properties`:

```properties
# Environment URLs
staging.baseurl=https://petstore.swagger.io/v2
prod.baseurl=https://petstore.swagger.io/v2
```

Run tests in specific environment:
```bash
# Default environment is staging
mvn test -Dtestng.suite=src/test/resources/testng-smoke.xml

# Explicitly specify environment
mvn test -Dtestng.suite=src/test/resources/testng-smoke.xml -Denvironment=staging
mvn test -Dtestng.suite=src/test/resources/testng-smoke.xml -Denvironment=prod
```

### Run All Smoke Tests
```bash
mvn clean test -Dtestng.suite=src/test/resources/testng-smoke.xml
# Or simply (uses default smoke suite):
mvn clean test
```

### Run Regression Tests (Parallel Execution)
```bash
mvn clean test -Dtestng.suite=src/test/resources/testng-regression.xml
```

### Run Tests by Group
```bash
mvn test -Dgroups=smoke              # Smoke tests only
mvn test -Dgroups=regression         # Regression tests only
mvn test -Dgroups=negative           # Negative tests only
```

### Generate and View Allure Reports
```bash
mvn clean test -Dtestng.suite=src/test/resources/testng-smoke.xml
allure serve target/allure-results
```

### View ExtentReports
After test execution, open: `target/reports/PetstoreAPI_TestReport_<timestamp>.html`

## 📁 Project Structure

```
RestAssured-API-Forge/
├── src/test/java/com/example/
│   ├── base/
│   │   └── BaseTest.java              # Base test class with setup/teardown
│   ├── pojo/
│   │   ├── Pet.java                   # Pet entity with status enum
│   │   ├── User.java                  # User entity
│   │   ├── Category.java, Tag.java    # Supporting entities
│   │   └── ApiResponse.java           # API response model
│   ├── utils/
│   │   ├── RestClient.java            # HTTP methods wrapper
│   │   └── PayloadHelper.java         # Test data generation
│   ├── listeners/
│   │   └── AllureListener.java        # Allure attachments
│   ├── smoke/
│   │   └── PetSmokeTests.java         # 5 critical path tests
│   ├── regression/
│   │   └── PetRegressionTests.java    # 8 boundary/edge tests
│   └── negative/
│       └── NegativeTests.java         # 8 negative scenario tests
├── src/test/resources/
│   ├── config.properties              # Configuration settings
│   ├── log4j2.xml                     # Logging configuration
│   ├── testng-smoke.xml               # Smoke suite definition
│   └── testng-regression.xml          # Regression suite (parallel)
├── .github/workflows/
│   └── maven.yml                      # CI/CD pipeline
└── pom.xml                            # Maven dependencies
```

## 🧪 Test Coverage

| Test Suite | Count | Coverage |
|------------|-------|----------|
| **Smoke** | 5 | GET by status, POST create, GET by ID, PUT update, DELETE |
| **Regression** | 8 | Name boundaries (1-256 chars), ID boundaries (0, MAX_LONG), invalid status, concurrent creates, null fields, empty arrays, multiple tags, partial updates |
| **Negative** | 8 | Invalid IDs (404), non-existent deletion, invalid endpoints, empty body (400), malformed JSON, oversized payloads (50KB+), missing required fields, invalid URL formats |

## 🔄 CI/CD Integration

This project includes a **GitHub Actions workflow** (`.github/workflows/maven.yml`) that:
- Triggers on push to `main`/`develop` branches and pull requests
- Runs smoke tests first, then regression tests in sequence
- Generates Allure and ExtentReports
- Publishes reports to GitHub Pages
- Comments PR with test results link
- Uploads artifacts (reports, logs) for 30-day retention

**View Reports:** `https://<username>.github.io/<repo-name>/`

## 🔧 Extending the Framework

### Add User API Tests
1. Create `src/test/java/com/example/smoke/UserSmokeTests.java`
2. Use existing `User.java` POJO and `PayloadHelper.createNewUser()`
3. Test endpoints: `POST /user`, `GET /user/{username}`, `PUT /user/{username}`, `DELETE /user/{username}`
4. Add to TestNG suite XML files

### Add Store/Order Tests
1. Create POJOs: `Order.java`, `Store.java` in `pojo/` package
2. Implement tests for: `POST /store/order`, `GET /store/order/{orderId}`, `GET /store/inventory`
3. Update `PayloadHelper` with order creation methods

## 🐛 Troubleshooting

### Proxy Configuration
If behind a corporate proxy, add to `pom.xml`:
```xml
<configuration>
  <proxy>
    <host>proxy.company.com</host>
    <port>8080</port>
  </proxy>
</configuration>
```

### API Rate Limits
The public Petstore API has rate limits. If tests fail with `429 Too Many Requests`:
- Add delays: `Thread.sleep(1000)` between tests
- Reduce parallel thread count in `testng-regression.xml`
- Use `@Test(priority = X)` to sequence instead of parallelize

### Connection Timeouts
Increase timeout in `config.properties`:
```properties
timeout=10000
```

### Compilation Issues
```bash
mvn clean compile          # Clean and recompile
mvn dependency:purge-local-repository  # Clear corrupted dependencies
```

## 📊 Reports & Logs

- **Allure Reports**: `target/allure-results/` (raw), `target/site/allure-maven-plugin/` (generated)
- **ExtentReports**: `target/reports/PetstoreAPI_TestReport_*.html`
- **Logs**: `target/logs/petstore-api-tests.log` (rotates daily, max 10MB)
- **Surefire Reports**: `target/surefire-reports/` (TestNG XML results)

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-test-suite`
3. Commit changes: `git commit -am 'Add User API tests'`
4. Push to branch: `git push origin feature/new-test-suite`
5. Submit a Pull Request

---

**Built with ❤️ using RestAssured, TestNG, and best practices in API test automation.**
