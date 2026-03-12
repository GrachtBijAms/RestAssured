package com.restassured;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.restassured.base.SalesforceBaseTest;

import static io.restassured.RestAssured.given;

public class SalesforceUpdateTest extends SalesforceBaseTest {

    // Use the latest created Account ID
    private static final String ACCOUNT_ID = "001g500000EQnoDAAT";

    @Test
    public void updateAccountPhone() {
        System.out.println("✏️ Updating phone number for Account: " + ACCOUNT_ID);

        String requestBody = """
            {
                "Phone": "+31209999999"
            }
        """;

        given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .patch(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(204); // Salesforce returns 204 No Content on success

        System.out.println("✅ Phone updated successfully!");

        // Verify the update by reading the record back
        Response response = given()
            .spec(requestSpec)
            .queryParam("fields", "Id,Name,Phone")
        .when()
            .get(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(200)
            .extract().response();

        String updatedPhone = response.jsonPath().getString("Phone");
        System.out.println("   Updated Phone: " + updatedPhone);
        Assert.assertEquals(updatedPhone, "+31209999999", "Phone should be updated");
        System.out.println("✅ Phone update verified!");
    }

    @Test
    public void updateAccountMultipleFields() {
        System.out.println("✏️ Updating multiple fields for Account: " + ACCOUNT_ID);

        String requestBody = """
            {
                "BillingCity": "Amsterdam",
                "Industry": "Technology",
                "Description": "Updated by Rest Assured automation test"
            }
        """;

        given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .patch(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(204);

        System.out.println("✅ Multiple fields updated successfully!");

        // Verify the updates
        Response response = given()
            .spec(requestSpec)
            .queryParam("fields", "Id,Name,BillingCity,Industry,Description")
        .when()
            .get(recordUrl("Account", ACCOUNT_ID))
        .then()
            .statusCode(200)
            .extract().response();

        System.out.println("   BillingCity : " + response.jsonPath().getString("BillingCity"));
        System.out.println("   Industry    : " + response.jsonPath().getString("Industry"));
        System.out.println("   Description : " + response.jsonPath().getString("Description"));

        Assert.assertEquals(response.jsonPath().getString("BillingCity"), "Amsterdam");
        Assert.assertEquals(response.jsonPath().getString("Industry"),    "Technology");
        System.out.println("✅ All update assertions passed!");
    }

    @Test
    public void updateNonExistentAccount() {
        System.out.println("✏️ Testing update with invalid Account ID...");

        String requestBody = """
            {
                "Phone": "+31200000000"
            }
        """;

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .patch(recordUrl("Account", "INVALID_ID_12345"))
        .then()
            .statusCode(404) // Salesforce returns 400 for malformed IDs
            .extract().response();

        System.out.println("✅ Correctly returned 404 for invalid ID");
        System.out.println("   Body: " + response.getBody().asString());
    }
}