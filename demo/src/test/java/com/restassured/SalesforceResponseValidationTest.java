package com.restassured;

import com.restassured.base.SalesforceBaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SalesforceResponseValidationTest extends SalesforceBaseTest {

    private static final String ACCOUNT_ID = "001g500000ERlfVAAT";

    // =========================================================
    // 📋 1. HEADER VALIDATION
    // =========================================================

    @Test
    public void validateResponseHeaders() {
        System.out.println("🔍 Validating response headers...");

        given()
            .spec(requestSpec)
        .when()
            .get(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(200)
            // Content type must be JSON
            .header("Content-Type", containsString("application/json"))
            // Must have a Sforce-Limit-Info header (Salesforce API limit tracking)
            .header("Sforce-Limit-Info",  notNullValue())
            // Must not expose server internals
            .header("X-Powered-By",       nullValue())
            // Must have cache control
            .header("Cache-Control",      notNullValue());

        System.out.println("✅ Header validation passed!");
    }

    @Test
    public void validateCreateResponseHeaders() {
        System.out.println("🔍 Validating POST response headers...");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("Name",    "Header Validation Test Account");
        requestBody.put("Phone",   "+31201111111");
        requestBody.put("Industry","Technology");

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(sobjectUrl("Account"))
        .then()
            .statusCode(201)
            .header("Content-Type", containsString("application/json"))
            // Location header should point to newly created record
            .header("Location",     notNullValue())
            .extract().response();

        // Verify Location header contains the new record ID
        String location  = response.getHeader("Location");
        String createdId = response.jsonPath().getString("id");

        Assert.assertTrue(location.contains(createdId),
            "Location header should contain new record ID");

        System.out.println("✅ POST headers validated!");
        System.out.println("   Location : " + location);
        System.out.println("   Record ID: " + createdId);

        // Cleanup
        given().spec(requestSpec)
            .delete(recordUrl("Account", createdId));
    }

    // =========================================================
    // ⏱️ 2. RESPONSE TIME VALIDATION
    // =========================================================

    @Test
    public void validateResponseTime() {
        System.out.println("⏱️ Validating response time...");

        given()
            .spec(requestSpec)
        .when()
            .get(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(200)
            // Response must come back within 3 seconds
            .time(lessThan(3000L));

        System.out.println("✅ Response time within 3000ms!");
    }

    @Test
    public void validatePostResponseTime() {
        System.out.println("⏱️ Validating POST response time...");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("Name",    "Response Time Test Account");
        requestBody.put("Industry","Technology");

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(sobjectUrl("Account"))
        .then()
            .statusCode(201)
            // POST should complete within 5 seconds
            .time(lessThan(5000L))
            .extract().response();

        String createdId = response.jsonPath().getString("id");
        System.out.println("✅ POST completed within 5000ms!");

        // Cleanup
        given().spec(requestSpec)
            .delete(recordUrl("Account", createdId));
    }

    // =========================================================
    // ✅ 3. RESPONSE BODY VALIDATION
    // =========================================================

    @Test
    public void validateReadResponseBody() {
        System.out.println("📋 Validating GET response body...");

        given()
            .spec(requestSpec)
            .queryParam("fields",
                "Id,Name,Phone,Industry,BillingCity,BillingCountry")
        .when()
            .get(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(200)
            // Field presence
            .body("Id",             notNullValue())
            .body("Name",           notNullValue())
            // Field value assertions
            .body("Name",           equalTo("Cheekoo Solutions"))
            .body("BillingCity",    equalTo("Den Haag"))
            .body("Industry",       equalTo("Media"))
            // Field type assertions
            .body("Id",             instanceOf(String.class))
            .body("Name",           instanceOf(String.class));

        System.out.println("✅ Response body validation passed!");
    }

    @Test
    public void validateCreateResponseBody() {
        System.out.println("📋 Validating POST response body...");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("Name",    "Body Validation Test");
        requestBody.put("Industry","Finance");

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(sobjectUrl("Account"))
        .then()
            .statusCode(201)
            // Must have these fields
            .body("id",      notNullValue())
            .body("success", equalTo(true))
            .body("errors",  empty())
            // ID must be 18 chars (Salesforce standard)
            .body("id",      hasLength(18))
            .extract().response();

        String createdId = response.jsonPath().getString("id");
        System.out.println("✅ POST body validation passed!");
        System.out.println("   ID length: " + createdId.length() + " chars ✅");

        // Cleanup
        given().spec(requestSpec)
            .delete(recordUrl("Account", createdId));
    }

    // =========================================================
    // 📊 4. SALESFORCE API LIMIT VALIDATION
    // =========================================================

    @Test
    public void validateApiLimitHeaders() {
        System.out.println("📊 Checking Salesforce API usage limits...");

        Response response = given()
            .spec(requestSpec)
        .when()
            .get(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(200)
            .extract().response();

        // Salesforce returns API usage in header
        // Format: "api-usage=15/5000000"
        String limitInfo = response.getHeader("Sforce-Limit-Info");
        System.out.println("   API Limit Info: " + limitInfo);

        if (limitInfo != null) {
            String[] parts    = limitInfo.replace("api-usage=", "").split("/");
            int used          = Integer.parseInt(parts[0].trim());
            int total         = Integer.parseInt(parts[1].trim());
            double usedPercent = ((double) used / total) * 100;

            System.out.println("   Used  : " + used);
            System.out.println("   Total : " + total);
            System.out.printf ("   Usage : %.2f%%%n", usedPercent);

            // Warn if over 80% of API limit used
            Assert.assertTrue(usedPercent < 80,
                "⚠️ API usage over 80%! Used: " + used + "/" + total);

            System.out.println("✅ API limit check passed!");
        }
    }

    // =========================================================
    // 🔄 5. FULL END-TO-END RESPONSE VALIDATION
    // =========================================================

    @Test
    public void validateFullCRUDResponseCycle() {
        System.out.println("🔄 Full CRUD response validation cycle...");

        // ── CREATE ──
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("Name",        "Full Validation Account");
        requestBody.put("Phone",       "+31209876543");
        requestBody.put("Industry",    "Technology");
        requestBody.put("BillingCity", "Amsterdam");

        Response createResponse = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(sobjectUrl("Account"))
        .then()
            .statusCode(201)
            .time(lessThan(5000L))
            .header("Content-Type", containsString("application/json"))
            .body("success",         equalTo(true))
            .body("id",              notNullValue())
            .body("id",              hasLength(18))
            .extract().response();

        String newId = createResponse.jsonPath().getString("id");
        System.out.println("   ✅ CREATE validated → ID: " + newId);

        // ── READ ──
        given()
            .spec(requestSpec)
            .queryParam("fields", "Id,Name,Phone,Industry,BillingCity")
        .when()
            .get(recordUrl("Account", newId))
        .then()
            .statusCode(200)
            .time(lessThan(3000L))
            .header("Content-Type", containsString("application/json"))
            .body("Id",             equalTo(newId))
            .body("Name",           equalTo("Full Validation Account"))
            .body("BillingCity",    equalTo("Amsterdam"));

        System.out.println("   ✅ READ validated");

        // ── UPDATE ──
        given()
            .spec(requestSpec)
            .body(Map.of("BillingCity", "Rotterdam"))
        .when()
            .patch(recordUrl("Account", newId))
        .then()
            .statusCode(204)
            .time(lessThan(3000L));

        System.out.println("   ✅ UPDATE validated");

        // Verify update
        given()
            .spec(requestSpec)
            .queryParam("fields", "BillingCity")
        .when()
            .get(recordUrl("Account", newId))
        .then()
            .statusCode(200)
            .body("BillingCity", equalTo("Rotterdam"));

        System.out.println("   ✅ UPDATE verified");

        // ── DELETE ──
        given()
            .spec(requestSpec)
        .when()
            .delete(recordUrl("Account", newId))
        .then()
            .statusCode(204)
            .time(lessThan(3000L));

        System.out.println("   ✅ DELETE validated");

        // Verify deletion
        given()
            .spec(requestSpec)
        .when()
            .get(recordUrl("Account", newId))
        .then()
            .statusCode(404);

        System.out.println("   ✅ DELETE verified (404 confirmed)");
        System.out.println("✅ Full CRUD response validation cycle complete!");
    }
}
