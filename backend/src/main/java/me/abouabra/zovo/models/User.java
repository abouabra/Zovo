package me.abouabra.zovo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;




/**
 * <p>The <code>User</code> class represents the user entity within the system.</p>
 *
 * <p>This entity is mapped to the "users" table in the database and contains fields
 * such as username, email, password, roles, and account status attributes.</p>
 *
 * <ul>
 *   <li>Each user has a unique identifier (<code>id</code>), username, and email.</li>
 *   <li>Password is securely managed and ignored in JSON serialization.</li>
 *   <li>Relationships with roles are defined using a many-to-many association.</li>
 * </ul>
 */
@Entity
@Getter
@Setter
@ToString
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

    @Column(nullable = false, name = "two_fa_enabled")
    private boolean twoFactorEnabled;

    @Column(name = "two_fa_secret")
    private String twoFactorSecret;

    @Column(name = "two_fa_recovery_codes")
    private String twoFactorRecoveryCodes;
}
