package com.decisionhub.security;

import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

public interface AuthenticationFacade {
    Authentication getAuthentication();
    Optional<String> getCurrentUsername();
    Optional<UUID> getCurrentUserId();
}
