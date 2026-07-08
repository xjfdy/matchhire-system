package com.matchhire.authservice.mapper;

import com.matchhire.authservice.dto.RegisterRequest;
import com.matchhire.authservice.dto.UserResponse;
import com.matchhire.authservice.model.User;

import java.time.LocalDateTime;

public class UserMapper {

    public static User toModel(RegisterRequest request, String encodedPassword) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBio(request.getBio());
        user.setPassword(encodedPassword);
        user.setUserType(request.getUserType());
        user.setCreatedAt(LocalDateTime.now());
        user.setSkills(request.getSkills());
        return user;
    }

    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId().toString());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setBio(user.getBio());
        response.setUserType(user.getUserType().toString());
        response.setCreatedAt(user.getCreatedAt().toString());
        response.setSkills(user.getSkills().toString());
        return response;
    }
}
