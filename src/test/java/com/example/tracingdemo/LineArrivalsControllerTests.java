/*
package com.example.tracingdemo;

import com.example.tracingdemo.controller.LineArrivalsController;
import com.example.tracingdemo.filter.PreRoutingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LineArrivalsControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testServerWithRestTemplate() {
        // Provide the relative path (without /services-app)
        String response = restTemplate.postForObject("/services/V4/LineArrivals", null, String.class);

        // Print response explicitly
        if (response != null) {
            System.out.println("Response: " + response);
        } else {
            System.out.println("Response was null.");
        }
    }
}
*/
