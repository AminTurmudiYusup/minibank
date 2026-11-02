package com.example.minibank.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 100, message = "Username must be between 4 and 100 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        String fullName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9+\\- ]{8,20}$",
                message = "Invalid phone number"
        )
        String phone,

        @Size(max = 255)
        String address

) {}
