package me.abouabra.zovo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;



/**
 * <p>The <code>User</code> class represents a system user entity.</p>
 *
 * <p>This class is a JPA entity mapped to the "users" table with fields for
 * storing key user details such as username, email, and password. It also includes
 * user roles, status, and timestamp information.</p>
 *
 * <ul>
 *   <li>Supports unique constraints on username and email.</li>
 *   <li>Links to roles using a Many-to-Many relationship.</li>
 *   <li>Manages user activity & enablement status.</li>
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

    @Column(nullable = false, name = "\"isActive\"")
    private boolean isActive = false;

    @Column(nullable = false, name = "\"isEnabled\"")
    private boolean isEnabled = false;
}
