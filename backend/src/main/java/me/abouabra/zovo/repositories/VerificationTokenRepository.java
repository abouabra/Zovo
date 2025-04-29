package me.abouabra.zovo.repositories;

import me.abouabra.zovo.models.User;
import me.abouabra.zovo.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);

    List<VerificationToken> findByUser(User user);
}
