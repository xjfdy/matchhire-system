package com.matchhire.authservice.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "this-is-a-test-secret-key-that-is-long-enough-1234".getBytes());

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    void generateToken_thenExtractUserId_returnsOriginalId() {
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateToken(userId.toString(), "test@example.com", "CANDIDATE");

        assertEquals(userId, jwtUtil.extractUserId(token));
    }

    @Test
    void generateToken_thenExtractEmail_returnsOriginalEmail() {
        String token = jwtUtil.generateToken(UUID.randomUUID().toString(), "test@example.com", "CANDIDATE");

        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void validateToken_withValidToken_doesNotThrow() {
        String token = jwtUtil.generateToken(UUID.randomUUID().toString(), "test@example.com", "CANDIDATE");

        assertDoesNotThrow(() -> jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_withGarbageToken_throwsJwtException() {
        assertThrows(JwtException.class, () -> jwtUtil.validateToken("not-a-real-token"));
    }
}