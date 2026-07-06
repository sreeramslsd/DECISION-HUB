package com.decisionhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public Optional<String> getCurrentUsername() {
        return SecurityUtils.getCurrentUsername();
    }

    @Override
    public Optional<UUID> getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}
