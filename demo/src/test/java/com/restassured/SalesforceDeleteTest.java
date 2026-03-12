package com.restassured;


import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.restassured.base.SalesforceBaseTest;

import static io.restassured.RestAssured.given;

public class SalesforceDeleteTest extends SalesforceBaseTest {

    // This account will be created fresh and then deleted in the tests below
    private static String accountIdToDelete;

    @Test(priority = 1)
    public void createAccountForDeletion() {
        System.out.println("➕ Creating a fresh Account to delete...");

        String requestBody = """
            {
                "Name": "Account To Delete",
                "Phone": "+31201111111",
                "Industry": "Technology",
                "BillingCity": "Rotterdam"
            }
        """;

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(sobjectUrl("Account"))
        .then()
            .statusCode(201)
            .extract().response();

        accountIdToDelete = response.jsonPath().getString("id");
        Assert.assertNotNull(accountIdToDelete, "Account ID should not be null");
        System.out.println("✅ Created Account ID: " + accountIdToDelete);
    }

    @Test(priority = 2, dependsOnMethods = "createAccountForDeletion")
    public void deleteAccount() {
        System.out.println("🗑️ Deleting Account: " + accountIdToDelete);

        given()
            .spec(requestSpec)
        .when()
            .delete(recordUrl("Account", accountIdToDelete))
        .then()
            .statusCode(204); // 204 No Content = deleted successfully

        System.out.println("✅ Account deleted successfully!");
    }

    @Test(priority = 3, dependsOnMethods = "deleteAccount")
    public void verifyAccountDeleted() {
        System.out.println("🔍 Verifying Account is deleted: " + accountIdToDelete);

        // Reading a deleted record should return 404
        Response response = given()
            .spec(requestSpec)
        .when()
            .get(recordUrl("Account", accountIdToDelete))
        .then()
            .statusCode(404) // 404 = record no longer exists ✅
            .extract().response();

        System.out.println("✅ Confirmed — Account no longer exists (404)");
        System.out.println("   Body: " + response.getBody().asString());
    }

    @Test
    public void deleteNonExistentAccount() {
        System.out.println("🗑️ Testing delete with invalid Account ID...");

        Response response = given()
            .spec(requestSpec)
        .when()
            .delete(recordUrl("Account", "INVALID_ID_12345"))
        .then()
            .statusCode(404) // 400/404 for malformed ID
            .extract().response();

        System.out.println("✅ Correctly returned 404 for invalid ID");
        System.out.println("   Body: " + response.getBody().asString());
    }
}