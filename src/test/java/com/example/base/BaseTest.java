package com.example.base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class BaseTest implements ITestListener {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected static ExtentReports extentReports;
    protected static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    protected static Properties config;
    public static RequestSpecification requestSpec;
    
    // Static initialization block to ensure config and requestSpec are initialized
    static {
        try {
            // Initialize config
            config = new Properties();
            String configPath = "src/test/resources/config.properties";
            File configFile = new File(configPath);
            
            if (configFile.exists()) {
                FileInputStream fis = new FileInputStream(configFile);
                config.load(fis);
                fis.close();
            } else {
                config.setProperty("apikey", "special-key");
            }
            
            // Get environment from system property (default: staging)
            String environment = System.getProperty("environment", "staging");
            String baseUrl = config.getProperty(environment + ".baseurl", "https://petstore.swagger.io/v2");
            
            System.out.println("🌍 Running tests in " + environment.toUpperCase() + " environment");
            System.out.println("🔗 Base URL: " + baseUrl);
            
            // Initialize RestAssured
            RestAssured.baseURI = baseUrl;
            RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
            
            // Initialize requestSpec
            requestSpec = new RequestSpecBuilder()
                    .setBaseUri(RestAssured.baseURI)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("api_key", config.getProperty("apikey", "special-key"))
                    .addFilter(new RequestLoggingFilter())
                    .addFilter(new ResponseLoggingFilter())
                    .build();
                    
        } catch (Exception e) {
            System.err.println("Error initializing BaseTest: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @BeforeSuite
    public void setupExtentReports() {
        // Initialize only if not already initialized
        if (extentReports != null) {
            return;
        }
        
        // Create reports directory
        File reportsDir = new File("target/reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        // Generate report filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportPath = "target/reports/PetstoreAPI_TestReport_" + timestamp + ".html";

        // Initialize ExtentSparkReporter
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setDocumentTitle("Petstore API Test Automation Report");
        sparkReporter.config().setReportName("Petstore API Test Results");
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        sparkReporter.config().setEncoding("utf-8");

        // Initialize ExtentReports
        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Application", "Swagger Petstore API");
        extentReports.setSystemInfo("Base URL", "https://petstore.swagger.io/v2");
        extentReports.setSystemInfo("Environment", "Test");
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("User", System.getProperty("user.name"));

        logger.info("ExtentReports initialized successfully at: {}", reportPath);
    }

    @AfterSuite
    public void tearDownExtentReports() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("ExtentReports generated successfully");
        }
    }

    @BeforeClass
    public void setupRestAssured() {
        // Config and requestSpec are initialized in static block
        logger.info("RestAssured configured with baseURI: {}", RestAssured.baseURI);
        logger.info("API Key: {}", config.getProperty("apikey", "special-key"));
    }

    private void loadConfigProperties() {
        config = new Properties();
        try {
            String configPath = "src/test/resources/config.properties";
            File configFile = new File(configPath);
            
            if (!configFile.exists()) {
                logger.warn("Config file not found at: {}. Using default values.", configPath);
                config.setProperty("api.key", "special-key");
                return;
            }

            FileInputStream fis = new FileInputStream(configFile);
            config.load(fis);
            fis.close();
            logger.info("Configuration loaded successfully from: {}", configPath);
        } catch (IOException e) {
            logger.error("Failed to load configuration properties: {}", e.getMessage());
            config.setProperty("api.key", "special-key");
        }
    }

    // ITestListener Implementation

    @Override
    public void onTestStart(ITestResult result) {
        // Ensure ExtentReports is initialized
        if (extentReports == null) {
            setupExtentReports();
        }
        
        String testName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription() != null ? 
                result.getMethod().getDescription() : testName;
        
        ExtentTest test = extentReports.createTest(testName, description);
        extentTest.set(test);
        
        logger.info("========================================");
        logger.info("STARTING TEST: {}", testName);
        logger.info("========================================");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.pass("Test Passed: " + testName);
        }
        
        logger.info("========================================");
        logger.info("TEST PASSED: {}", testName);
        logger.info("Execution Time: {} ms", result.getEndMillis() - result.getStartMillis());
        logger.info("========================================");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.fail("Test Failed: " + testName);
            if (throwable != null) {
                test.fail(throwable);
            }
        }
        
        logger.error("========================================");
        logger.error("TEST FAILED: {}", testName);
        logger.error("Error Message: {}", throwable != null ? throwable.getMessage() : "Unknown error");
        logger.error("Execution Time: {} ms", result.getEndMillis() - result.getStartMillis());
        if (throwable != null) {
            logger.error("Stack Trace:", throwable);
        }
        logger.error("========================================");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.skip("Test Skipped: " + testName);
            if (throwable != null) {
                test.skip(throwable);
            }
        }
        
        logger.warn("========================================");
        logger.warn("TEST SKIPPED: {}", testName);
        logger.warn("Reason: {}", throwable != null ? throwable.getMessage() : "Unknown reason");
        logger.warn("========================================");
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.info("Test failed but within success percentage: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("========================================");
        logger.info("TEST SUITE STARTED: {}", context.getName());
        logger.info("========================================");
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("========================================");
        logger.info("TEST SUITE FINISHED: {}", context.getName());
        logger.info("Total Tests: {}", context.getAllTestMethods().length);
        logger.info("Passed: {}", context.getPassedTests().size());
        logger.info("Failed: {}", context.getFailedTests().size());
        logger.info("Skipped: {}", context.getSkippedTests().size());
        logger.info("========================================");
    }

    // Utility method to get ExtentTest instance for current thread
    protected static ExtentTest getExtentTest() {
        ExtentTest test = extentTest.get();
        if (test == null) {
            logger.warn("ExtentTest is null - test may not be properly initialized");
        }
        return test;
    }

    // Utility method to safely log to ExtentTest
    protected static void logPass(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.pass(message);
        }
    }

    protected static void logFail(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.fail(message);
        }
    }

    protected static void logInfo(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.info(message);
        }
    }

    protected static void logSkip(String message) {
        ExtentTest test = extentTest.get();
        if (test != null) {
            test.skip(message);
        }
    }

    // Utility method to get config property
    protected static String getConfigProperty(String key) {
        return config.getProperty(key);
    }

    // Utility method to get config property with default value
    protected static String getConfigProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
}
