package me.abouabra.zovo.repositories;

import me.abouabra.zovo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;


/**
 * <p>The <code>UserRepository</code> interface provides methods for performing CRUD operations
 * and custom queries on <code>User</code> entities.</p>
 *
 * <p>Extends <code>JpaRepository</code> to include database interaction features with pagination
 * and sorting support.</p>
 *
 * <ul>
 *   <li>Allows retrieval of users by username or email.</li>
 *   <li>Checks user existence based on specific criteria.</li>
 *   <li>Supports custom queries for active and enabled users.</li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username of the user to find. Must not be null.
     * @return an {@link Optional} containing the {@link User} if found, or an empty {@link Optional} if no user exists with the given username.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email address of the user to find.
     * @return an {@link Optional} containing the user if found, or an empty {@link Optional} if not.
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Checks if a user exists by matching the provided username or email.
     *
     * @param username the username to search for.
     * @param email the email to search for.
     * @return <code>true</code> if a user exists with the given username or email; otherwise <code>false</code>.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username OR u.email = :email")
    boolean existsByUsernameOrEmail(String username, String email);

    /**
     * Retrieves an active and enabled user by their email address.
     *
     * @param email the email address of the user to find, must not be null.
     * @return an {@link Optional} containing the active and enabled {@link User} if found, or an empty {@link Optional} if no such user exists.
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isEnabled = true AND u.isActive = true")
    Optional<User> findActiveUserByEmail(String email);

}
