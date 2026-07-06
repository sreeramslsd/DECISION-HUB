package com.decisionhub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Define production-ready CORS bounds. Allow local client hosts in dev and wildcard defaults.
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://*.decisionhub.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache CORS preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
