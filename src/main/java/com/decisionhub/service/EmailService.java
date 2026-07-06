package com.decisionhub.service;

/**
 * Service handling email notifications.
 */
public interface EmailService {

    /**
     * Sends a password reset instruction link to the specified email address.
     *
     * @param email    the recipient's email address
     * @param resetUrl the password reset web link
     */
    void sendPasswordResetEmail(String email, String resetUrl);
}
