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
 * <p>Handles operations for generating, validating, and deleting verification tokens
 * in association with {@link User} entities. Implements expiration logic and manages
 * token persistence through the repository.</p>
 */
@Slf4j
@Service
@AllArgsConstructor
public class VerificationTokenService {
    private static final long EXPIRATION_TIME_IN_MINUTES = 20;
    private final VerificationTokenRepository tokenRepository;


    /**
     * Generates a verification token for the specified user and token type.
     * <p>
     * If a token of the given type already exists for the user, it updates the token
     * and the expiration date. Otherwise, it creates a new token.
     * </p>
     *
     * @param user the user for whom the verification token is generated
     * @param tokenType the type of the verification token
     * @return the generated unique verification token as a {@code String}
     */
    public String generateVerificationToken(User user, VerificationTokenType tokenType) {
        String uuidToken = UUID.randomUUID().toString();
        ZonedDateTime expiryDate = ZonedDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES);

        Optional<VerificationToken> existingToken = tokenRepository.findByUserAndType(user, tokenType);

        if (existingToken.isPresent()) {
            VerificationToken token = existingToken.get();
            token.setToken(uuidToken);
            token.setExpiredAt(expiryDate);
            tokenRepository.save(token);
        } else {
            VerificationToken verificationToken = new VerificationToken(
                    null,
                    uuidToken,
                    user,
                    tokenType,
                    expiryDate
            );
            tokenRepository.save(verificationToken);
        }

        return uuidToken;
    }


    /**
     * Validates a given verification token based on its type and expiration.
     *
     * @param uuidToken the unique identifier of the verification token.
     * @param tokenType the type of the token to validate.
     * @return an {@link Optional} containing the valid {@link VerificationToken}, or empty if invalid or expired.
     */
    public Optional<VerificationToken> validateToken(String uuidToken, VerificationTokenType tokenType) {
        return tokenRepository.findValidToken(uuidToken, tokenType, ZonedDateTime.now());
    }


    /**
     * Deletes all verification tokens associated with the specified user.
     *
     * <p>This method removes all tokens linked to the provided user from the repository.</p>
     *
     * @param user the {@link User} whose tokens are to be deleted, must not be null.
     */
    @Transactional
    public void deleteTokenByUser(User user) {
        tokenRepository.deleteByUser(user);
    }
}