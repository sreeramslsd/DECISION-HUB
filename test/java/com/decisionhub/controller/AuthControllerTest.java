package com.decisionhub.controller;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.ForgotPasswordRequest;
import com.decisionhub.dto.ResetPasswordRequest;
import com.decisionhub.dto.TokenRefreshRequest;
import com.decisionhub.dto.UserLoginRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.decisionhub\\.security\\..*"
    )
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    // Prevent JPA Auditing context load failures during WebMvcTest
    @MockBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void register_withValidPayload_returnsCreated() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("testuser", "test@test.com", "Password123!", "John", "Doe");
        UserResponse response = new UserResponse(UUID.randomUUID(), "testuser", "test@test.com", "John", "Doe", "ROLE_USER", "ACTIVE", null, Instant.now());

        when(authenticationService.register(any(UserRegisterRequest.class), any(), any())).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void register_withInvalidPayload_returnsBadRequest() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("us", "invalid-email", "", "John", "Doe");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void login_withValidCredentials_returnsOk() throws Exception {
        UserLoginRequest request = new UserLoginRequest("testuser", "Password123!");
        AuthTokenResponse response = new AuthTokenResponse("access-token", "refresh-token", "Bearer", 900L);

        when(authenticationService.login(any(UserLoginRequest.class), any(), any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_withBlankUsername_returnsBadRequest() throws Exception {
        UserLoginRequest request = new UserLoginRequest("", "Password123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_withValidToken_returnsOk() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");
        AuthTokenResponse response = new AuthTokenResponse("new-access-token", "new-refresh-token", "Bearer", 900L);

        when(authenticationService.refresh(any(TokenRefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refresh_withBlankToken_returnsBadRequest() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_withValidToken_returnsNoContent() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

        doNothing().when(authenticationService).logout(eq("valid-refresh-token"), any(), any());

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void forgotPassword_withValidEmail_returnsOk() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");

        doNothing().when(authenticationService).forgotPassword(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_withInvalidEmail_returnsBadRequest() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("not-an-email");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_withValidPayload_returnsOk() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("token-123", "NewSecurePassword123!");

        doNothing().when(authenticationService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_withShortPassword_returnsBadRequest() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("token-123", "short");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
