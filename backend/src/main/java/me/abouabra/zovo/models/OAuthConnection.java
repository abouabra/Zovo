package me.abouabra.zovo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "oauth_connections")
public class OAuthConnection implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    private String email;

    private String name;

    @Column(name = "created_at", insertable = false, updatable = false)
    private ZonedDateTime createdAt;
}