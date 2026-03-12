package com.restassured.utils;

import com.restassured.base.SalesforceBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import com.restassured.utils.ExcelDataReader;
import com.restassured.utils.CsvDataReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class SalesforceCleanupTest extends SalesforceBaseTest {

    // =========================================================
    // 🗑️ OPTION 1 — Delete by hardcoded IDs
    // =========================================================

    @Test
    public void deleteAccountsByIds() {
        List<String> accountIds = List.of(
            //"001g500000ERlfVAAT",  // NekstIT Solutions
            //"001g500000EQdfpAAD"   // Add all IDs you want to delete
            // add more IDs here...
        );

        deleteAccounts(accountIds);
    }

    // =========================================================
    // 🗑️ OPTION 2 — Delete by querying Salesforce (SOQL)
    // Find and delete ALL accounts matching a condition
    // =========================================================

    @Test
    public void deleteAccountsByQuery() {
        System.out.println("🔍 Querying accounts to delete...");

        // Find all accounts created by your test data
        String soqlQuery =
            "SELECT Id, Name FROM Account WHERE " +
            "Name IN ('Teju Solutions','NekstIT B.V.','DataBridge NL'," +
            "'CloudNine Europe','GreenTech Eindhoven','HealthPlus NL'," +
            "'LogiTrans BV','EduLearn NL','Account To Delete')";

        Response queryResponse = given()
            .spec(requestSpec)
            .queryParam("q", soqlQuery)
        .when()
            .get(queryUrl())
        .then()
            .statusCode(200)
            .extract().response();

        int totalFound = queryResponse.jsonPath().getInt("totalSize");
        System.out.println("📋 Found " + totalFound + " accounts to delete");

        if (totalFound == 0) {
            System.out.println("✅ No accounts found — nothing to delete!");
            return;
        }

        // Extract IDs from query results
        List<String> ids      = queryResponse.jsonPath().getList("records.Id");
        List<String> names    = queryResponse.jsonPath().getList("records.Name");

        // Print what will be deleted
        System.out.println("🗑️ Accounts to delete:");
        for (int i = 0; i < ids.size(); i++) {
            System.out.println("   " + names.get(i) + " → " + ids.get(i));
        }

        deleteAccounts(ids);
    }

    // =========================================================
    // 🗑️ OPTION 3 — Delete accounts matching Excel data
    // Queries Salesforce by name, then deletes matches
    // =========================================================

    @Test
    public void deleteAccountsFromExcel() {
        System.out.println("📊 Reading account names from Excel...");

        List<Map<String, String>> excelData = ExcelDataReader.readExcel(
            "src/test/resources/data/salesforce_accounts.xlsx",
            "Accounts"
        );

        // Build IN clause from Excel names
        List<String> names = excelData.stream()
            .map(row -> "'" + row.get("Name") + "'")
            .toList();

        String soqlQuery =
            "SELECT Id, Name FROM Account WHERE Name IN (" +
            String.join(",", names) + ")";

        System.out.println("🔍 Querying: " + soqlQuery);

        Response queryResponse = given()
            .spec(requestSpec)
            .queryParam("q", soqlQuery)
        .when()
            .get(queryUrl())
        .then()
            .statusCode(200)
            .extract().response();

        int totalFound = queryResponse.jsonPath().getInt("totalSize");
        System.out.println("📋 Found " + totalFound + " matching accounts");

        if (totalFound == 0) {
            System.out.println("✅ No accounts found — nothing to delete!");
            return;
        }

        List<String> ids = queryResponse.jsonPath().getList("records.Id");
        deleteAccounts(ids);
    }

    // =========================================================
    // 🗑️ OPTION 4 — Delete accounts matching CSV data
    // =========================================================

    @Test
    public void deleteAccountsFromCsv() {
        System.out.println("📄 Reading account names from CSV...");

        List<Map<String, String>> csvData = CsvDataReader.readCsv(
            "src/test/resources/data/salesforce_accounts.csv"
        );

        List<String> names = csvData.stream()
            .map(row -> "'" + row.get("Name") + "'")
            .toList();

        String soqlQuery =
            "SELECT Id, Name FROM Account WHERE Name IN (" +
            String.join(",", names) + ")";

        Response queryResponse = given()
            .spec(requestSpec)
            .queryParam("q", soqlQuery)
        .when()
            .get(queryUrl())
        .then()
            .statusCode(200)
            .extract().response();

        int totalFound = queryResponse.jsonPath().getInt("totalSize");
        System.out.println("📋 Found " + totalFound + " matching accounts");

        if (totalFound == 0) {
            System.out.println("✅ No accounts found — nothing to delete!");
            return;
        }

        List<String> ids = queryResponse.jsonPath().getList("records.Id");
        deleteAccounts(ids);
    }

    // =========================================================
    // 🔧 HELPER — Delete a list of account IDs
    // =========================================================

    private void deleteAccounts(List<String> accountIds) {
        System.out.println("\n🗑️ Deleting " + accountIds.size() + " accounts...");

        List<String> deleted = new ArrayList<>();
        List<String> failed  = new ArrayList<>();

        for (String id : accountIds) {
            Response response = given()
                .spec(requestSpec)
            .when()
                .delete(recordUrl("Account", id))
            .then()
                .extract().response();

            if (response.getStatusCode() == 204) {
                deleted.add(id);
                System.out.println("   ✅ Deleted: " + id);
            } else {
                failed.add(id);
                System.out.println("   ❌ Failed : " + id +
                    " → " + response.getStatusCode() +
                    " | " + response.getBody().asString());
            }
        }

        // Summary
        System.out.println("\n📊 Cleanup Summary:");
        System.out.println("   ✅ Deleted : " + deleted.size());
        System.out.println("   ❌ Failed  : " + failed.size());

        if (!failed.isEmpty()) {
            System.out.println("   Failed IDs: " + failed);
        }

        System.out.println("🏁 Cleanup complete!");
    }
}
