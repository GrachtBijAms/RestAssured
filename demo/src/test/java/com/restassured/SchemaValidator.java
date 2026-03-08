package com.restassured;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.module.jsv.JsonSchemaValidator;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class SchemaValidator {
    
    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }

        // ── Validate single object schema ─────────────────────────────────────
    @Test
    public void testPostSchema() {
        given()
            .header("Content-Type", "application/json")
        .when()
            .get("/posts/1")
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                "schemas/post_schema.json"
            ));

        System.out.println("✅ Post schema is valid");
    }

        // ── Validate array schema ─────────────────────────────────────────────
    @Test
    public void testUsersSchema() {
        given()
            .header("Content-Type", "application/json")
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                "schemas/users_schema.json"
            ));

        System.out.println("✅ Users schema is valid");
    }

        // ── Validate schema + values together ────────────────────────────────
    @Test
    public void testPostSchemaAndValues() {
        given()
            .header("Content-Type", "application/json")
        .when()
            .get("/posts/1")
        .then()
            .statusCode(200)
            // Schema check first
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath(
                "schemas/post_schema.json"
            ))
            // Then value checks
            .body("id",  equalTo(1))
            .body("userId", equalTo(1))
            .body("title",  notNullValue());

        System.out.println("✅ Schema and values both valid");
    }


}
