package com.decisionhub.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoint authorizations (supporting both context-path prefixed and clean paths)
                .requestMatchers(
                    "/auth/register", "/api/v1/auth/register",
                    "/auth/login", "/api/v1/auth/login",
                    "/auth/refresh", "/api/v1/auth/refresh",
                    "/auth/forgot-password", "/api/v1/auth/forgot-password",
                    "/auth/reset-password", "/api/v1/auth/reset-password"
                ).permitAll()
                // OpenAPI / Swagger endpoints
                .requestMatchers(
                    "/api-docs/**", "/api/v1/api-docs/**",
                    "/swagger-ui/**", "/api/v1/swagger-ui/**",
                    "/swagger-ui.html", "/api/v1/swagger-ui.html"
                ).permitAll()
                // Public read-only endpoints for communities
                .requestMatchers(org.springframework.http.HttpMethod.GET,
                    "/communities", "/api/v1/communities",
                    "/communities/**", "/api/v1/communities/**"
                ).permitAll()
                // Any other request must be fully authenticated
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';"))
                .frameOptions(frame -> frame.deny())
            )
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
