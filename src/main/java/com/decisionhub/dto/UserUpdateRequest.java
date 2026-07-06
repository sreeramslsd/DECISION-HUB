package com.decisionhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(max = 50, message = "First name must be less than 50 characters")
    String firstName,

    @Size(max = 50, message = "Last name must be less than 50 characters")
    String lastName,

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    String email,

    @Size(max = 255)
    String avatarUrl
) {}
