package me.abouabra.zovo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>The <code>User</code> class represents a user entity in the system.</p>
 *
 * <p>This class is annotated as a JPA entity and is mapped to the "users" table in the database.
 * It serves as the main representation of application users and supports essential user attributes
 * and relationships.</p>
 *
 * <ul>
 *   <li>The <code>id</code> field is a unique identifier for each user, auto-generated using
 *   <i>GenerationType.IDENTITY</i>.</li>
 *   <li>The <code>username</code> and <code>email</code> are unique, mandatory fields, constrained
 *   by <code>UniqueConstraint</code> definitions to ensure uniqueness in the database.</li>
 *   <li>A <code>password</code> field is used to securely store user passwords, which is hidden
 *   from serialization using <code>@JsonIgnore</code>.</li>
 *   <li>The <code>createdAt</code> field is timestamped and provides information about the
 *   creation date and time of the user entity.</li>
 *   <li>This class establishes a many-to-many relationship with the <code>Role</code> entity,
 *   allowing the assignment of multiple roles to a user and vice versa.</li>
 * </ul>
 *
 * <p>The relationship with the <code>Role</code> entity is maintained with a junction table
 * named "users_roles." This table's join columns ensure accurate mapping of the associations
 * between users and roles.</p>
 *
 * <p>Helper methods <code>addRole(Role role)</code> and <code>removeRole(Role role)</code>
 * are provided for managing roles associated with a user, ensuring bidirectional consistency
 * within the many-to-many relationship.</p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private  String password;

    @Column(name = "\"createdAt\"",columnDefinition = "timestamptz", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }
}
