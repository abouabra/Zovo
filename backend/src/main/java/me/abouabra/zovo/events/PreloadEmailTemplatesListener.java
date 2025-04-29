package me.abouabra.zovo.events;

import me.abouabra.zovo.services.EmailService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener responsible for preloading email templates upon application startup.
 * <p>
 * Utilizes {@link EmailService} to ensure templates are processed and ready for use,
 * improving application runtime performance where email operations are required.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PreloadEmailTemplatesListener {

    private final EmailService emailService;

    /**
     * Preloads email templates during application startup.
     * <p>
     * This method invokes {@code emailService.preloadTemplates()} to ensure that
     * email templates are compiled and ready to use. It is triggered when the
     * application is fully initialized.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void preloadEmailTemplates() {
        log.info("Preloading email templates at application startup");
        emailService.preloadTemplates();
    }
}