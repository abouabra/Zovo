package me.abouabra.zovo.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.enums.VerificationTokenType;
import me.abouabra.zovo.enums.VerificationTokenType.EmailTemplateData;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Service class for handling email-related operations.
 * <p>
 * This class is responsible for preparing and sending emails, managing templates,
 * and providing asynchronous email delivery.
 * </p>
 */
@Service
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final String senderEmail;
    private final String emailDisplayName;
    private final String baseURL;
    private final TemplateEngine templateEngine;
    private final AvatarStorageService avatarStorageService;

    /**
     * Constructs an EmailService with the required dependencies.
     *
     * @param javaMailSender The JavaMailSender instance used for sending emails.
     * @param senderEmail The sender's email address.
     * @param emailDisplayName The display name for the sender email address.
     * @param baseURL The base URL used to construct email links.
     * @param templateEngine The TemplateEngine instance used for email template processing.
     */
    public EmailService(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String senderEmail,
            @Value("${spring.mail.display-name}") String emailDisplayName,
            @Value("${app.base-url}") String baseURL,
            TemplateEngine templateEngine, AvatarStorageService avatarStorageService) {
        this.javaMailSender = javaMailSender;
        this.senderEmail = senderEmail;
        this.emailDisplayName = emailDisplayName;
        this.baseURL = baseURL;
        this.templateEngine = templateEngine;
        this.avatarStorageService = avatarStorageService;
    }

    /**
     * Preloads and processes email templates for all {@link VerificationTokenType} values.
     * <p>
     * This method ensures that email templates are compiled and ready to use by
     * processing dummy data through the template engine.
     * </p>
     * <p>
     * Logs any errors encountered during the preloading process.
     * </p>
     */
    public void preloadTemplates() {
        try {
            for (VerificationTokenType tokenType : VerificationTokenType.values()) {
                String dummyToken = "dummy-token";
                String logoURl = avatarStorageService.getAvatarUrl("logo.png");
                EmailTemplateData templateData = tokenType.getEmailTemplateData(dummyToken, baseURL, logoURl);
                String templateName = templateData.getTemplateName();

                Context context = new Context();
                context.setVariables(Map.of("dummyVar", "placeholder"));
                templateEngine.process(templateName, context);
                log.debug("Preloaded template: {}", templateName);
            }
        } catch (Exception e) {
            log.error("Error preloading templates", e);
        }
    }

    /**
     * Asynchronously sends an email based on the provided token type and unique token.
     * <p>
     * If the email fails to send, an error is logged.
     * </p>
     *
     * @param recipientEmail the recipient's email address
     * @param verificationTokenType the type of verification token associated with the email
     * @param UUIDToken the unique token used for email interactions
     */
    @Async("emailExecutor")
    public void sendMailAsync(String recipientEmail, VerificationTokenType verificationTokenType, String UUIDToken) {
        boolean success = sendMail(recipientEmail, verificationTokenType, UUIDToken);
        if (!success) {
            log.error("Failed to send {} email to {}", verificationTokenType, recipientEmail);
        }
    }

    /**
     * Sends an email to the specified recipient based on the provided verification token type and unique token.
     *
     * @param recipientEmail the email address of the recipient.
     * @param verificationTokenType the type of verification token determining the email template.
     * @param UUIDToken the unique token to include in the email content or URL.
     * @return {@code true} if the email was sent successfully, {@code false} otherwise.
     */
    public boolean sendMail(String recipientEmail, VerificationTokenType verificationTokenType, String UUIDToken) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(new InternetAddress(senderEmail, emailDisplayName, "UTF-8"));
            helper.setTo(recipientEmail);

            String logoURl = avatarStorageService.getAvatarUrl("logo.png");
            EmailTemplateData templateData = verificationTokenType.getEmailTemplateData(UUIDToken, baseURL, logoURl);
            helper.setSubject(templateData.getSubject());

            Context context = new Context();
            context.setVariables(templateData.getVariables());

            String htmlContent = templateEngine.process(templateData.getTemplateName(), context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error sending email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

}