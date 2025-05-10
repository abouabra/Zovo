package me.abouabra.zovo.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "channels", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Channel implements Serializable {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String type; // "personal" or "group"

    @Column(nullable = false)
    private String name;

    @Column(name = "avatar_key")
    private String avatarKey;

    @Column(name = "created_at", columnDefinition = "timestamptz", insertable = false, updatable = false)
    private ZonedDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "channel_members",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
}
