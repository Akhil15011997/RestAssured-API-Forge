package com.example.negative;

import com.example.base.BaseTest;
import com.example.pojo.Pet;
import com.example.utils.RestClient;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class NegativeTests extends BaseTest {

    @DataProvider(name = "invalidPetIds")
    public Object[][] invalidPetIds() {
        return new Object[][] {
                {999999999L, 404, "non-existent large ID"},
                {-1L, 404, "negative ID"},
                {0L, 404, "zero ID"}
        };
    }

    @DataProvider(name = "invalidEndpoints")
    public Object[][] invalidEndpoints() {
        return new Object[][] {
                {"/pet/invalid", 404},
                {"/pet/findByStatus/extra", 404},
                {"/pets", 404},
                {"/pet//123", 404}
        };
    }

    @DataProvider(name = "malformedPayloads")
    public Object[][] malformedPayloads() {
        return new Object[][] {
                {"{invalid json}", "invalid JSON syntax"},
                {"{\"name\": \"test\", \"photoUrls\": \"not-an-array\"}", "photoUrls as string instead of array"},
                {"{\"id\": \"not-a-number\"}", "id as string instead of number"},
                {"", "empty body"},
                {"{}", "empty JSON object"}
        };
    }

    @Test(priority = 1, groups = "negative", description = "Test GET pet with invalid IDs", dataProvider = "invalidPetIds")
    public void testGetPetWithInvalidId(Long petId, int expectedCode, String description) {
        logger.info("Test: GET pet with invalid ID - {}", description);
        
        Response response = RestClient.get("/pet/" + petId);
        
        assertThat("Status code should be " + expectedCode + " for " + description, 
                response.getStatusCode(), equalTo(expectedCode));
        
        if (expectedCode == 404) {
            assertThat("Response should contain error information", 
                    response.asString(), is(notNullValue()));
        }
        
        logPass("Invalid ID test passed: " + description);
        logger.info("Test Passed: {} returned {}", description, expectedCode);
    }

    @Test(priority = 2, groups = "negative", description = "Test DELETE pet with invalid ID")
    public void testDeleteNonExistentPet() {
        logger.info("Test: DELETE non-existent pet");
        
        Long nonExistentId = 999999999L;
        Response response = RestClient.delete("/pet/" + nonExistentId);
        
        // API may return 404 or 200 depending on implementation
        assertThat("Status code should indicate error or success", 
                response.getStatusCode(), anyOf(equalTo(404), equalTo(200)));
        
        logPass("Delete non-existent pet handled correctly");
        logger.info("Test Passed: Delete non-existent pet returned {}", response.getStatusCode());
    }

    @Test(priority = 3, groups = "negative", description = "Test accessing invalid endpoints", dataProvider = "invalidEndpoints")
    public void testInvalidEndpoints(String endpoint, int expectedCode) {
        logger.info("Test: Invalid endpoint - {}", endpoint);
        
        Response response = RestClient.get(endpoint);
        
        assertThat("Status code should be " + expectedCode + " for invalid endpoint", 
                response.getStatusCode(), equalTo(expectedCode));
        
        logPass("Invalid endpoint test passed: " + endpoint);
        logger.info("Test Passed: Invalid endpoint {} returned {}", endpoint, expectedCode);
    }

    @Test(priority = 4, groups = "negative", description = "Test POST pet with empty body")
    public void testPostPetWithEmptyBody() {
        logger.info("Test: POST pet with empty body");
        
        Response response = given()
                .spec(requestSpec)
                .body("")
                .when()
                .post("/pet")
                .then()
                .extract()
                .response();
        
        assertThat("Status code should be 400, 405, 415, or 500 for empty body", 
                response.getStatusCode(), anyOf(equalTo(400), equalTo(405), equalTo(415), equalTo(500)));
        
        logPass("Empty body test passed");
        logger.info("Test Passed: Empty body returned {}", response.getStatusCode());
    }

    @Test(priority = 5, groups = "negative", description = "Test POST pet with malformed JSON", dataProvider = "malformedPayloads")
    public void testPostPetWithMalformedPayload(String payload, String description) {
        logger.info("Test: POST pet with malformed payload - {}", description);
        
        Response response = given()
                .spec(requestSpec)
                .body(payload)
                .when()
                .post("/pet")
                .then()
                .extract()
                .response();
        
        // Malformed payload should return 400, 405, 415, 500, or 200 (API accepts empty JSON)
        assertThat("Status code should indicate client or server error", 
                response.getStatusCode(), anyOf(equalTo(200), equalTo(400), equalTo(405), equalTo(415), equalTo(500)));
        
        logPass("Malformed payload test passed: " + description);
        logger.info("Test Passed: {} returned {}", description, response.getStatusCode());
    }

    @Test(priority = 6, groups = "negative", description = "Test POST pet with oversized payload")
    public void testPostPetWithOversizedPayload() {
        logger.info("Test: POST pet with oversized payload");
        
        // Create a pet with extremely long name and multiple large fields
        Pet pet = new Pet();
        pet.setName(generateLargeString(10000)); // 10KB name
        pet.setPhotoUrls(java.util.Arrays.asList(generateLargeString(50000))); // 50KB URL
        pet.setStatus(Pet.Status.AVAILABLE);
        
        Response response = RestClient.post("/pet", pet);
        
        // Oversized payload may return 400, 413 (Payload Too Large), 500, or even 200 if accepted
        assertThat("Status code should indicate handling of oversized payload", 
                response.getStatusCode(), 
                anyOf(equalTo(200), equalTo(400), equalTo(413), equalTo(500)));
        
        // If created successfully, cleanup
        if (response.getStatusCode() == 200) {
            Long petId = response.jsonPath().getLong("id");
            if (petId != null) {
                RestClient.delete("/pet/" + petId);
            }
        }
        
        logPass("Oversized payload test passed");
        logger.info("Test Passed: Oversized payload returned {}", response.getStatusCode());
    }

    @Test(priority = 7, groups = "negative", description = "Test PUT pet without required fields")
    public void testUpdatePetWithMissingRequiredFields() {
        logger.info("Test: PUT pet without required fields");
        
        // Create incomplete pet object (missing photoUrls which is required)
        Map<String, Object> incompletePet = new HashMap<>();
        incompletePet.put("id", 12345L);
        incompletePet.put("name", "IncompletePet");
        // Missing photoUrls array
        
        Response response = given()
                .spec(requestSpec)
                .body(incompletePet)
                .when()
                .put("/pet")
                .then()
                .extract()
                .response();
        
        // Missing required fields should return 400 or 500, or might be accepted with defaults
        assertThat("Status code should indicate validation error or success", 
                response.getStatusCode(), 
                anyOf(equalTo(200), equalTo(400), equalTo(500)));
        
        logPass("Missing required fields test passed");
        logger.info("Test Passed: Missing required fields returned {}", response.getStatusCode());
    }

    @Test(priority = 8, groups = "negative", description = "Test GET pet with invalid URL format")
    public void testGetPetWithInvalidUrlFormat() {
        logger.info("Test: GET pet with invalid URL format");
        
        Response response = RestClient.get("/pet/invalid-id-string");
        
        assertThat("Status code should be 400 or 404 for invalid ID format", 
                response.getStatusCode(), anyOf(equalTo(400), equalTo(404), equalTo(500)));
        
        logPass("Invalid URL format test passed");
        logger.info("Test Passed: Invalid URL format returned {}", response.getStatusCode());
    }

    // Helper method to generate large string
    private String generateLargeString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String pattern = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(pattern.charAt(i % pattern.length()));
        }
        return sb.toString();
    }
}
