package com.decisionhub.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private SecurityProperties securityProperties;

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Enforce mock key properties (256-bit key requirement for HMAC-SHA256)
        when(securityProperties.getSecret()).thenReturn("dGVzdF9zZWNyZXRfd2l0aF9zdWZmaWNpZW50X2xlbmd0aF9mb3JfYWxnb3JpdGhtX2hsYWMyNTZfdGVzdF9rZXk=");
        when(securityProperties.getExpirationMs()).thenReturn(900000L); // 15 mins

        userDetails = new User("test_user", "password", Collections.emptyList());
    }

    @Test
    void testTokenGenerationAndExtraction() {
        String token = jwtTokenProvider.generateToken(userDetails);
        
        assertNotNull(token);
        assertEquals("test_user", jwtTokenProvider.extractUsername(token));
    }

    @Test
    void testTokenValidationSuccess() {
        String token = jwtTokenProvider.generateToken(userDetails);
        
        assertTrue(jwtTokenProvider.validateToken(token));
        assertTrue(jwtTokenProvider.validateToken(token, userDetails));
    }

    @Test
    void testTokenValidationFailureForMismatchedUser() {
        String token = jwtTokenProvider.generateToken(userDetails);
        UserDetails otherUser = new User("other_user", "password", Collections.emptyList());
        
        assertFalse(jwtTokenProvider.validateToken(token, otherUser));
    }
}
