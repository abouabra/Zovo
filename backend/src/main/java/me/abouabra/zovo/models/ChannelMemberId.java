package me.abouabra.zovo.models;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Embeddable
public class ChannelMemberId implements Serializable {
    @Column(name = "channel_id", columnDefinition = "uuid")
    private UUID channelId;

    @Column(name = "user_id")
    private Long userId;

    public ChannelMemberId() {}

    public ChannelMemberId(UUID channelId, Long userId) {
        this.channelId = channelId;
        this.userId = userId;
    }

    // getters, setters, equals & hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelMemberId that)) return false;
        return Objects.equals(channelId, that.channelId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, userId);
    }
}
