package me.abouabra.zovo.repositories;

import me.abouabra.zovo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * <p>The <code>UserRepository</code> interface manages data persistence and retrieval operations for {@link User} entities.</p>
 *
 * <p>It extends the {@link JpaRepository} interface to provide standard data access methods such as save, delete, and findById.
 * By default, these methods interact with the "users" table mapped by the {@link User} entity.</p>
 *
 * <p>This interface is annotated with <code>@Repository</code>, marking it as a Spring-managed bean. This allows seamless
 * integration with Spring's dependency injection and exception translation mechanisms.</p>
 *
 * <p>Custom query methods:</p>
 * <ul>
 *   <li><code>Optional&lt;User&gt; findUserByUsername(String username)</code>: Retrieves a {@link User} entity by its unique username property.
 *       <ul>
 *           <li>Returns an {@link Optional} containing the {@link User}, or an empty {@link Optional} if no match is found.</li>
 *           <li>This method is useful for scenarios such as user-specific operations, authentication, or user search functionality.</li>
 *       </ul>
 *   </li>
 *   <li><code>Optional&lt;User&gt; findUserByEmail(String email)</code>: Retrieves a {@link User} entity by its unique email property.
 *       <ul>
 *           <li>Returns an {@link Optional} containing the {@link User}, or an empty {@link Optional} if no match is found.</li>
 *           <li>It is commonly employed for tasks like user verification and email-based account management.</li>
 *       </ul>
 *   </li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);

}
