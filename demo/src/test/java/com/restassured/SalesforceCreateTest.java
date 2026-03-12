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
                "Name": "Teju Solutions",
                "Phone": "+31691234568",
                "Industry": "Media",
                "BillingCity": "Den Haag",
                "BillingState": "NH",
                "BillingPostalCode": "1012AB",
                "BillingCountry": "Netherlands",
                "Type": "Customer - Direct"
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