package com.decisionhub.service.impl;

import com.decisionhub.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService that currently logs reset URLs for development/verification.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendPasswordResetEmail(String email, String resetUrl) {
        // Log the generated reset URL as required for the current phase.
        log.info("PASSWORD RESET REQUESTED - An email would be sent to: {} containing the link: {}", email, resetUrl);
    }
}
