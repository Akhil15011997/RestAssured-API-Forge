error id: file://<WORKSPACE>/src/test/java/com/example/utils/RestClient.java:_empty_/BaseTest#requestSpec#
file://<WORKSPACE>/src/test/java/com/example/utils/RestClient.java
empty definition using pc, found symbol in pc: _empty_/BaseTest#requestSpec#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 8418
uri: file://<WORKSPACE>/src/test/java/com/example/utils/RestClient.java
text:
```scala
package com.example.utils;

import com.example.base.BaseTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

public class RestClient {

    private static final Logger logger = LogManager.getLogger(RestClient.class);

    /**
     * Perform GET request
     *
     * @param path API endpoint path
     * @return Response object
     */
    public static Response get(String path) {
        logger.info("========================================");
        logger.info("GET Request to: {}", path);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .when()
                .get(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform GET request with query parameters
     *
     * @param path        API endpoint path
     * @param queryParams Query parameters
     * @return Response object
     */
    public static Response get(String path, Object queryParams) {
        logger.info("========================================");
        logger.info("GET Request to: {}", path);
        logger.info("Query Parameters: {}", queryParams);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .queryParams(queryParams.toString())
                .when()
                .get(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform POST request
     *
     * @param path API endpoint path
     * @param body Request body object
     * @return Response object
     */
    public static Response post(String path, Object body) {
        logger.info("========================================");
        logger.info("POST Request to: {}", path);
        logger.info("Request Body: {}", body);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .body(body)
                .when()
                .post(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform POST request without body
     *
     * @param path API endpoint path
     * @return Response object
     */
    public static Response post(String path) {
        logger.info("========================================");
        logger.info("POST Request to: {}", path);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .when()
                .post(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform PUT request
     *
     * @param path API endpoint path
     * @param body Request body object
     * @return Response object
     */
    public static Response put(String path, Object body) {
        logger.info("========================================");
        logger.info("PUT Request to: {}", path);
        logger.info("Request Body: {}", body);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .body(body)
                .when()
                .put(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform PUT request without body
     *
     * @param path API endpoint path
     * @return Response object
     */
    public static Response put(String path) {
        logger.info("========================================");
        logger.info("PUT Request to: {}", path);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .when()
                .put(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform DELETE request
     *
     * @param path API endpoint path
     * @return Response object
     */
    public static Response delete(String path) {
        logger.info("========================================");
        logger.info("DELETE Request to: {}", path);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .when()
                .delete(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform PATCH request
     *
     * @param path API endpoint path
     * @param body Request body object
     * @return Response object
     */
    public static Response patch(String path, Object body) {
        logger.info("========================================");
        logger.info("PATCH Request to: {}", path);
        logger.info("Request Body: {}", body);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .body(body)
                .when()
                .patch(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform PATCH request without body
     *
     * @param path API endpoint path
     * @return Response object
     */
    public static Response patch(String path) {
        logger.info("========================================");
        logger.info("PATCH Request to: {}", path);
        logger.info("========================================");

        Response response = given()
                .spec(BaseTest.requestSpec)
                .when()
                .patch(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Extract response as String
     *
     * @param response Response object
     * @return Response body as String
     */
    public static String asString(Response response) {
        String responseBody = response.asString();
        logger.debug("Response extracted as String: {}", responseBody);
        return responseBody;
    }

    /**
     * Extract response as POJO
     *
     * @param response Response object
     * @param clazz    Class type to deserialize
     * @param <T>      Generic type
     * @return Response body as POJO
     */
    public static <T> T asPojo(Response response, Class<T> clazz) {
        T pojo = response.as(clazz);
        logger.debug("Response extracted as POJO: {}", pojo);
        return pojo;
    }

    /**
     * Log response details
     *
     * @param response Response object
     */
    private static void logResponse(Response response) {
        logger.info("----------------------------------------");
        logger.info("Response Status Code: {}", response.getStatusCode());
        logger.info("Response Status Line: {}", response.getStatusLine());
        logger.info("Response Time: {} ms", response.getTime());
        logger.info("Response Headers: {}", response.getHeaders());
        
        String responseBody = response.asString();
        if (responseBody != null && !responseBody.isEmpty()) {
            logger.info("Response Body: {}", responseBody);
        } else {
            logger.info("Response Body: [Empty]");
        }
        logger.info("========================================");
    }

    /**
     * Get custom request specification
     *
     * @return RequestSpecification
     */
    public static RequestSpecification getRequestSpec() {
        return given().spec(BaseTest.re@@questSpec);
    }

    /**
     * Perform GET request with custom specification
     *
     * @param path    API endpoint path
     * @param reqSpec Custom RequestSpecification
     * @return Response object
     */
    public static Response get(String path, RequestSpecification reqSpec) {
        logger.info("========================================");
        logger.info("GET Request (Custom Spec) to: {}", path);
        logger.info("========================================");

        Response response = given()
                .spec(reqSpec)
                .when()
                .get(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }

    /**
     * Perform POST request with custom specification
     *
     * @param path    API endpoint path
     * @param body    Request body object
     * @param reqSpec Custom RequestSpecification
     * @return Response object
     */
    public static Response post(String path, Object body, RequestSpecification reqSpec) {
        logger.info("========================================");
        logger.info("POST Request (Custom Spec) to: {}", path);
        logger.info("Request Body: {}", body);
        logger.info("========================================");

        Response response = given()
                .spec(reqSpec)
                .body(body)
                .when()
                .post(path)
                .then()
                .extract()
                .response();

        logResponse(response);
        return response;
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/BaseTest#requestSpec#