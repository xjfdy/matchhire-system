package com.matchhire.authservice.service.impl;

import com.matchhire.authservice.dto.LoginRequest;
import com.matchhire.authservice.dto.LoginResponse;
import com.matchhire.authservice.dto.RegisterRequest;
import com.matchhire.authservice.exception.EmailAlreadyExistsException;
import com.matchhire.authservice.exception.InvalidCredentialsException;
import com.matchhire.authservice.exception.UserNotFoundException;
import com.matchhire.authservice.model.User;
import com.matchhire.authservice.model.UserType;
import com.matchhire.authservice.repository.UserRepository;
import com.matchhire.authservice.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    // ---------- register ----------

    @Test
    void register_whenEmailExists_throwsEmailAlreadyExistsException() {
        RegisterRequest request = registerRequest("alice@test.com");
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenNewEmail_savesUserWithEncodedPassword() {
        RegisterRequest request = registerRequest("alice@test.com");
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("alice@test.com", saved.getEmail());
        assertEquals("hashed-password", saved.getPassword());
        assertEquals(UserType.CANDIDATE, saved.getUserType());
    }

    // ---------- login ----------

    @Test
    void login_whenEmailNotFound_throwsUserNotFoundException() {
        LoginRequest request = loginRequest("nobody@test.com", "password123");
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void login_whenPasswordWrong_throwsInvalidCredentialsException() {
        User user = existingUser();
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest("alice@test.com", "wrong-password")));

        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void login_whenCredentialsValid_returnsToken() {
        User user = existingUser();
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtUtil.generateToken(user.getId().toString(), "alice@test.com", "CANDIDATE"))
                .thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(loginRequest("alice@test.com", "password123"));

        assertEquals("fake-jwt-token", response.getToken());
    }

    // ---------- helpers ----------

    private RegisterRequest registerRequest(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setUserType(UserType.CANDIDATE);
        request.setSkills(List.of("Java", "Spring"));
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private User existingUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("alice@test.com");
        user.setPassword("hashed-password");
        user.setUserType(UserType.CANDIDATE);
        return user;
    }
}