package me.abouabra.zovo.repositories;

import me.abouabra.zovo.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * <p>The <code>RoleRepository</code> interface provides CRUD operations and custom query methods
 * for managing <code>Role</code> entities in the system.</p>
 *
 * <p>This repository is responsible for retrieving <code>Role</code> data from the underlying
 * database and supports pagination and sorting through inheritance of <code>JpaRepository</code>.</p>
 *
 * <ul>
 *   <li>Supports standard CRUD operations for the <code>Role</code> entity.</li>
 *   <li>Provides a custom method to find roles by their name.</li>
 * </ul>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String roleUser);
}
