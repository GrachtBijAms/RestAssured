package com.restassured.base;


import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.restassured.helper.SalesforceAPI;
import com.restassured.utils.ConfigReader;


public class SalesforceBaseTest {

    protected static String accessToken;
    protected static String instanceUrl;
    protected static String apiVersion;
    protected static RequestSpecification requestSpec;

    @BeforeSuite
public void globalSetup() {
    System.out.println("🔐 Authenticating with Salesforce...");
    accessToken = SalesforceAPI.getAccessToken();  // matches your existing method name
    instanceUrl = SalesforceAPI.getInstanceUrl();
    apiVersion  = ConfigReader.getApiVersion();
}

    @BeforeClass
    public void setup() {
        // Build reusable request spec for every test class
        requestSpec = new RequestSpecBuilder()
            .setBaseUri(instanceUrl)
            .addHeader("Authorization", "Bearer " + accessToken)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build();
    }

    @AfterSuite
public void cleanup() {
    System.out.println("🧹 Running post-suite cleanup...");
    // call your cleanup logic here
}

    /**
     * Base URL for Salesforce REST API calls.
     * Example: https://yourorg.salesforce.com/services/data/v57.0
     */
    protected String baseApiUrl() {
        return "/services/data/" + apiVersion;
    }

    /**
     * Base URL for a specific Salesforce object.
     * Example: /services/data/v57.0/sobjects/Account
     */
    protected String sobjectUrl(String objectName) {
        return baseApiUrl() + "/sobjects/" + objectName;
    }

    /**
     * Base URL for a specific Salesforce record.
     * Example: /services/data/v57.0/sobjects/Account/0015g00000XXXXXX
     */
    protected String recordUrl(String objectName, String recordId) {
        return sobjectUrl(objectName) + "/" + recordId;
    }

    /**
     * Base URL for SOQL queries.
     * Example: /services/data/v57.0/query
     */
    protected String queryUrl() {
        return baseApiUrl() + "/query";
    }
}