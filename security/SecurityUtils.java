package com.decisionhub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class providing access helper methods to retrieve security context attributes.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Retrieves the currently logged-in user's username.
     *
     * @return Optional username string.
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return Optional.of(((UserDetails) principal).getUsername());
        } else if (principal instanceof String) {
            return Optional.of((String) principal);
        }
        
        return Optional.empty();
    }

    /**
     * Retrieves the currently logged-in user's UUID.
     *
     * @return Optional UUID.
     */
    public static Optional<UUID> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return Optional.of(((CustomUserDetails) principal).getId());
        }
        
        return Optional.empty();
    }
}
