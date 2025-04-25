package me.abouabra.zovo.repositories;

import me.abouabra.zovo.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * <p>The <code>RoleRepository</code> interface handles data persistence and retrieval operations for {@link Role} entities.</p>
 *
 * <p>It extends the {@link JpaRepository} interface, which provides standard data access methods (e.g., save, delete, findById).
 * By default, <code>JpaRepository</code> methods interact with the "roles" table mapped by the {@link Role} entity.</p>
 *
 * <p>This repository is marked as a Spring-managed <code>@Repository</code> bean, enabling integration with Spring's dependency
 * injection and custom exception translation mechanisms.</p>
 *
 * <p>Additional custom query methods:</p>
 * <ul>
 *     <li><code>Optional&lt;Role&gt; findByName(String roleUser)</code>: Retrieves a {@link Role} entity based on its unique name property.
 *         <ul>
 *             <li>Returns an {@link Optional} containing the {@link Role}, or an empty {@link Optional} if no match is found.</li>
 *             <li>Can be used to retrieve specific roles like <code>ROLE_USER</code>, <code>ROLE_ADMIN</code>, etc.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p>This repository is typically used in service layers such as {@link me.abouabra.zovo.services.AuthService} and
 * {@link me.abouabra.zovo.services.RoleService} to manage roles, including assigning roles to users or retrieving roles
 * for authentication and authorization purposes within the system.</p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String roleUser);
}
