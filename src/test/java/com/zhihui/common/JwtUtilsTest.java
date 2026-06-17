package com.zhihui.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {
    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void testGenerateAndParse() {
        String token = jwtUtils.generateToken(1L, "admin");
        assertNotNull(token);
        assertEquals(1L, jwtUtils.getUserIdFromToken(token));
    }

    @Test
    void testTamperedToken() {
        String token = jwtUtils.generateToken(1L, "admin");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertFalse(jwtUtils.validateToken(tampered));
    }
}
