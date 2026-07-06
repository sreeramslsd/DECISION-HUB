package com.decisionhub.controller;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.TokenRefreshRequest;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.dto.ForgotPasswordRequest;
import com.decisionhub.dto.ResetPasswordRequest;
import com.decisionhub.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication & Authorization endpoints")
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers user credentials and returns user details")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegisterRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("Request received: Register user: {}", request.username());
        String ipAddress = getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);
        
        UserResponse response = authenticationService.register(request, ipAddress, userAgent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue tokens", description = "Authenticates user password and issues JWT access and refresh tokens")
    public ResponseEntity<AuthTokenResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("Request received: Login user: {}", request.username());
        String ipAddress = getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);

        AuthTokenResponse response = authenticationService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate expired access tokens", description = "Validates the refresh token and issues rotated access and refresh tokens")
    public ResponseEntity<AuthTokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Request received: Refresh token");
        AuthTokenResponse response = authenticationService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Terminate session and revoke tokens", description = "Invalidates the refresh token and terminates the active session")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("Request received: Logout");
        String ipAddress = getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);

        authenticationService.logout(request.refreshToken(), ipAddress, userAgent);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset", description = "Generates a reset token and logs/emails a recovery link to the user")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Request received: Forgot password for email: {}", request.email());
        authenticationService.forgotPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Verifies the reset token and updates the user's password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Request received: Reset password");
        authenticationService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
