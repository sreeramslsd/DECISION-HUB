package com.decisionhub.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator mapping security evaluation in method-level @PreAuthorize.
 */
@Component
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }
        
        log.debug("Evaluating target permission {} on object {}", permission, targetDomainObject);
        return hasPrivilege(authentication, (String) permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || targetType == null || !(permission instanceof String)) {
            return false;
        }

        log.debug("Evaluating target permission {} on resource type {} with ID {}", permission, targetType, targetId);
        return hasPrivilege(authentication, (String) permission);
    }

    private boolean hasPrivilege(Authentication authentication, String permission) {
        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority.getAuthority().equalsIgnoreCase(permission)) {
                return true;
            }
        }
        return false;
    }
}
