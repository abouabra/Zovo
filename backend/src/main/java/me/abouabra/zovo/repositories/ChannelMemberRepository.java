package me.abouabra.zovo.repositories;

import me.abouabra.zovo.models.ChannelMember;
import me.abouabra.zovo.models.ChannelMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {
    @Query("""
      SELECT cm.channel.id, COUNT(cm.user.id)
      FROM ChannelMember cm
      WHERE cm.channel.id IN :channelIds
      GROUP BY cm.channel.id
    """)
    List<Object[]> countMembersByChannelIds(@Param("channelIds") List<UUID> channelIds);
}
