package com.decisionhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record InvitationRequest(
    @NotBlank(message = "Invitee email is required")
    @Email(message = "Invalid email format")
    String inviteeEmail,

    UUID decisionId,

    UUID communityId
) {}
