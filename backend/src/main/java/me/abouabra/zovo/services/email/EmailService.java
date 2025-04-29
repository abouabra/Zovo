package me.abouabra.zovo.services.email;

import me.abouabra.zovo.enums.VerificationTokenType;

/**
 * A service interface for sending email messages with support for dynamic templates.
 * <p>
 * Provides methods for constructing and delivering emails based on specific verification tokens
 * and application-defined templates.
 * </p>
 */
public interface EmailService {
    boolean sendMail(String recipientEmail, VerificationTokenType verificationTokenType, String uuidToken);
}
