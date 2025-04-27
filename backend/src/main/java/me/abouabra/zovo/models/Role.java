package me.abouabra.zovo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;


/**
 * <p>The <code>Role</code> class represents a role entity used for managing authorities
 * within the system.</p>
 *
 * <p>This class is annotated as a JPA entity and is mapped to the "roles" table.
 * Each <code>Role</code> instance defines a specific authority that can be granted
 * to users in the application.</p>
 *
 * <ul>
 *   <li>The <code>id</code> field uniquely identifies the role.</li>
 *   <li>The <code>name</code> field specifies the role name and serves as its authority.</li>
 * </ul>
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

    @Override
    public String getAuthority() {
        return name;
    }
}
