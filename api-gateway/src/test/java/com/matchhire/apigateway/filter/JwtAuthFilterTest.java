package com.matchhire.apigateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "this-is-a-test-secret-key-that-is-long-enough-1234".getBytes(StandardCharsets.UTF_8));

    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(SECRET);
    }

    @Test
    void doFilterInternal_forLoginPath_skipsAuthCheck() throws Exception {
        when(request.getRequestURI()).thenReturn("/auth/login");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_missingAuthHeader_returns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/jobs");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_invalidToken_returns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/jobs");
        when(request.getHeader("Authorization")).thenReturn("Bearer not-a-real-token");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_validToken_proceedsWithWrappedRequest() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
        String token = Jwts.builder()
                .subject("test@example.com")
                .claim("id", "candidate-1")
                .claim("role", "CANDIDATE")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        when(request.getRequestURI()).thenReturn("/jobs");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(UserIdHttpServletRequestWrapper.class), eq(response));
        verify(response, never()).setStatus(anyInt());
    }
}