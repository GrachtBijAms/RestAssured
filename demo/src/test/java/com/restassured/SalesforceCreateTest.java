package com.restassured;

import org.testng.annotations.Test;

import com.restassured.base.SalesforceBaseTest;

import io.restassured.response.Response;
import static io.restassured.RestAssured.*;

public class SalesforceCreateTest extends SalesforceBaseTest {

    private static String accountId;

    @Test
    public void createAccount() {
        String requestBody = """
            {
                "Name": "NekstIT Solutions",
                "Phone": "+31201234567",
                "Industry": "Technology",
                "BillingCity": "Amstelveen"
            }
        """;

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post("/services/data/" + apiVersion + "/sobjects/Account")
        .then()
            .statusCode(201)
            .extract().response();

        accountId = response.jsonPath().getString("id");
        System.out.println("Created Account ID: " + accountId);
    }
}