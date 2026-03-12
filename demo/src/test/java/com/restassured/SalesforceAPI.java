package com.restassured;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.response.Response;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

import static io.restassured.RestAssured.given;

/**
 * SalesforceAuthHelper - Handles OAuth 2.0 JWT Bearer Flow.
 * No password or security token required — uses private key to sign JWT.
 */
public class SalesforceAPI {

    private static String accessToken;
    private static String instanceUrl;

    /**
     * Authenticates using JWT Bearer Flow.
     * Token is cached — Salesforce is only called once per test run.
     */
    private static void authenticate() {
        if (accessToken != null && instanceUrl != null) {
            return; // Already authenticated, reuse cached token
        }

        try {
            // Step 1 — Load private key from PEM file
            PrivateKey privateKey = loadPrivateKey(
                ConfigReader.get("sf.private.key.path")
            );
            System.out.println("✅ Private key loaded successfully!");

            // Step 2 — Build and sign JWT
            String jwtToken = Jwts.builder()
                .setIssuer(ConfigReader.get("sf.client.id"))       // Consumer Key
                .setSubject(ConfigReader.get("sf.username"))        // SF username
                .setAudience("https://login.salesforce.com")          // SF login URL
                .setExpiration(new Date(System.currentTimeMillis()
                    + (3 * 60 * 1000)))                             // expires in 3 mins
                .signWith(privateKey, SignatureAlgorithm.RS256)     // sign with RSA-256
                .compact();
            System.out.println("✅ JWT token built successfully!");

            // Step 3 — Exchange JWT for Salesforce Access Token
            Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type",
                    "urn:ietf:params:oauth:grant-type:jwt-bearer") // JWT grant type
                .formParam("assertion", jwtToken)                   // signed JWT
            .when()
                .post(ConfigReader.getLoginUrl() + "/services/oauth2/token")
            .then()
                .extract().response();

            System.out.println("🔍 Status : " + response.getStatusCode());
            System.out.println("🔍 Body   : " + response.getBody().asString());

            if (response.getStatusCode() == 200) {
                accessToken = response.jsonPath().getString("access_token");
                instanceUrl = response.jsonPath().getString("instance_url");
                System.out.println("✅ Authenticated via JWT Bearer Flow!");
                System.out.println("📍 Instance URL: " + instanceUrl);
            } else {
                throw new RuntimeException("❌ JWT Auth failed: "
                    + response.getBody().asString());
            }

        } catch (Exception e) {
            throw new RuntimeException("❌ JWT Authentication error: "
                + e.getMessage(), e);
        }
    }

    /**
     * Loads RSA Private Key from a PEM file.
     * Supports both PKCS#1 and PKCS#8 formats.
     */
    private static PrivateKey loadPrivateKey(String filePath) throws Exception {
        String keyContent = new String(Files.readAllBytes(Paths.get(filePath)))
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", ""); // remove all whitespace and newlines

        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Returns the OAuth access token.
     * Authenticates first if not already done.
     */
    public static String getAccessToken() {
        authenticate();
        return accessToken;
    }

    /**
     * Returns the Salesforce instance URL.
     * Authenticates first if not already done.
     */
    public static String getInstanceUrl() {
        authenticate();
        return instanceUrl;
    }

    /**
     * Clears cached token — use this if you get a 401 Unauthorized.
     * Next call to getAccessToken() will re-authenticate.
     */
    public static void resetToken() {
        accessToken = null;
        instanceUrl = null;
        System.out.println("🔄 Token cleared. Will re-authenticate on next request.");
    }
}