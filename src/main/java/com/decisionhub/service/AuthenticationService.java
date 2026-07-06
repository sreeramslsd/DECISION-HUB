package com.decisionhub.service;

import com.decisionhub.dto.AuthTokenResponse;
import com.decisionhub.dto.TokenRefreshRequest;
import com.decisionhub.dto.UserRegisterRequest;
import com.decisionhub.dto.UserResponse;
import com.decisionhub.dto.UserLoginRequest;

import com.decisionhub.dto.ForgotPasswordRequest;
import com.decisionhub.dto.ResetPasswordRequest;

public interface AuthenticationService {

    UserResponse register(UserRegisterRequest request, String ipAddress, String userAgent);

    AuthTokenResponse login(UserLoginRequest request, String ipAddress, String userAgent);

    AuthTokenResponse refresh(TokenRefreshRequest request);

    void logout(String refreshToken, String ipAddress, String userAgent);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
