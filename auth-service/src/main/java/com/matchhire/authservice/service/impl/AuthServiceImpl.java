package com.matchhire.authservice.service.impl;

import com.matchhire.authservice.dto.*;
import com.matchhire.authservice.exception.EmailAlreadyExistsException;
import com.matchhire.authservice.exception.InvalidCredentialsException;
import com.matchhire.authservice.exception.UserNotFoundException;
import com.matchhire.authservice.mapper.UserMapper;
import com.matchhire.authservice.model.User;
import com.matchhire.authservice.repository.UserRepository;
import com.matchhire.authservice.service.AuthService;
import com.matchhire.authservice.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterRequest register) {
        log.info("Registering new user with email: {}", register.getEmail());
        if (userRepository.existsByEmail(register.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + register.getEmail());
        }
        String encodedPassword = passwordEncoder.encode(register.getPassword());
        User user = UserMapper.toModel(register, encodedPassword);
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid Email or Password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId().toString(), user.getEmail(), user.getUserType().toString());
        return new LoginResponse(token);
    }

    @Override
    public void changePassword(UUID id, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if(!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User Not Found with ID: " + id));

        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getBio() != null) user.setBio(request.getBio());

        User updatedUser = userRepository.save(user);
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse getProfile(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return UserMapper.toResponse(user);
    }


}
