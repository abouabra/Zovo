package me.abouabra.zovo.repositories;

import me.abouabra.zovo.enums.VerificationTokenType;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {

    /**
     * Retrieves a valid verification token based on the provided parameters.
     *
     * @param token the token string to be searched.
     * @param tokenType the type of the verification token.
     * @param now the current date and time to check for token expiration.
     * @return an {@link Optional} containing the valid {@link VerificationToken}, or empty if none is found.
     */
    @Query("SELECT t FROM VerificationToken t WHERE t.token = :token AND t.type = :tokenType AND t.expiredAt > :now")
    Optional<VerificationToken> findValidToken(String token, VerificationTokenType tokenType, ZonedDateTime now);

    /**
     * Retrieves a {@link VerificationToken} based on the associated {@link User} and token type.
     *
     * @param user the user associated with the token.
     * @param tokenType the type of verification token to be retrieved.
     * @return an {@link Optional} containing the matching {@link VerificationToken}, or empty if none found.
     */
    @Query("SELECT t FROM VerificationToken t WHERE t.user = :user AND t.type = :tokenType")
    Optional<VerificationToken> findByUserAndType(User user, VerificationTokenType tokenType);

    /**
     * Deletes all verification tokens associated with the given user.
     *
     * @param user the {@link User} whose tokens are to be deleted, must not be null.
     */
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.user = :user")
    void deleteByUser(User user);
}
