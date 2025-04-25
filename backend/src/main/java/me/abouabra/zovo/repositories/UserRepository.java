package me.abouabra.zovo.repositories;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import me.abouabra.zovo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<Object> findUserByUsername(@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores") @NotBlank(message = "Username cannot be blank") @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters") String username);

    Optional<Object> findUserByEmail(@NotBlank(message = "Email cannot be blank") @Email(message = "Invalid email format") String email);
}
