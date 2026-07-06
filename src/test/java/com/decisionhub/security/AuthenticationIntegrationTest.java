package com.decisionhub.security;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.ForgotPasswordRequest;
import com.decisionhub.dto.ResetPasswordRequest;
import com.decisionhub.dto.TokenRefreshRequest;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.entity.PasswordResetToken;
import com.decisionhub.entity.User;
import com.decisionhub.repository.PasswordResetTokenRepository;
import com.decisionhub.repository.RefreshTokenRepository;
import com.decisionhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/decisionhub",
    "spring.datasource.username=decisionhub_app",
    "spring.datasource.password=dh_dev_sec_pwd_2026",
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.autoconfigure.exclude=org.springframework.ai.autoconfigure.vertexai.gemini.VertexAiGeminiAutoConfiguration"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    @Transactional
    void cleanUp() {
        // Clear tokens to isolate test executions
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        // Delete users but keep seeded roles (seeded via Flyway)
        userRepository.deleteAll();
    }

    @Test
    void testSuccessfulRegistrationAndLogin() throws Exception {
        UserRegisterRequest regRequest = new UserRegisterRequest(
                "integrationuser", "integration@test.com", "Password123!", "First", "Last"
        );

        // 1. Register User
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("integrationuser"))
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        // Verify BCrypt hashing in database
        Optional<User> userOptional = userRepository.findByUsername("integrationuser");
        assertTrue(userOptional.isPresent());
        User user = userOptional.get();
        assertTrue(passwordEncoder.matches("Password123!", user.getPasswordHash()));
        assertTrue(user.getPasswordHash().startsWith("$2a$12$")); // Strength factor 12 prefix

        // 2. Login User
        UserLoginRequest loginRequest = new UserLoginRequest("integrationuser", "Password123!");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        UserRegisterRequest regRequest = new UserRegisterRequest(
                "wrongcreduser", "wrong@test.com", "Password123!", "First", "Last"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Incorrect password
        UserLoginRequest loginRequest = new UserLoginRequest("wrongcreduser", "WrongPassword!");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testJwtAccessControlScenarios() throws Exception {
        // Hitting a protected URL with no JWT
        mockMvc.perform(get("/decisions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Full authentication is required to access this resource"));

        // Hitting with a tampered JWT
        String tamperedJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxODAwMDAwMDAwfQ.invalidSignature";
        mockMvc.perform(get("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tamperedJwt))
                .andExpect(status().isUnauthorized());

        // Hitting with a missing JWT prefix
        mockMvc.perform(get("/decisions")
                        .header(HttpHeaders.AUTHORIZATION, "rawTokenNoBearer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshTokenRotationAndReuseDetection() throws Exception {
        // Create user and seed session refresh tokens
        UserRegisterRequest regRequest = new UserRegisterRequest(
                "refreshuser", "refresh@test.com", "Password123!", "First", "Last"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Login
        UserLoginRequest loginRequest = new UserLoginRequest("refreshuser", "Password123!");
        String loginResponseJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthTokenResponse loginResponse = objectMapper.readValue(loginResponseJson, AuthTokenResponse.class);
        String initialRefreshToken = loginResponse.refreshToken();

        // Rotate once
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest(initialRefreshToken);
        String refreshResponseJson = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthTokenResponse refreshResponse = objectMapper.readValue(refreshResponseJson, AuthTokenResponse.class);
        String rotatedRefreshToken = refreshResponse.refreshToken();

        // Attempting to reuse the revoked initial refresh token
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRefreshRequest(initialRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token is revoked"));

        // Verify token reuse detection revoked the rotated refresh token as well
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRefreshRequest(rotatedRefreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void testLogoutInvalidation() throws Exception {
        UserRegisterRequest regRequest = new UserRegisterRequest(
                "logoutuser", "logout@test.com", "Password123!", "First", "Last"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        UserLoginRequest loginRequest = new UserLoginRequest("logoutuser", "Password123!");
        String loginResponseJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthTokenResponse loginResponse = objectMapper.readValue(loginResponseJson, AuthTokenResponse.class);
        String refreshToken = loginResponse.refreshToken();
        String accessToken = loginResponse.accessToken();

        // Logout
        TokenRefreshRequest logoutRequest = new TokenRefreshRequest(refreshToken);
        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isNoContent());

        // Verify refresh fails after logout
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TokenRefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testForgotPasswordAndResetPasswordWorkflows() throws Exception {
        UserRegisterRequest regRequest = new UserRegisterRequest(
                "recoveruser", "recover@test.com", "Password123!", "First", "Last"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Forgot password - valid email
        ForgotPasswordRequest forgotRequest = new ForgotPasswordRequest("recover@test.com");
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotRequest)))
                .andExpect(status().isOk());

        // Retrieve generated token directly from DB for test verification
        Optional<User> userOptional = userRepository.findByUsername("recoveruser");
        assertTrue(userOptional.isPresent());
        User user = userOptional.get();

        // Check token creation
        java.util.List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();
        assertFalse(tokens.isEmpty());
        PasswordResetToken resetToken = tokens.stream()
                .filter(t -> t.getUser().getId().equals(user.getId()))
                .findFirst().orElseThrow();

        // 1. Reset password successfully
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(resetToken.getToken(), "NewSecuredPassword99!");
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        // Verify password hash changed
        Optional<User> updatedUserOpt = userRepository.findByUsername("recoveruser");
        assertTrue(updatedUserOpt.isPresent());
        assertTrue(passwordEncoder.matches("NewSecuredPassword99!", updatedUserOpt.get().getPasswordHash()));

        // Verify token is deleted after use (cannot reuse)
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void testForgotPasswordWithExpiredToken() throws Exception {
        UserRegisterRequest regRequest = new UserRegisterRequest(
                "expiretokenuser", "expiretoken@test.com", "Password123!", "First", "Last"
        );
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        User user = userRepository.findByUsername("expiretokenuser").orElseThrow();

        // Create an expired password reset token directly
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .createdAt(Instant.now().minus(10, ChronoUnit.MINUTES))
                .build();
        passwordResetTokenRepository.save(expiredToken);

        // Attempt password reset with expired token
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(expiredToken.getToken(), "NewSecuredPassword1!");
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Password reset token has expired"));
    }

    @Test
    void testSqlInjectionAttemptsOnAuthEndpoints() throws Exception {
        // SQL injection payload in login
        UserLoginRequest injectionLogin = new UserLoginRequest("admin' OR '1'='1", "Password123!");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(injectionLogin)))
                .andExpect(status().isUnauthorized());

        // SQL injection payload in register username
        UserRegisterRequest injectionRegister = new UserRegisterRequest(
                "admin' OR '1'='1", "injection@test.com", "Password123!", "John", "Doe"
        );
        // Validates length and format correctly, should handle SQL characters safely without DB errors
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(injectionRegister)))
                .andExpect(status().isCreated()); // Standard registration succeeds since username is validated string literal
    }

    @Test
    void testInvalidRequestPayloads() throws Exception {
        // Malformed JSON syntax
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact system administration."));
    }
}
