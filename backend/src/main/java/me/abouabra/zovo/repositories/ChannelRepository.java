package me.abouabra.zovo.repositories;

import me.abouabra.zovo.models.Channel;
import me.abouabra.zovo.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {
    // fetch all channels where the given user is a member
    @Query("SELECT c FROM Channel c JOIN c.members m WHERE m.id = :userId")
    List<Channel> findAllByMemberId(@Param("userId") long userId);

    // fetch channels with members eagerly, so we don’t trigger lazy loads later
    @EntityGraph(attributePaths = "members")
    @Query("SELECT c FROM Channel c JOIN c.members m WHERE m.id = :userId")
    List<Channel> findAllWithMembersByMemberId(@Param("userId") long userId);

    @EntityGraph(attributePaths = "members")
    List<Channel> findTop10ByTypeAndNameContainingIgnoreCase(String group, String keyword);

    @EntityGraph(attributePaths = "members")
    List<Channel> findTop10ByTypeAndNameContainingIgnoreCaseAndMembers_Id(String type, String keyword, Long userId);
}

