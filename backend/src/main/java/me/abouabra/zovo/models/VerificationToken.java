package me.abouabra.zovo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.abouabra.zovo.enums.VerificationTokenType;

import java.io.Serializable;
import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "verification_tokens", uniqueConstraints = {
        @UniqueConstraint(columnNames = "token"),
})
public class VerificationToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationTokenType type;

    @Column(name = "\"expiredAt\"",columnDefinition = "timestamptz", updatable = false)
    private ZonedDateTime expiredAt;
}
