package com.example.minibank.auth.service;


import com.example.minibank.auth.dto.LoginRequest;
import com.example.minibank.auth.dto.TokenResponse;
import com.example.minibank.auth.entity.User;
import com.example.minibank.auth.repository.UserRepository;
import com.example.minibank.common.enums.UserStatus;
import com.example.minibank.common.exception.DuplicateResourceException;
import com.example.minibank.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User createUser(
            String username,
            String email,
            String rawPassword) {

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }


    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid credentials")
                );

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        return new TokenResponse(
                accessToken,
                "Bearer",
                900
        );
    }
}
