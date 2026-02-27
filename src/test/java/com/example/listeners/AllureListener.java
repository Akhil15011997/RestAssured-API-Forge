package com.example.listeners;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.restassured.RestAssured;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class AllureListener implements ITestListener, OrderedFilter {

    private static final Logger logger = LogManager.getLogger(AllureListener.class);
    private static final ThreadLocal<String> requestBody = new ThreadLocal<>();
    private static final ThreadLocal<String> responseBody = new ThreadLocal<>();

    public AllureListener() {
        // Register this filter with RestAssured
        RestAssured.filters(this);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // Execute last to capture final request/response
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, 
                          FilterableResponseSpecification responseSpec, 
                          FilterContext ctx) {
        // Capture request details
        String reqBody = requestSpec.getBody() != null ? requestSpec.getBody().toString() : "";
        requestBody.set(reqBody);
        
        // Execute request
        Response response = ctx.next(requestSpec, responseSpec);
        
        // Capture response details
        String respBody = response.asString();
        responseBody.set(respBody);
        
        return response;
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        
        logger.info("Allure: Test started - {}", testName);
        Allure.getLifecycle().getCurrentTestCase().ifPresent(uuid -> {
            Allure.step("Starting test: " + testName);
        });
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("Allure: Test passed - {}", testName);
        
        // Attach request and response
        attachRequestResponse();
        
        // Add test information
        Allure.step("Test completed successfully: " + testName);
        
        // Clear thread locals
        clearThreadLocals();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        
        logger.error("Allure: Test failed - {}", testName);
        
        // Attach request and response
        attachRequestResponse();
        
        // Attach failure details
        if (throwable != null) {
            attachText("Failure Message", throwable.getMessage());
            attachText("Stack Trace", getStackTrace(throwable));
        }
        
        // Add failure step
        Allure.step("Test failed: " + testName);
        
        // Clear thread locals
        clearThreadLocals();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        
        logger.warn("Allure: Test skipped - {}", testName);
        
        if (throwable != null) {
            attachText("Skip Reason", throwable.getMessage());
        }
        
        Allure.step("Test skipped: " + testName);
        
        // Clear thread locals
        clearThreadLocals();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.info("Allure: Test failed but within success percentage - {}", 
                result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("Allure: Test suite started - {}", context.getName());
        Allure.step("Test Suite: " + context.getName() + " started");
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("Allure: Test suite finished - {}", context.getName());
        
        // Add suite summary
        String summary = String.format(
                "Test Suite: %s%nTotal: %d | Passed: %d | Failed: %d | Skipped: %d",
                context.getName(),
                context.getAllTestMethods().length,
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size()
        );
        
        attachText("Test Suite Summary", summary);
        Allure.step("Test Suite: " + context.getName() + " completed");
    }

    /**
     * Attach request and response to Allure report
     */
    private void attachRequestResponse() {
        String reqBody = requestBody.get();
        String respBody = responseBody.get();
        
        if (reqBody != null && !reqBody.isEmpty()) {
            attachJson("Request Body", reqBody);
        }
        
        if (respBody != null && !respBody.isEmpty()) {
            attachJson("Response Body", respBody);
        }
    }

    /**
     * Attach JSON content to Allure report
     */
    @Attachment(value = "{name}", type = "application/json", fileExtension = ".json")
    public byte[] attachJson(String name, String content) {
        logger.debug("Attaching JSON to Allure: {}", name);
        return content.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Attach text content to Allure report
     */
    @Attachment(value = "{name}", type = "text/plain", fileExtension = ".txt")
    public String attachText(String name, String content) {
        logger.debug("Attaching text to Allure: {}", name);
        return content;
    }

    /**
     * Attach screenshot (placeholder for API testing)
     */
    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] attachScreenshot(byte[] screenshot) {
        return screenshot;
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        if (throwable.getCause() != null) {
            sb.append("Caused by: ");
            sb.append(getStackTrace(throwable.getCause()));
        }
        return sb.toString();
    }

    /**
     * Clear thread local variables
     */
    private void clearThreadLocals() {
        requestBody.remove();
        responseBody.remove();
    }

    /**
     * Manually attach request body (can be called from test classes)
     */
    public static void attachRequestBody(String request) {
        Allure.addAttachment("Request Body", "application/json", 
                new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)), ".json");
    }

    /**
     * Manually attach response body (can be called from test classes)
     */
    public static void attachResponseBody(String response) {
        Allure.addAttachment("Response Body", "application/json", 
                new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)), ".json");
    }

    /**
     * Attach custom step
     */
    public static void addStep(String stepDescription) {
        Allure.step(stepDescription);
    }

    /**
     * Attach custom parameter
     */
    public static void addParameter(String name, String value) {
        Allure.parameter(name, value);
    }
}
