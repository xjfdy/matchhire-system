package com.matchhire.authservice.controller;

import com.matchhire.authservice.dto.*;
import com.matchhire.authservice.service.AuthService;
import com.matchhire.authservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "API for Authentication")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @Operation(summary = "Create a new User")
    public ResponseEntity<String> register(@Validated @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/change-password")
    @Operation(summary = "User changes password")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Validated @RequestBody ChangePasswordRequest request) {
        UUID id = extractUserIdFromHeader(authHeader);
        authService.changePassword(id, request);
        return ResponseEntity.ok().body("Password changed successfully");
    }

    @PutMapping("/modify-profile")
    @Operation(summary = "User updates profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Validated @RequestBody UpdateProfileRequest request) {
        UUID id = extractUserIdFromHeader(authHeader);
        UserResponse response = authService.updateProfile(id, request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader("Authorization") String authHeader) {
        UUID id = extractUserIdFromHeader(authHeader);
        UserResponse response = authService.getProfile(id);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<Void> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        jwtUtil.validateToken(token);
        return ResponseEntity.ok().build();
    }

    private UUID extractUserIdFromHeader(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }
}
