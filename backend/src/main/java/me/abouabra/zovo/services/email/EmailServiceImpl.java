package me.abouabra.zovo.services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import me.abouabra.zovo.enums.VerificationTokenType;
import me.abouabra.zovo.enums.VerificationTokenType.EmailTemplateData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Implementation of the EmailService facilitating the sending of dynamic template-based emails.
 * <p>
 * Utilizes a template engine for rendering email content and supports configuration of sender details,
 * email templates, and application-specific URLs.
 * </p>
 */
@Service
@Data
public class EmailServiceImpl implements EmailService {
    private JavaMailSender javaMailSender;
    private String senderEmail;
    private String emailDisplayName;
    private TemplateEngine templateEngine;
    private String baseURL;


    /**
     * Service implementation for sending emails with dynamic templates.
     * <p>
     * Configures email sender, display name, base URL, and template processing engine.
     * </p>
     *
     * @param javaMailSender   the JavaMailSender instance for sending emails
     * @param senderEmail      the sender's email address
     * @param emailDisplayName the name displayed as email sender
     * @param baseURL          the base URL for application links
     * @param templateEngine   the template engine used for rendering email templates
     */
    public EmailServiceImpl(JavaMailSender javaMailSender,
                            @Value("${spring.mail.username}") String senderEmail,
                            @Value("${spring.mail.display-name}") String emailDisplayName,
                            @Value("${app.base-url:http://localhost:8080}") String baseURL,
                            TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.senderEmail = senderEmail;
        this.emailDisplayName = emailDisplayName;
        this.templateEngine = templateEngine;
        this.baseURL = baseURL;
    }

    /**
     * Sends an email to a specified recipient using a predefined template and token.
     *
     * @param recipientEmail The recipient's email address.
     * @param verificationTokenType The type of verification token to determine the email template.
     * @param UUIDToken The unique token used to generate dynamic email content.
     * @return {@code true} if the email was sent successfully, otherwise {@code false}.
     */
    @Override
    public boolean sendMail(String recipientEmail, VerificationTokenType verificationTokenType, String UUIDToken) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(new InternetAddress(senderEmail, emailDisplayName, "UTF-8"));

            EmailTemplateData templateData = verificationTokenType.getEmailTemplateData(UUIDToken, baseURL);
            String templateName = templateData.getTemplateName();
            String subject = templateData.getSubject();
            Map<String, Object> variables = templateData.getVariables();

            helper.setTo(recipientEmail);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            return false;
        }
    }

}