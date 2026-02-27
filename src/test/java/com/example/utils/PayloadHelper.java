package com.example.utils;

import com.example.pojo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PayloadHelper {

    private static final Logger logger = LogManager.getLogger(PayloadHelper.class);
    private static final Random random = new Random();

    /**
     * Create a new Pet payload with default values
     *
     * @return Pet object
     */
    public static Pet createNewPet() {
        long petId = generateRandomId();
        
        Category category = new Category(1L, "Dogs");
        
        List<String> photoUrls = Arrays.asList(
                "https://example.com/photos/pet1.jpg",
                "https://example.com/photos/pet2.jpg"
        );
        
        List<Tag> tags = Arrays.asList(
                new Tag(1L, "friendly"),
                new Tag(2L, "trained")
        );
        
        Pet pet = new Pet(
                petId,
                category,
                "Buddy",
                photoUrls,
                tags,
                Pet.Status.AVAILABLE
        );
        
        logger.info("Created new Pet payload: {}", pet);
        return pet;
    }

    /**
     * Create a new Pet payload with custom values
     *
     * @param petId      Pet ID
     * @param petName    Pet name
     * @param status     Pet status
     * @param categoryId Category ID
     * @param categoryName Category name
     * @return Pet object
     */
    public static Pet createPet(Long petId, String petName, Pet.Status status, 
                                Long categoryId, String categoryName) {
        Category category = new Category(categoryId, categoryName);
        
        List<String> photoUrls = Arrays.asList(
                "https://example.com/photos/" + petName.toLowerCase() + ".jpg"
        );
        
        List<Tag> tags = new ArrayList<>();
        
        Pet pet = new Pet(petId, category, petName, photoUrls, tags, status);
        
        logger.info("Created custom Pet payload: {}", pet);
        return pet;
    }

    /**
     * Create a minimal Pet payload (only required fields)
     *
     * @param petName Pet name
     * @return Pet object
     */
    public static Pet createMinimalPet(String petName) {
        List<String> photoUrls = Arrays.asList("https://example.com/photo.jpg");
        
        Pet pet = new Pet(
                null,
                null,
                petName,
                photoUrls,
                null,
                Pet.Status.AVAILABLE
        );
        
        logger.info("Created minimal Pet payload: {}", pet);
        return pet;
    }

    /**
     * Update Pet status
     *
     * @param pet    Existing Pet object
     * @param status New status
     * @return Updated Pet object
     */
    public static Pet updatePetStatus(Pet pet, Pet.Status status) {
        pet.setStatus(status);
        logger.info("Updated Pet status to: {}", status);
        return pet;
    }

    /**
     * Create a new User payload with default values
     *
     * @return User object
     */
    public static User createNewUser() {
        long userId = generateRandomId();
        String username = "user" + userId;
        
        User user = new User(
                userId,
                username,
                "John",
                "Doe",
                username + "@example.com",
                "password123",
                "555-1234-" + userId,
                1
        );
        
        logger.info("Created new User payload: {}", user);
        return user;
    }

    /**
     * Create a new User payload with custom values
     *
     * @param username  Username
     * @param firstName First name
     * @param lastName  Last name
     * @param email     Email address
     * @param password  Password
     * @param phone     Phone number
     * @return User object
     */
    public static User createUser(String username, String firstName, String lastName,
                                  String email, String password, String phone) {
        long userId = generateRandomId();
        
        User user = new User(
                userId,
                username,
                firstName,
                lastName,
                email,
                password,
                phone,
                1
        );
        
        logger.info("Created custom User payload: {}", user);
        return user;
    }

    /**
     * Create a minimal User payload (only username)
     *
     * @param username Username
     * @return User object
     */
    public static User createMinimalUser(String username) {
        User user = new User(
                null,
                username,
                null,
                null,
                null,
                null,
                null,
                null
        );
        
        logger.info("Created minimal User payload: {}", user);
        return user;
    }

    /**
     * Create an array of User payloads
     *
     * @param count Number of users to create
     * @return List of User objects
     */
    public static List<User> createUserArray(int count) {
        List<User> users = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            long userId = generateRandomId();
            String username = "user" + userId;
            
            User user = new User(
                    userId,
                    username,
                    "FirstName" + i,
                    "LastName" + i,
                    username + "@example.com",
                    "password" + i,
                    "555-" + i + "-" + userId,
                    1
            );
            
            users.add(user);
        }
        
        logger.info("Created {} User payloads", count);
        return users;
    }

    /**
     * Create a Category payload
     *
     * @param id   Category ID
     * @param name Category name
     * @return Category object
     */
    public static Category createCategory(Long id, String name) {
        Category category = new Category(id, name);
        logger.info("Created Category payload: {}", category);
        return category;
    }

    /**
     * Create a Tag payload
     *
     * @param id   Tag ID
     * @param name Tag name
     * @return Tag object
     */
    public static Tag createTag(Long id, String name) {
        Tag tag = new Tag(id, name);
        logger.info("Created Tag payload: {}", tag);
        return tag;
    }

    /**
     * Create multiple Tags
     *
     * @param tagNames Array of tag names
     * @return List of Tag objects
     */
    public static List<Tag> createTags(String... tagNames) {
        List<Tag> tags = new ArrayList<>();
        long tagId = 1L;
        
        for (String tagName : tagNames) {
            tags.add(new Tag(tagId++, tagName));
        }
        
        logger.info("Created {} Tag payloads", tags.size());
        return tags;
    }

    /**
     * Create an ApiResponse payload
     *
     * @param code    Response code
     * @param type    Response type
     * @param message Response message
     * @return ApiResponse object
     */
    public static ApiResponse createApiResponse(Integer code, String type, String message) {
        ApiResponse apiResponse = new ApiResponse(code, type, message);
        logger.info("Created ApiResponse payload: {}", apiResponse);
        return apiResponse;
    }

    /**
     * Generate a random ID
     *
     * @return Random long ID
     */
    private static long generateRandomId() {
        return 10000L + random.nextInt(90000);
    }

    /**
     * Generate random pet name
     *
     * @return Random pet name
     */
    public static String generateRandomPetName() {
        String[] petNames = {
                "Buddy", "Max", "Charlie", "Rocky", "Cooper",
                "Bella", "Luna", "Daisy", "Lucy", "Sadie",
                "Bailey", "Molly", "Maggie", "Sophie", "Chloe"
        };
        return petNames[random.nextInt(petNames.length)];
    }

    /**
     * Generate random username
     *
     * @return Random username
     */
    public static String generateRandomUsername() {
        return "testuser" + generateRandomId();
    }

    /**
     * Generate random email
     *
     * @param username Username for email
     * @return Random email address
     */
    public static String generateRandomEmail(String username) {
        String[] domains = {"example.com", "test.com", "demo.com", "mail.com"};
        return username + "@" + domains[random.nextInt(domains.length)];
    }
}
