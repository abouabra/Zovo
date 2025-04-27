package me.abouabra.zovo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>The <code>User</code> class represents an application user entity.</p>
 *
 * <p>This class is mapped to the "users" table and includes the following:</p>
 * <ul>
 *   <li>Unique <code>username</code> and <code>email</code> fields.</li>
 *   <li><code>password</code> field, excluded from JSON serialization.</li>
 *   <li><code>createdAt</code> field for timestamp management.</li>
 *   <li>Association with <code>Role</code> entities to define user roles.</li>
 * </ul>
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
