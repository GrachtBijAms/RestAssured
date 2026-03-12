package com.restassured.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigReader - Utility class to read Salesforce API configuration
 * from config.properties file.
 *
 * Usage: ConfigReader.get("sf.username");
 */
public class ConfigReader {

    private static Properties properties;
    private static final String CONFIG_FILE_PATH = "src/test/resources/config.properties";

    // Static block — loads config once when class is first used
    static {
        try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
            properties = new Properties();
            properties.load(input);
            System.out.println("✅ Config loaded successfully from: " + CONFIG_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to load config.properties from: "
                    + CONFIG_FILE_PATH, e);
        }
    }

    /**
     * Get a property value by key.
     * Throws RuntimeException if key is missing.
     */
    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("❌ Missing required config property: " + key);
        }
        return value.trim();
    }

    /**
     * Get a property value with a fallback default.
     * Returns defaultValue if key is missing.
     */
    public static String get(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            System.out.println("⚠️ Config key '" + key + "' not found. Using default: " + defaultValue);
            return defaultValue;
        }
        return value.trim();
    }

    /**
     * Returns the full Salesforce password (password + security token combined).
     * Salesforce requires these to be concatenated into a single string.
     */
    public static String getSalesforcePassword() {
        String password = get("sf.password");
        String token = get("sf.security.token", ""); // token optional if IP is whitelisted
        return password + token;
    }

    /**
     * Returns the Salesforce login URL.
     * Defaults to production login URL if not specified.
     */
public static String getLoginUrl() {
    // Automatically strips trailing slash if present
    return get("sf.login.url", "https://login.salesforce.com")
        .replaceAll("/$", "");  // 👈 removes trailing slash
}

    /**
     * Returns the Salesforce API version.
     * Defaults to v57.0 if not specified.
     */
    public static String getApiVersion() {
        return get("sf.api.version", "v57.0");
    }

    public static String getClientId() {
        return get("sf.client.id");
    }  
    
    public static String getClientSecret() {
        return get("sf.client.secret");
    }
}