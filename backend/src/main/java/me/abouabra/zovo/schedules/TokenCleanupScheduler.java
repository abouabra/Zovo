package me.abouabra.zovo.schedules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.repositories.VerificationTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * Schedules and performs cleanup of expired verification tokens from the database.
 * <p>
 * This class uses a scheduled task to periodically remove expired tokens,
 * improving database performance and ensuring outdated tokens are not retained.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final VerificationTokenRepository tokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanExpiredTokens() {
        ZonedDateTime now = ZonedDateTime.now();
        tokenRepository.deleteAllExpiredSince(now);
        log.debug("Cleaned up expired verification tokens");
    }
}
