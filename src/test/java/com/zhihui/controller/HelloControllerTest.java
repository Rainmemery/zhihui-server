package com.zhihui.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelloControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testHello() {
        ResponseEntity<Map> resp = restTemplate.getForEntity("/api/hello", Map.class);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("Hello, 智会!", ((Map)resp.getBody()).get("data"));
    }
}
