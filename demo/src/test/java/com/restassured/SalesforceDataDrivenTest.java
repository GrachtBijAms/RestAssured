package com.restassured;


import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.restassured.base.SalesforceBaseTest;
import com.restassured.utils.CsvDataReader;
import com.restassured.utils.ExcelDataReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class SalesforceDataDrivenTest extends SalesforceBaseTest {

    // =========================================================
    // 📊 DATA PROVIDERS
    // =========================================================

    /**
     * Reads account data from Excel file.
     * Each row becomes one test iteration.
     */
    @DataProvider(name = "accountDataFromExcel")
    public Object[][] getAccountDataFromExcel() {
        return ExcelDataReader.readExcelAsDataProvider(
            "src/test/resources/data/salesforce_accounts.xlsx",
            "Accounts"  // sheet name
        );
    }

    /**
     * Reads account data from CSV file.
     */
    @DataProvider(name = "accountDataFromCsv")
    public Object[][] getAccountDataFromCsv() {
        return CsvDataReader.readCsvAsDataProvider(
            "src/test/resources/data/salesforce_accounts.csv"
        );
    }

    // =========================================================
    // 🧪 TESTS — Excel Driven
    // =========================================================

    @Test(dataProvider = "accountDataFromExcel")
    public void createAccountFromExcel(Map<String, String> accountData) {
        String name = accountData.get("Name");
        System.out.println("📊 Creating Account from Excel: " + name);

        // Build request body from Excel row data
        String requestBody = String.format("""
            {
                "Name": "%s",
                "Phone": "%s",
                "Industry": "%s",
                "BillingCity": "%s",
                "BillingState": "%s",
                "BillingPostalCode": "%s",
                "BillingCountry": "%s",
                "Type": "%s"
            }
            """,
            name,
            accountData.get("Phone"),
            accountData.get("Industry"),
            accountData.get("BillingCity"),
            accountData.get("BillingState"),
            accountData.get("BillingPostalCode"),
            accountData.get("BillingCountry"),
            accountData.get("Type")
        );

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(sobjectUrl("Account"))
        .then()
            .statusCode(201)
            .extract().response();

        String createdId = response.jsonPath().getString("id");
        Assert.assertNotNull(createdId, "Account ID should not be null");
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        System.out.println("   ✅ Created: " + name + " → ID: " + createdId);
    }

    // =========================================================
    // 🧪 TESTS — CSV Driven
    // =========================================================

    @Test(dataProvider = "accountDataFromCsv")
    public void createAccountFromCsv(Map<String, String> accountData) {
        String name = accountData.get("Name");
        System.out.println("📄 Creating Account from CSV: " + name);

        String requestBody = String.format("""
            {
                "Name": "%s",
                "Phone": "%s",
                "Industry": "%s",
                "BillingCity": "%s",
                "BillingState": "%s",
                "BillingPostalCode": "%s",
                "BillingCountry": "%s",
                "Type": "%s"
            }
            """,
            name,
            accountData.get("Phone"),
            accountData.get("Industry"),
            accountData.get("BillingCity"),
            accountData.get("BillingState"),
            accountData.get("BillingPostalCode"),
            accountData.get("BillingCountry"),
            accountData.get("Type")
        );

        Response response = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(sobjectUrl("Account"))
        .then()
            .statusCode(201)
            .extract().response();

        String createdId = response.jsonPath().getString("id");
        Assert.assertNotNull(createdId);
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        System.out.println("   ✅ Created: " + name + " → ID: " + createdId);
    }

    // =========================================================
    // 🧪 BULK CREATE + VERIFY
    // =========================================================

    @Test
    public void bulkCreateAndVerifyAccounts() {
        System.out.println("📊 Bulk creating accounts from Excel...");

        List<Map<String, String>> allAccounts = ExcelDataReader.readExcel(
            "src/test/resources/data/salesforce_accounts.xlsx",
            "Accounts"
        );

        List<String> createdIds = new ArrayList<>();

        // Create all accounts
        for (Map<String, String> account : allAccounts) {
            String requestBody = String.format("""
                {
                    "Name": "%s",
                    "Phone": "%s",
                    "Industry": "%s",
                    "BillingCity": "%s",
                    "BillingCountry": "%s"
                }
                """,
                account.get("Name"),
                account.get("Phone"),
                account.get("Industry"),
                account.get("BillingCity"),
                account.get("BillingCountry")
            );

            Response response = given()
                .spec(requestSpec)
                .body(requestBody)
            .when()
                .post(sobjectUrl("Account"))
            .then()
                .statusCode(201)
                .extract().response();

            String id = response.jsonPath().getString("id");
            createdIds.add(id);
            System.out.println("   ✅ " + account.get("Name") + " → " + id);
        }

        // Verify all were created
        System.out.println("\n🔍 Verifying all " + createdIds.size() + " accounts...");
        for (String id : createdIds) {
            given()
                .spec(requestSpec)
            .when()
                .get(recordUrl("Account", id))
            .then()
                .statusCode(200);
        }

        System.out.println("✅ All " + createdIds.size() + " accounts created and verified!");
        Assert.assertEquals(createdIds.size(), allAccounts.size());
    }
}
