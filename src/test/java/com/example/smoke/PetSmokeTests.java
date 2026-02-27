package com.example.smoke;

import com.example.base.BaseTest;
import com.example.pojo.Pet;
import com.example.utils.PayloadHelper;
import com.example.utils.RestClient;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PetSmokeTests extends BaseTest {

    private static Long createdPetId;

    @Test(priority = 1, groups = "smoke", description = "Find pets by status - available")
    public void testFindPetsByStatus() {
        logger.info("Test: Find pets by status = available");
        
        Response response = RestClient.get("/pet/findByStatus?status=available");
        
        // Verify status code
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        
        // Verify response is non-empty list
        assertThat("Response should be a list", response.jsonPath().getList("$"), is(notNullValue()));
        assertThat("Response list should not be empty", response.jsonPath().getList("$").size(), greaterThan(0));
        
        // Verify first pet has required fields
        assertThat("First pet should have an id", response.jsonPath().get("[0].id"), is(notNullValue()));
        assertThat("First pet should have a name", response.jsonPath().get("[0].name"), is(notNullValue()));
        
        logPass("Successfully found pets with status 'available'");
        logger.info("Test Passed: Found {} pets with status 'available'", response.jsonPath().getList("$").size());
    }

    @Test(priority = 2, groups = "smoke", description = "Create a new pet")
    public void testCreatePet() {
        logger.info("Test: Create a new pet");
        
        // Create pet payload
        Pet newPet = PayloadHelper.createNewPet();
        
        // Send POST request
        Response response = RestClient.post("/pet", newPet);
        
        // Verify status code
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        
        // Extract and verify pet ID
        createdPetId = response.jsonPath().getLong("id");
        assertThat("Created pet should have an id", createdPetId, is(notNullValue()));
        assertThat("Created pet id should be greater than 0", createdPetId, greaterThan(0L));
        
        // Verify pet details
        assertThat("Pet name should match", response.jsonPath().getString("name"), equalTo(newPet.getName()));
        assertThat("Pet status should match", response.jsonPath().getString("status"), equalTo(newPet.getStatus().toString()));
        
        logPass("Successfully created pet with ID: " + createdPetId);
        logger.info("Test Passed: Created pet with ID: {}", createdPetId);
    }

    @Test(priority = 3, groups = "smoke", description = "Get pet by ID", dependsOnMethods = "testCreatePet")
    public void testGetPetById() {
        logger.info("Test: Get pet by ID: {}", createdPetId);
        
        // Send GET request
        Response response = RestClient.get("/pet/" + createdPetId);
        
        // Verify status code
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        
        // Verify pet details
        assertThat("Pet ID should match", response.jsonPath().getLong("id"), equalTo(createdPetId));
        assertThat("Pet should have a name", response.jsonPath().getString("name"), is(notNullValue()));
        assertThat("Pet should have photoUrls", response.jsonPath().getList("photoUrls"), is(notNullValue()));
        
        // Deserialize to Pet object
        Pet retrievedPet = RestClient.asPojo(response, Pet.class);
        assertThat("Retrieved pet ID should match", retrievedPet.getId(), equalTo(createdPetId));
        assertThat("Retrieved pet name should not be null", retrievedPet.getName(), is(notNullValue()));
        
        logPass("Successfully retrieved pet with ID: " + createdPetId);
        logger.info("Test Passed: Retrieved pet - Name: {}, Status: {}", retrievedPet.getName(), retrievedPet.getStatus());
    }

    @Test(priority = 4, groups = "smoke", description = "Update pet status", dependsOnMethods = "testGetPetById")
    public void testUpdatePet() {
        logger.info("Test: Update pet status for ID: {}", createdPetId);
        
        // Get current pet
        Response getResponse = RestClient.get("/pet/" + createdPetId);
        Pet existingPet = RestClient.asPojo(getResponse, Pet.class);
        
        // Update pet status
        Pet updatedPet = PayloadHelper.updatePetStatus(existingPet, Pet.Status.SOLD);
        
        // Send PUT request
        Response response = RestClient.put("/pet", updatedPet);
        
        // Verify status code
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        
        // Verify updated status
        assertThat("Pet ID should match", response.jsonPath().getLong("id"), equalTo(createdPetId));
        assertThat("Pet status should be updated to 'sold'", response.jsonPath().getString("status"), equalTo("sold"));
        
        // Verify the update by fetching the pet again
        Response verifyResponse = RestClient.get("/pet/" + createdPetId);
        assertThat("Verified status should be 'sold'", verifyResponse.jsonPath().getString("status"), equalTo("sold"));
        
        logPass("Successfully updated pet status to 'sold' for ID: " + createdPetId);
        logger.info("Test Passed: Updated pet status to 'sold' for ID: {}", createdPetId);
    }

    @Test(priority = 5, groups = "smoke", description = "Delete pet by ID", dependsOnMethods = "testUpdatePet")
    public void testDeletePet() {
        logger.info("Test: Delete pet with ID: {}", createdPetId);
        
        // Send DELETE request
        Response response = RestClient.delete("/pet/" + createdPetId);
        
        // Verify status code
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        
        // Verify response message
        assertThat("Response should contain a message", response.jsonPath().getString("message"), is(notNullValue()));
        
        // Verify pet is deleted by trying to fetch it
        Response verifyResponse = RestClient.get("/pet/" + createdPetId);
        assertThat("Deleted pet should return 404", verifyResponse.getStatusCode(), equalTo(404));
        
        logPass("Successfully deleted pet with ID: " + createdPetId);
        logger.info("Test Passed: Deleted pet with ID: {}", createdPetId);
    }
}
