package me.abouabra.zovo.repositories;

import io.lettuce.core.dynamic.annotation.Param;
import me.abouabra.zovo.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query(
            value = """
                        SELECT *
                        FROM messages m
                        WHERE m.id IN (
                            SELECT DISTINCT ON (m2.channel_id) m2.id
                            FROM messages m2
                            WHERE m2.channel_id IN (:channelIds)
                            ORDER BY m2.channel_id, m2.timestamp DESC
                        )
                    """,
            nativeQuery = true
    )
    List<Message> findLatestMessagesByChannelIds(@Param("channelIds") List<UUID> channelIds);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.channel.id = :channelId ORDER BY m.timestamp ASC")
    List<Message> findAllByChannel_Id(@Param("channelId") UUID channelId);
}
