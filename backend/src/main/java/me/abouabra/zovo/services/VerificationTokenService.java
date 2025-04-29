package me.abouabra.zovo.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.enums.VerificationTokenType;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.models.VerificationToken;
import me.abouabra.zovo.repositories.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>Service class for managing verification tokens.</p>
 *
 * <p>Responsible for creating, validating, and deleting tokens used for
 * user verification processes. Ensures token uniqueness, expiration handling,
 * and token association to users.</p>
 */
@Slf4j
@Service
@AllArgsConstructor
public class VerificationTokenService {
    private static final long EXPIRATION_TIME_IN_MINUTES = 20;
    private final VerificationTokenRepository tokenRepository;


    /**
     * Generates a new verification token for the specified user and token type.
     * <p>
     * Deletes any existing tokens associated with the user before creating a new one.
     *
     * @param user the user for whom the verification token is generated.
     * @param tokenType the type of the verification token to be created.
     * @return the newly generated token as a string.
     */
    @Transactional
    public String generateVerificationToken(User user, VerificationTokenType tokenType) {
        String uuidToken = UUID.randomUUID().toString();
        ZonedDateTime expiryDate = ZonedDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES);

        tokenRepository.deleteAll(tokenRepository.findByUser(user));

        VerificationToken verificationToken = new VerificationToken(
                null,
                uuidToken,
                user,
                tokenType,
                expiryDate
        );
        tokenRepository.save(verificationToken);
        log.debug("Generated {} token for user {}, expires at {}", tokenType, user.getEmail(), expiryDate);

        return uuidToken;
    }


    /**
     * Validates a verification token based on its UUID and type.
     * <p>
     * Ensures that the token exists, has not expired, and matches the expected type.
     *
     * @param uuidToken the unique identifier of the token to validate.
     * @param tokenType the expected type of the verification token.
     * @return an {@code Optional} containing the valid {@code VerificationToken}, or empty if invalid.
     */
    @Transactional(readOnly = true)
    public Optional<VerificationToken> validateToken(String uuidToken, VerificationTokenType tokenType) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(uuidToken);

        if (tokenOpt.isEmpty()) {
            log.error("### - Token not found: {}", uuidToken);
            return Optional.empty();
        }

        VerificationToken token = tokenOpt.get();

        if (ZonedDateTime.now().isAfter(token.getExpiredAt())) {
            log.error("### - Token expired: {}", uuidToken);
            return Optional.empty();
        }

        if (token.getType() != tokenType) {
            log.error("### - Token type mismatch. Expected: {}, Found: {}", tokenType, token.getType());
            return Optional.empty();
        }

        return tokenOpt;
    }


    /**
     * Deletes all verification tokens associated with the specified user.
     *
     * @param user the user whose tokens are to be deleted.
     */
    @Transactional
    public void deleteTokenByUser(User user) {
        tokenRepository.deleteAll(tokenRepository.findByUser(user));
        log.debug("Deleted all tokens for user: {}", user.getEmail());
    }
}