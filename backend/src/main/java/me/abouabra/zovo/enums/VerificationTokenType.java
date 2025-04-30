package me.abouabra.zovo.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents types of verification tokens used for email communication.
 * <p>
 * Each token type is associated with a specific email template, such as
 * confirming email, resetting passwords, or sending welcome messages.
 * </p>
 *
 * <ul>
 *   <li><b>CONFIRM_EMAIL</b>: Email confirmation token.</li>
 *   <li><b>PASSWORD_RESET</b>: Password reset token.</li>
 *   <li><b>WELCOME_EMAIL</b>: Welcome email token.</li>
 * </ul>
 */
public enum VerificationTokenType {
    CONFIRM_EMAIL("email/confirm-email"),
    PASSWORD_RESET("email/password-reset"),
    WELCOME_EMAIL("email/welcome"),
    TWO_FACTOR_AUTH_ENABLED("email/two-fa-status"),
    TWO_FACTOR_AUTH_DISABLED("email/two-fa-status");

    private final String templateName;

    /**
     * Constructs a VerificationTokenType with the specified email template name.
     *
     * @param templateName the name of the email template associated with this token type
     */
    VerificationTokenType(String templateName) {
        this.templateName = templateName;
    }

    /**
     * Generates dynamic email template data based on the verification token type.
     * <p>
     * The method constructs the subject, endpoint URL, and template variables
     * for different email scenarios (e.g., confirmation, reset, welcome).
     * </p>
     *
     * @param UUIDToken the unique token to append to the email URL.
     * @param baseURL   the base URL of the application used to construct links.
     * @return an instance of {@link EmailTemplateData} containing the template name, subject, and variables.
     * @throws IllegalArgumentException if the token type is unsupported.
     */
    public EmailTemplateData getEmailTemplateData(String UUIDToken, String baseURL) {
        Map<String, Object> variables = new HashMap<>();
        String subject;
        String endpointURL;

        switch (this) {
            case CONFIRM_EMAIL:
                subject = "Confirm Your Email Address";
                endpointURL = baseURL + "/api/v1/auth/confirm-email?token=" + UUIDToken;
                variables.put("confirmationURL", endpointURL);
                break;
            case PASSWORD_RESET:
                subject = "Password Reset Request";
                endpointURL = baseURL + "/api/v1/auth/password-reset?token=" + UUIDToken;
                variables.put("passwordResetURL", endpointURL);
                break;
            case WELCOME_EMAIL:
                subject = "Welcome to Our Platform";
                endpointURL = baseURL + "/api/v1/auth/login";
                variables.put("loginURL", endpointURL);
                break;
            case TWO_FACTOR_AUTH_ENABLED:
                subject = "2FA Has Been Activated";
                endpointURL = baseURL + "/api/v1/auth/login";
                variables.put("loginURL", endpointURL);
                variables.put("title", "2FA Has Been Activated.");
                variables.put("description", "Two-factor authentication has been enabled for your account. This adds an extra layer of security by requiring both your password and a verification code when logging in.");
                break;
            case TWO_FACTOR_AUTH_DISABLED:
                subject = "2FA Has Been Disabled";
                endpointURL = baseURL + "/api/v1/auth/login";
                variables.put("loginURL", endpointURL);
                variables.put("title", "2FA Has Been Disabled.");
                variables.put("description", "Two-factor authentication has been disabled for your account. You will now log in using only your password.");
                break;

            default:
                throw new IllegalArgumentException("Unexpected VerificationTokenType: " + this);
        }

        return new EmailTemplateData(this.templateName, subject, variables);
    }

    /**
     * Represents the data required for rendering an email template.
     * <p>
     * Encapsulates the template name, email subject, and a set of variables
     * used for dynamic content generation within the email body.
     * </p>
     */
    @Getter
    public static class EmailTemplateData {
        private final String templateName;
        private final String subject;
        private final Map<String, Object> variables;

        public EmailTemplateData(String templateName, String subject, Map<String, Object> variables) {
            this.templateName = templateName;
            this.subject = subject;
            this.variables = variables;
        }

    }
}
