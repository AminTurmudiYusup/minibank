package com.example.minibank.auth.controller;

import com.example.minibank.auth.dto.LoginRequest;
import com.example.minibank.auth.dto.TokenResponse;
import com.example.minibank.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request) {

        TokenResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}