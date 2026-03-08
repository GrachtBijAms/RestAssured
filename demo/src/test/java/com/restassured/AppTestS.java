package com.restassured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import io.restassured.RestAssured;

public class AppTestS {
    

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }

    @Test
    public void testGetUsers() {

        given()
            .header("Content-Type", "application/json")
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("size()", equalTo(10))
            .body("[0].name", equalTo("Leanne Graham"))
            .body("[0].email", equalTo("Sincere@april.biz"));

    }

    @Test
    public void testGetUserById() { 

        given().header("Content-Type", "application/json")
        .when()
        .get("/users/1")
        .then()
        .statusCode(200)
        .body("name", equalTo("Leanne Graham"))
        .body("email", equalTo("Sincere@april.biz"));
    }

    @Test
    public void testCreatePost(){
        String reqbody = """
        {
            "title": "SDET Bootcamp ",
            "body": "Learning RestAssured with Java",
            "userId": 1
        }
                """;

        given()
            .header("Content-Type", "application/json")
            .body(reqbody)
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .body("title", equalTo("SDET Bootcamp "))
            .body("body", equalTo("Learning RestAssured with Java"))
            .body("userId", equalTo(1));
    }

    @Test
    public void testDeletePost() {
        given()
        .when()
            .delete("/posts/1")
        .then()
            .statusCode(200);
}

    @Test
    public void getUserEmails() {
        List<String> userNames = given()
            .header("Content-Type", "application/json")
        .when()            .get("/users")
        .then()
            .statusCode(200)
            .extract()
            .path("email");
        Assert.assertTrue(userNames.contains("Sincere@april.biz"));
        Assert.assertTrue(userNames.size()==10);
        Assert.assertFalse(userNames.isEmpty());
        Assert.assertFalse(userNames.contains(null));
            // for(String email: userNames){
            //     System.out.println(email);
            // }
            userNames.forEach(System.out::println);
    }

    @Test
    public void testCreatenewPost(){
        String reqbody = """
        {
            "title": "SDET Bootcamp ",
            "body": "Learning RestAssured with Java",   
            "userId": 1
        }
                """;

        String responseid = given()
            .header("Content-Type", "application/json")
            .body(reqbody)
        .when()
            .post("/posts")
        .then()
            .statusCode(201)
            .extract()
            .jsonPath().getString("id");

        Assert.assertNotNull(responseid);
        System.out.println("Created Post ID: " + responseid);

        given()
        .when().get("/posts/" + responseid)
        .then()
        .statusCode(200);
    }

}
