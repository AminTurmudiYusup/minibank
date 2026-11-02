package com.example.minibank.auth.service;

import com.example.minibank.auth.dto.LoginRequest;
import com.example.minibank.auth.dto.TokenResponse;
import com.example.minibank.auth.entity.User;
import com.example.minibank.auth.repository.UserRepository;
import com.example.minibank.common.enums.UserStatus;
import com.example.minibank.common.exception.DuplicateResourceException;
import com.example.minibank.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldCreateUserSuccessfully() {

        // Arrange
        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = authService.createUser(
                "john",
                "john@gmail.com",
                "Password123!"
        );

        // Assert
        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals("john@gmail.com", result.getEmail());
        assertEquals("hashed-password", result.getPasswordHash());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("Password123!");
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {

        // Arrange
        when(userRepository.existsByUsername("john"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> authService.createUser(
                        "john",
                        "john@gmail.com",
                        "Password123!"
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        // Arrange
        when(userRepository.existsByUsername("john"))
                .thenReturn(false);

        when(userRepository.existsByEmail("john@gmail.com"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> authService.createUser(
                        "john",
                        "john@gmail.com",
                        "Password123!"
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {

        // Arrange
        User user = new User();
        user.setUsername("john");
        user.setPasswordHash("hashed-password");
        user.setStatus(UserStatus.ACTIVE);

        LoginRequest request = new LoginRequest(
                "john",
                "Password123!"
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123!",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("jwt-token");

        // Act
        TokenResponse result = authService.login(request);

        // Assert
        assertEquals("jwt-token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(900, result.expiresIn());

        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void shouldRejectInvalidPassword() {

        // Arrange
        User user = new User();
        user.setUsername("john");
        user.setPasswordHash("hashed-password");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        // Act & Assert
        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(
                        new LoginRequest("john", "wrong-password")
                )
        );

        verify(jwtService, never()).generateAccessToken(any());
    }
}