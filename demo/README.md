# RestAssured Project

## Overview
This project is a Java-based testing framework built using RestAssured for API testing. It includes test cases for validating API responses, schema validation, and Salesforce API integration.

## Project Structure
```
demo/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── restassured/
│   │               └── App.java
│   ├── test/
│   │   └── java/
│   │       └── com/
│   │           └── restassured/
│   │               ├── AppTest.java
│   │               ├── AppTestS.java
│   │               ├── SalesforceCreateTest.java
│   │               ├── SchemaValidator.java
│   │               ├── base/
│   │               │   └── SalesforceBaseTest.java
│   │               ├── helper/
│   │               │   └── SalesforceAPI.java
│   │               └── utils/
│   │                   └── ConfigReader.java
│   └── resources/
│       ├── config.properties
│       └── schemas/
│           ├── post_schema.json
│           ├── user_schema.json
│           └── users_schema.json
└── target/
    ├── classes/
    ├── surefire-reports/
    └── test-classes/
```

## Features
- **API Testing**: Test cases for validating API responses.
- **Schema Validation**: JSON schema validation for API responses.
- **Salesforce Integration**: Test cases for Salesforce API endpoints.
- **Configuration Management**: Centralized configuration using `config.properties`.

## Prerequisites
- Java 8 or higher
- Maven 3.6+

## Setup
1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
2. Navigate to the project directory:
   ```bash
   cd RestAssured/demo
   ```
3. Install dependencies:
   ```bash
   mvn clean install
   ```

## Running Tests
To execute the test cases, run the following command:
```bash
mvn test
```

## Test Reports
Test reports are generated in the `target/surefire-reports` directory. Open `index.html` for a detailed report.

## Configuration
Update the `config.properties` file in `src/test/resources` to set up environment-specific configurations.

## Directory Details
- **src/main/java**: Contains the main application code.
- **src/test/java**: Contains test cases and utility classes.
- **src/test/resources**: Contains configuration files and JSON schemas.
- **target**: Contains compiled classes and test reports.

## Key Classes
- `SalesforceCreateTest`: Test cases for Salesforce API creation.
- `SchemaValidator`: JSON schema validation utility.
- `ConfigReader`: Reads configuration properties.
- `SalesforceAPI`: Helper class for Salesforce API interactions.



## Author
Prathamesh Patil