package com.matchhire.authservice.service;


import com.matchhire.authservice.dto.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface AuthService {
    void register(RegisterRequest register);

    LoginResponse login(LoginRequest login);

    void changePassword(UUID id, ChangePasswordRequest changePasswordRequest);

    UserResponse updateProfile(UUID id, UpdateProfileRequest updateProfile);

    UserResponse getProfile(UUID id);
}
