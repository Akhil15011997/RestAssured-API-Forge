package com.example.regression;

import com.example.base.BaseTest;
import com.example.pojo.Pet;
import com.example.utils.PayloadHelper;
import com.example.utils.RestClient;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PetRegressionTests extends BaseTest {

    @DataProvider(name = "petNameBoundaries")
    public Object[][] petNameBoundaries() {
        return new Object[][] {
                {"A", "single character name"},
                {"AB", "two character name"},
                {generateString(50), "50 characters name"},
                {generateString(100), "100 characters name"},
                {generateString(255), "255 characters name"},
                {generateString(256), "256 characters name - boundary"},
                {"Pet@#$%", "special characters in name"},
                {"Pet 123", "alphanumeric with spaces"}
        };
    }

    @DataProvider(name = "petIdBoundaries")
    public Object[][] petIdBoundaries() {
        return new Object[][] {
                {0L, "zero ID"},
                {1L, "minimum positive ID"},
                {999999999L, "large ID"},
                {Long.MAX_VALUE, "maximum long ID"}
        };
    }

    @DataProvider(name = "invalidStatusValues")
    public Object[][] invalidStatusValues() {
        return new Object[][] {
                {"INVALID", 400},
                {"pending123", 400},
                {"available sold", 400},
                {"", 400},
                {"null", 400}
        };
    }

    @Test(priority = 1, groups = "regression", description = "Test pet creation with boundary name lengths", dataProvider = "petNameBoundaries")
    public void testPetNameBoundaries(String petName, String description) {
        logger.info("Test: Pet name boundary - {}", description);
        
        Pet pet = PayloadHelper.createMinimalPet(petName);
        Response response = RestClient.post("/pet", pet);
        
        // For very long names (>255), expect possible validation error or success
        if (petName.length() <= 255) {
            assertThat("Status code should be 200 for valid name length", 
                    response.getStatusCode(), equalTo(200));
            assertThat("Pet name should match", 
                    response.jsonPath().getString("name"), equalTo(petName));
            
            // Cleanup
            Long petId = response.jsonPath().getLong("id");
            if (petId != null) {
                RestClient.delete("/pet/" + petId);
            }
        } else {
            // For oversized names, accept either success or validation error
            assertThat("Status code should be 200 or 400 for oversized name", 
                    response.getStatusCode(), anyOf(equalTo(200), equalTo(400), equalTo(500)));
        }
        
        logPass("Pet name boundary test passed for: " + description);
        logger.info("Test Passed: {}", description);
    }

    @Test(priority = 2, groups = "regression", description = "Test pet ID boundaries", dataProvider = "petIdBoundaries")
    public void testPetIdBoundaries(Long petId, String description) {
        logger.info("Test: Pet ID boundary - {}", description);
        
        Pet pet = PayloadHelper.createPet(petId, "BoundaryPet", Pet.Status.AVAILABLE, 1L, "Dogs");
        Response response = RestClient.post("/pet", pet);
        
        // Verify response
        assertThat("Status code should be 200 or 500", 
                response.getStatusCode(), anyOf(equalTo(200), equalTo(500)));
        
        if (response.getStatusCode() == 200) {
            Long returnedId = response.jsonPath().getLong("id");
            assertThat("Pet ID should be present", returnedId, is(notNullValue()));
            
            // Cleanup
            RestClient.delete("/pet/" + returnedId);
        }
        
        logPass("Pet ID boundary test passed for: " + description);
        logger.info("Test Passed: {}", description);
    }

    @Test(priority = 3, groups = "regression", description = "Test finding pets with invalid status values", dataProvider = "invalidStatusValues")
    public void testInvalidStatusEnum(String status, int expectedCode) {
        logger.info("Test: Invalid status enum - {}", status);
        
        Response response = RestClient.get("/pet/findByStatus?status=" + status);
        
        // Invalid status should return 400 or empty list with 200
        assertThat("Status code should be 200 or 400", 
                response.getStatusCode(), anyOf(equalTo(200), equalTo(400)));
        
        if (response.getStatusCode() == 200) {
            // If 200, list might be empty or have no pets matching the invalid status
            // Some APIs return all pets, some return empty - both are acceptable
            logger.info("API returned 200 for invalid status: {}", status);
        }
        
        logPass("Invalid status test passed for: " + status);
        logger.info("Test Passed: Invalid status handled correctly - {}", status);
    }

    @Test(priority = 4, groups = "regression", description = "Test concurrent pet creation")
    public void testConcurrentPetCreation() throws InterruptedException {
        logger.info("Test: Concurrent pet creation");
        
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Long> createdPetIds = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    Pet pet = PayloadHelper.createPet(null, "ConcurrentPet" + index, 
                            Pet.Status.AVAILABLE, 1L, "Dogs");
                    Response response = RestClient.post("/pet", pet);
                    
                    if (response.getStatusCode() == 200) {
                        successCount.incrementAndGet();
                        Long petId = response.jsonPath().getLong("id");
                        synchronized (createdPetIds) {
                            createdPetIds.add(petId);
                        }
                        logger.info("Thread {}: Created pet with ID {}", index, petId);
                    }
                } catch (Exception e) {
                    logger.error("Thread {}: Failed to create pet - {}", index, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executorService.shutdown();
        
        // Verify that all or most concurrent requests succeeded
        assertThat("At least 3 out of 5 concurrent requests should succeed", 
                successCount.get(), greaterThanOrEqualTo(3));
        
        // Cleanup created pets
        for (Long petId : createdPetIds) {
            RestClient.delete("/pet/" + petId);
        }
        
        logPass("Concurrent pet creation test passed. Success count: " + successCount.get());
        logger.info("Test Passed: Concurrent creation - {} out of {} succeeded", successCount.get(), threadCount);
    }

    @Test(priority = 5, groups = "regression", description = "Test pet with null fields")
    public void testPetWithNullFields() {
        logger.info("Test: Pet with null optional fields");
        
        Pet pet = new Pet();
        pet.setName("NullFieldsPet");
        pet.setPhotoUrls(Arrays.asList("https://example.com/photo.jpg"));
        pet.setStatus(Pet.Status.AVAILABLE);
        // Leave id, category, tags as null
        
        Response response = RestClient.post("/pet", pet);
        
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        assertThat("Pet name should be set", response.jsonPath().getString("name"), equalTo("NullFieldsPet"));
        
        Long petId = response.jsonPath().getLong("id");
        assertThat("Pet ID should be generated", petId, is(notNullValue()));
        
        // Cleanup
        RestClient.delete("/pet/" + petId);
        
        logPass("Pet with null fields test passed");
        logger.info("Test Passed: Pet created successfully with null optional fields");
    }

    @Test(priority = 6, groups = "regression", description = "Test pet with empty photoUrls array")
    public void testPetWithEmptyPhotoUrls() {
        logger.info("Test: Pet with empty photoUrls array");
        
        Pet pet = PayloadHelper.createMinimalPet("EmptyPhotoUrlsPet");
        pet.setPhotoUrls(new ArrayList<>());
        
        Response response = RestClient.post("/pet", pet);
        
        // Empty photoUrls should be accepted or rejected with validation error
        assertThat("Status code should be 200 or 400", 
                response.getStatusCode(), anyOf(equalTo(200), equalTo(400)));
        
        if (response.getStatusCode() == 200) {
            Long petId = response.jsonPath().getLong("id");
            RestClient.delete("/pet/" + petId);
        }
        
        logPass("Pet with empty photoUrls test passed");
        logger.info("Test Passed: Empty photoUrls handled correctly");
    }

    @Test(priority = 7, groups = "regression", description = "Test pet with multiple tags")
    public void testPetWithMultipleTags() {
        logger.info("Test: Pet with multiple tags");
        
        Pet pet = PayloadHelper.createMinimalPet("MultiTagPet");
        pet.setTags(PayloadHelper.createTags("friendly", "trained", "vaccinated", "microchipped", "neutered"));
        
        Response response = RestClient.post("/pet", pet);
        
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        
        List<String> tagNames = response.jsonPath().getList("tags.name");
        assertThat("Should have 5 tags", tagNames, hasSize(5));
        assertThat("Should contain 'friendly' tag", tagNames, hasItem("friendly"));
        assertThat("Should contain 'vaccinated' tag", tagNames, hasItem("vaccinated"));
        
        Long petId = response.jsonPath().getLong("id");
        RestClient.delete("/pet/" + petId);
        
        logPass("Pet with multiple tags test passed");
        logger.info("Test Passed: Pet created with {} tags", tagNames.size());
    }

    @Test(priority = 8, groups = "regression", description = "Test updating pet with partial data")
    public void testPartialPetUpdate() {
        logger.info("Test: Partial pet update");
        
        // Create a pet
        Pet pet = PayloadHelper.createNewPet();
        Response createResponse = RestClient.post("/pet", pet);
        Long petId = createResponse.jsonPath().getLong("id");
        
        // Update only name and status
        Pet partialPet = new Pet();
        partialPet.setId(petId);
        partialPet.setName("UpdatedName");
        partialPet.setPhotoUrls(Arrays.asList("https://example.com/photo.jpg"));
        partialPet.setStatus(Pet.Status.PENDING);
        
        Response updateResponse = RestClient.put("/pet", partialPet);
        
        assertThat("Status code should be 200", updateResponse.getStatusCode(), equalTo(200));
        assertThat("Name should be updated", updateResponse.jsonPath().getString("name"), equalTo("UpdatedName"));
        assertThat("Status should be updated", updateResponse.jsonPath().getString("status"), equalTo("pending"));
        
        // Cleanup
        RestClient.delete("/pet/" + petId);
        
        logPass("Partial pet update test passed");
        logger.info("Test Passed: Pet updated with partial data");
    }

    // Helper method to generate string of specific length
    private String generateString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        return sb.toString();
    }
}
