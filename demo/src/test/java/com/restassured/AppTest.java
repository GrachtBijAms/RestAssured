package com.restassured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test          
    public void testGetGreeting() {   
        when().get("https://apichallenges.eviltester.com/sim/entities").
        then().statusCode(200).contentType(ContentType.JSON);
        System.out.println();
    }

    @Test
    public void testGetASingleEntity() { 
        given().
        when().get("https://apichallenges.eviltester.com/sim/entities/1").
        then().statusCode(200).contentType(ContentType.JSON);
    }


    @Test
    public void testGetASingleEntityWhichDoesnotexist() { 
        given().
        when().get("https://apichallenges.eviltester.com/sim/entities/13").
        then().statusCode(404).contentType(ContentType.JSON);
        
    }


    @Test
    public void testPostASingleEntity() { 
        given().body("{\"name\": \"bob\"}").
        when().post("https://apichallenges.eviltester.com/sim/entities").
        then().statusCode(201).contentType(ContentType.JSON).
        body("name", equalTo("bob"));
        
    }

    @Test
    public void testDeleteASingleEntity() { 
        given().
        when().get("https://apichallenges.eviltester.com/sim/entities/11").
        then().statusCode(200).contentType(ContentType.JSON);
        
    }
}
