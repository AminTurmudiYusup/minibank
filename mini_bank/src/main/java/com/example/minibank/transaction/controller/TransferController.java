package com.example.minibank.transaction.controller;


import com.example.minibank.transaction.dto.TransferRequest;
import com.example.minibank.transaction.dto.TransferResponse;
import com.example.minibank.transaction.service.TransferService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Validated
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(
            @AuthenticationPrincipal Jwt jwt,

            @RequestHeader("Idempotency-Key")
            @NotBlank
            @Size(max = 100)
            String idempotencyKey,

            @Valid @RequestBody TransferRequest request) {

        UUID userId = UUID.fromString(
                jwt.getClaimAsString("userId")
        );

        return transferService.transfer(
                userId,
                idempotencyKey,
                request
        );
    }
}


