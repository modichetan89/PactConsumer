package com.rahulshettyacademy;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahulshettyacademy.controller.LibraryController;
import com.rahulshettyacademy.controller.ProductsPrices;
import com.rahulshettyacademy.controller.SpecificProduct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "CoursesCatalogue")
public class PactConsumerTest1 {

    @Autowired
    private LibraryController libraryController;

    /** Test 1*/
    //In below method we have defined consumer as booksCatalogue
    //Using builder object we have construct a dummy response using pact
    //This response is returned when /allCourseDetails get called,
    // then status 200 returned with response as defined in body with minimum size of response array as 2 with price value as 12 for both index of array.
    @Disabled
    @Pact(consumer = "BooksCatalogue")
    public RequestResponsePact pactAllCoursesDetailsConfig(PactDslWithProvider builder){
        return builder.given("courses exist")
                .uponReceiving("getting all courses details")
                .path("/allCourseDetails")
                .willRespondWith()
                .status(200)
                .body(PactDslJsonArray.arrayMinLike(2)
                        .stringType("course_name")
                        .stringType("id")
                        .integerType("price", 12)
                        .stringType("category")
                        .closeObject()
                ).toPact();
    }



    @Pact(consumer = "BooksCatalogue")
    public RequestResponsePact pactAllCoursesDetailsPriceCheck(PactDslWithProvider builder){
        return builder.given("courses exist")
                .uponReceiving("getting all courses details")
                .path("/allCourseDetails")
                .willRespondWith()
                .status(200)
                .body(PactDslJsonArray
                        .arrayMinLike(2)
                        .integerType("price", 12)
                        .closeObject()
                ).toPact();
    }


    //Here in below test method we have to pass pactMethod name,
    //so that this test will execute as per the rules defined in provided method.
    @Test
    @PactTestFor(pactMethod = "pactAllCoursesDetailsPriceCheck", port = "9999")
    public void testAllProductsSum(MockServer mockServer) throws JsonProcessingException {
        String expectedJson = "{\"booksPrice\":250,\"coursesPrice\":24}";
        //booksPrice is hardcoded value, so we kept it as it is.
        //coursesPrice should actually coming from courses service
        //but in our case it will come from config method, we set value as 12 for two values of array. so sum is 24.
        //Make sure to pass expected json in correct format. To remove any spacing issues - https://jsontostring.com/
        libraryController.setBaseUrl(mockServer.getUrl());
        ProductsPrices productsPrices = libraryController.getProductPrices();
        //How to start pact server so that instead of hitting the actual service url
        //it should hit our pact server and provide response from there which we configured above.
        //We have to override the setBaseURL method which is present in this service.
        //When working on actual project, find a way or method where we can pass base url.
        //We have to provide port in @PactTestFor annotation where pact server should run.
        //To get the url for pact server, we have to use class MockServer and method name as getUrl()
        ObjectMapper obj = new ObjectMapper();
        String actualJson = obj.writeValueAsString(productsPrices);
        //String actualJson = obj.writerWithDefaultPrettyPrinter().writeValueAsString(productsPrices);
        //writerWithDefaultPrettyPrinter this method will make json format correctly.
        Assertions.assertEquals(expectedJson, actualJson);
        //Once our test case get passed this will write pact contract json file in target/pacts folder.
        //If provider make any changes in response schema then this file will tell them that it can impact consumer unit test cases.
    }


/** Test 2*/

    @Pact(consumer = "BooksCatalogue")
    public RequestResponsePact pactAppiumCourseDetails(PactDslWithProvider builder){
        return builder.given("appium course exist")
                .uponReceiving("getting appium course detail")
                .path("/getCourseByName/Appium")
                .willRespondWith()
                .status(200)
                .body(new PactDslJsonBody()
                        .integerType("price", 450)
                        .stringType("category", "mobile")
                ).toPact();
        //In this method we have used PactDslJsonBody because we are getting only Json in response
        //instead of array of json. We don't need to use close object for this.
        //We have defined key and value for Price and category in our Pact contract to validate
    }

    @Test
    @PactTestFor(pactMethod = "pactAppiumCourseDetails", port = "9999")
    public void testByProductName(MockServer mockServer) throws JsonProcessingException {
        String expectedJson = "{\"product\":{\"book_name\":\"Appium\",\"id\":\"ttefs36\",\"isbn\":\"ttefs\",\"aisle\":36,\"author\":\"Shetty\"},\"price\":450,\"category\":\"mobile\"}";
        libraryController.setBaseUrl(mockServer.getUrl());
        SpecificProduct specificProduct = libraryController.getProductFullDetails("Appium");
        ObjectMapper objectMapper = new ObjectMapper();
        String actualJson = objectMapper.writeValueAsString(specificProduct);
        Assertions.assertEquals(expectedJson, actualJson);
        //Here in this method we are using pact method as pactAppiumCourseDetails.
        //We are overriding the courses actual url with our mock one.
        //We are asserting if price and category from courses api's
        // are getting with correct name and values as defined in pact config.
    }

/** Test 3*/

    @Pact(consumer = "BooksCatalogue")
    public RequestResponsePact pactGetCourseByNameNotExist(PactDslWithProvider builder){
        return builder.given("appium course not exist", "name", "Appium")
                .uponReceiving("not getting appium course detail")
                .path("/getCourseByName/Appium")
                .willRespondWith()
                .status(404)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "pactGetCourseByNameNotExist", port = "9999")
    public void testByProductNameNotExist(MockServer mockServer) throws JsonProcessingException {
        String expectedJson = "{\"product\":{\"book_name\":\"Appium\",\"id\":\"ttefs36\",\"isbn\":\"ttefs\",\"aisle\":36,\"author\":\"Shetty\"},\"msg\":\"AppiumCategory and price details are not available at this time\"}";
        libraryController.setBaseUrl(mockServer.getUrl());
        SpecificProduct specificProduct = libraryController.getProductFullDetails("Appium");
        ObjectMapper objectMapper = new ObjectMapper();
        String actualJson = objectMapper.writeValueAsString(specificProduct);
        Assertions.assertEquals(expectedJson, actualJson);
    }



}
