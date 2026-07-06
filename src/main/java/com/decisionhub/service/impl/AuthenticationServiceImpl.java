package com.decisionhub.service.impl;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.TokenRefreshRequest;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.entity.AuditLog;
import com.decisionhub.entity.RefreshToken;
import com.decisionhub.entity.Role;
import com.decisionhub.entity.RoleName;
import com.decisionhub.entity.User;
import com.decisionhub.entity.UserStatus;
import com.decisionhub.exception.ConflictException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.exception.UnauthorizedException;
import com.decisionhub.mapper.UserMapper;
import com.decisionhub.repository.AuditLogRepository;
import com.decisionhub.repository.RefreshTokenRepository;
import com.decisionhub.repository.RoleRepository;
import com.decisionhub.repository.UserRepository;
import com.decisionhub.dto.ForgotPasswordRequest;
import com.decisionhub.dto.ResetPasswordRequest;
import com.decisionhub.entity.PasswordResetToken;
import com.decisionhub.repository.PasswordResetTokenRepository;
import com.decisionhub.security.CustomUserDetails;
import com.decisionhub.security.JwtTokenProvider;
import com.decisionhub.security.SecurityProperties;
import com.decisionhub.service.AuthenticationService;
import com.decisionhub.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final SecurityProperties securityProperties;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserResponse register(UserRegisterRequest request, String ipAddress, String userAgent) {
        log.info("Attempting registration for username: {} and email: {}", request.username(), request.email());

        // Validate duplicates
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        // Fetch default user role
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseThrow(() -> new ResourceNotFoundException("Default User role not found"));

        // Build and save user
        User user = User.builder()
            .username(request.username())
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .role(userRole)
            .status(UserStatus.ACTIVE)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Write audit log
        AuditLog auditLog = AuditLog.builder()
            .user(savedUser)
            .action("REGISTER")
            .targetTable("users")
            .targetId(savedUser.getId())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        auditLogRepository.save(auditLog);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthTokenResponse login(UserLoginRequest request, String ipAddress, String userAgent) {
        log.info("Authenticating user: {}", request.username());

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Issue tokens
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        
        // Exclude and delete existing token, then issue new rotated refresh token
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiresAt(Instant.now().plusMillis(securityProperties.getRefreshExpirationMs()))
            .revoked(false)
            .build();
        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        log.info("User {} authenticated successfully", user.getUsername());

        // Write audit log
        AuditLog auditLog = AuditLog.builder()
            .user(user)
            .action("LOGIN")
            .targetTable("users")
            .targetId(user.getId())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        auditLogRepository.save(auditLog);

        return new AuthTokenResponse(
            accessToken,
            savedRefreshToken.getToken(),
            "Bearer",
            securityProperties.getExpirationMs() / 1000
        );
    }

    @Override
    @Transactional(noRollbackFor = {UnauthorizedException.class})
    public AuthTokenResponse refresh(TokenRefreshRequest request) {
        log.info("Processing token refresh request");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            // Revoked token reuse detected! Revoke all tokens for this user as a security measure (refresh token reuse detection).
            refreshTokenRepository.deleteByUser(refreshToken.getUser());
            log.warn("Revoked refresh token reuse detected for user {}. All user sessions revoked.", refreshToken.getUser().getUsername());
            throw new UnauthorizedException("Refresh token is revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token is expired");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Rotate token: invalidate the used one and issue a new one
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        RefreshToken rotatedRefreshToken = RefreshToken.builder()
            .user(user)
            .token(UUID.randomUUID().toString())
            .expiresAt(Instant.now().plusMillis(securityProperties.getRefreshExpirationMs()))
            .revoked(false)
            .build();
        RefreshToken savedRotatedToken = refreshTokenRepository.save(rotatedRefreshToken);

        String accessToken = jwtTokenProvider.generateToken(userDetails);

        log.info("Tokens rotated successfully for user {}", user.getUsername());
        return new AuthTokenResponse(
            accessToken,
            savedRotatedToken.getToken(),
            "Bearer",
            securityProperties.getExpirationMs() / 1000
        );
    }

    @Override
    @Transactional
    public void logout(String refreshTokenString, String ipAddress, String userAgent) {
        log.info("Processing logout request");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);
        log.info("User {} logged out successfully", user.getUsername());

        // Write audit log
        AuditLog auditLog = AuditLog.builder()
            .user(user)
            .action("LOGOUT")
            .targetTable("users")
            .targetId(user.getId())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password request for email: {}", request.email());

        // Seek user by email
        // To avoid user enumeration attacks, if user is not found, we return successfully
        // without throwing an exception.
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            log.info("Forgot password email {} not associated with any active user. Silently skipping token generation.", request.email());
            return;
        }

        // Invalidate prior active password reset tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate a new secure UUID reset token
        String token = UUID.randomUUID().toString();
        // Set expiration to 15 minutes
        Instant expiresAt = Instant.now().plus(java.time.Duration.ofMinutes(15));

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
            .user(user)
            .token(token)
            .expiresAt(expiresAt)
            .createdAt(Instant.now())
            .build();

        passwordResetTokenRepository.save(passwordResetToken);
        log.info("Generated password reset token for user: {}", user.getUsername());

        // Construct reset URL
        String resetUrl = "http://localhost:8080/api/v1/auth/reset-password?token=" + token;

        // Delegate to emailService
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing reset password request");

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
            .orElseThrow(() -> new UnauthorizedException("Invalid or expired password reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new UnauthorizedException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Invalidate / delete the token after successful password reset
        passwordResetTokenRepository.delete(resetToken);
        log.info("Password reset successfully for user: {}", user.getUsername());

        // Write audit log
        AuditLog auditLog = AuditLog.builder()
            .user(user)
            .action("PASSWORD_RESET")
            .targetTable("users")
            .targetId(user.getId())
            .ipAddress("SYSTEM")
            .userAgent("SYSTEM")
            .build();
        auditLogRepository.save(auditLog);
    }
}
