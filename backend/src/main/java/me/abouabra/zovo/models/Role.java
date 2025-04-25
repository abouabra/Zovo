package me.abouabra.zovo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>The <code>Role</code> class represents a role entity in the system and implements the
 * <code>GrantedAuthority</code> interface from Spring Security.</p>
 *
 * <p>This class is annotated as a JPA entity and is mapped to the "roles" table in the database.
 * It serves as part of the relationship between users and system-level permissions.</p>
 *
 * <ul>
 *   <li>Each <code>Role</code> is uniquely identified by an auto-generated <code>id</code>.</li>
 *   <li>Contains a <code>name</code> property indicating the role name, which must not be null.</li>
 *   <li>Has a many-to-many relationship with the <code>User</code> entity, enabling associations
 *   between users and roles.</li>
 * </ul>
 *
 * <p>The many-to-many relationship with the <code>User</code> entity uses the EAGER fetch type,
 * indicating related users will be fetched along with the role. This is handled using the "roles"
 * field mapped by the <code>User</code> class.</p>
 *
 * <p>By implementing the <code>GrantedAuthority</code> interface, the <code>Role</code> class
 * integrates with Spring Security, providing the necessary authority information for user
 * authentication and authorization processes.</p>
 *
 * <p>The <code>getAuthority</code> method overrides the default implementation from
 * <code>GrantedAuthority</code> and returns the name of the role as its authority string.</p>
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @Override
    public String getAuthority() {
        return name;
    }
}
