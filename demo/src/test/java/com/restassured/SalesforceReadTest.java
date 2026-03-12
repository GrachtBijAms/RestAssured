package com.restassured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import com.restassured.base.SalesforceBaseTest;

import io.restassured.response.Response;

public class SalesforceReadTest extends SalesforceBaseTest {

    private static String accountId = "001g500000EQdfpAAD"; // Access accountId from create test

    @Test
    public void readAccount() {
        // This test depends on the account created in SalesforceCreateTest
        if (accountId == null) {
            throw new RuntimeException("❌ Account ID is null. Ensure createAccount() runs first.");
        }

        Response res = given()
            .spec(requestSpec)
        .when()
            .get("/services/data/" + apiVersion + "/sobjects/Account/" + accountId)
        .then()
            .statusCode(200)
            .body("Id", equalTo(accountId))
            .extract().response();

        System.out.println("Account Name: " + res.jsonPath().getString("Name"));
    }   


        @Test
    public void readMalformedAccount() {
        // This test depends on the account created in SalesforceCreateTest
        if (accountId == null) {
            throw new RuntimeException("❌ Account ID is null. Ensure createAccount() runs first.");
        }

        Response res = given()
            .spec(requestSpec)
        .when()
            .get("/services/data/" + apiVersion + "/sobjects/Account/" + "001g500000EQdfpAAA") // Invalid ID
        .then()
            .statusCode(400)
            .extract().response();

        System.out.println("Account Name: " + res.jsonPath().getString("Name"));
    }   

            @Test
    public void readInvalidAccount() {
        // This test depends on the account created in SalesforceCreateTest
        if (accountId == null) {
            throw new RuntimeException("❌ Account ID is null. Ensure createAccount() runs first.");
        }

        Response res = given()
            .spec(requestSpec)
        .when()
            .get("/services/data/" + apiVersion + "/sobjects/Account/" + "001g50dfpAAA") // Invalid ID
        .then()
            .statusCode(404)
            .extract().response();

        System.out.println("Account Name: " + res.jsonPath().getString("Name"));
    }   
    
}
