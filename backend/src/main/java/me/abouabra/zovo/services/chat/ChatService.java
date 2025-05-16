package me.abouabra.zovo.services.chat;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.dtos.MessageDTO;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.mappers.MessageMapper;
import me.abouabra.zovo.models.Channel;
import me.abouabra.zovo.models.ChannelMember;
import me.abouabra.zovo.models.Message;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.ChannelMemberRepository;
import me.abouabra.zovo.repositories.ChannelRepository;
import me.abouabra.zovo.repositories.MessageRepository;
import me.abouabra.zovo.repositories.UserRepository;
import me.abouabra.zovo.services.storage.AvatarStorageService;
import me.abouabra.zovo.utils.ApiResponse;
import me.abouabra.zovo.utils.AvatarGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.directory.SearchResult;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@AllArgsConstructor
public class ChatService {
    private final UserRepository userRepo;
    private final ChannelRepository channelRepo;
    private final MessageRepository messageRepo;
    private final AvatarStorageService avatarStorageService;
    private final AvatarGenerator avatarGenerator;
    private final ChannelMemberRepository channelMemberRepo;
    private SimpMessagingTemplate messagingTemplate;
    private MessageMapper messageMapper;

    @Transactional(readOnly = true)
    public ResponseEntity<? extends ApiResponse<?>> getSidebarData(User loggedInUser) {
        Long userId = loggedInUser.getId();

        List<Channel> channels = channelRepo.findAllWithMembersByMemberId(userId);

        List<UUID> channelIds = channels.stream()
                .map(Channel::getId)
                .toList();
        List<Message> latestMessages = messageRepo.findLatestMessagesByChannelIds(channelIds);
        Map<UUID, Message> latestMap = latestMessages.stream()
                .collect(Collectors.toMap(m -> m.getChannel().getId(), Function.identity()));

        Map<UUID, Long> memberCountMap = channelMemberRepo.countMembersByChannelIds(channelIds).stream()
                .collect(Collectors.toMap(
                        arr -> (UUID) arr[0],
                        arr -> (Long) arr[1]
                ));

        List<SidebarChannel> sidebar = getSidebarChannels(loggedInUser, channels, latestMap, memberCountMap);

        return ApiResponse.success(sidebar);
    }

    private List<SidebarChannel> getSidebarChannels(User loggedInUser, List<Channel> channels, Map<UUID, Message> latestMap, Map<UUID, Long> memberCountMap) {
        return channels.stream().map(ch -> {
            String name, avatarKey, status = "";
            if ("personal".equals(ch.getType())) {
                User other = ch.getMembers().stream()
                        .filter(u -> !u.getId().equals(loggedInUser.getId()))
                        .findFirst()
                        .orElse(loggedInUser);
                name = other.getUsername();
                avatarKey = other.getAvatarKey();
                status = other.getStatus();
            } else {
                name = ch.getName();
                avatarKey = ch.getAvatarKey();
            }

            String avatarUrl = avatarStorageService.getAvatarUrl(avatarKey);

            Message m = latestMap != null ? latestMap.get(ch.getId()) : null;
            SidebarChannel.LastMessage last = m == null
                    ? null
                    : new SidebarChannel.LastMessage(m.getContent(), m.getTimestamp());

            return new SidebarChannel(
                    ch.getId(),
                    ch.getType(),
                    name,
                    avatarUrl,
                    status,
                    0,
                    memberCountMap != null ? memberCountMap.getOrDefault(ch.getId(), 0L).intValue() : null,
                    last
            );
        }).toList();
    }

    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> getChannelMessages(UUID channelUUID, User loggedInUser) {
        List<Message> messages = messageRepo.findAllByChannel_Id(channelUUID);
        List<ChannelMessage> channelMessages = messages.stream().map(message -> new ChannelMessage(
                message.getId(),
                message.getContent(),
                message.getTimestamp(),
                channelUUID,
                new ChannelMessage.Sender(
                        message.getSender().getId(),
                        message.getSender().getUsername(),
                        avatarStorageService.getAvatarUrl(message.getSender().getAvatarKey()),
                        message.getSender().getStatus()
                )
        )).toList();
        return ApiResponse.success(channelMessages);
    }

    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> getSearchResult(String keyword, User user) {
        List<Channel> globalSearch = channelRepo.findTop10ByTypeAndNameContainingIgnoreCase("group", keyword);
        List<Channel> personalSearch = channelRepo.findTop10ByTypeAndNameContainingIgnoreCase("personal", keyword);
        List<Channel> combined = Stream.concat(globalSearch.stream(), personalSearch.stream()).toList();

        List<SidebarChannel> searchList = getSidebarChannels(user, combined, null, null);

        return ApiResponse.success(searchList);
    }

    @Transactional
    public void sendMessage(MessageDTO messageDTO) {
        String destination = "/topic/channel." + messageDTO.getChannelId();
        Message recievedMessage = messageMapper.toEntity(messageDTO);
        recievedMessage.setTimestamp(ZonedDateTime.now());
        recievedMessage = messageRepo.save(recievedMessage);

        ChannelMessage channelMessage = new ChannelMessage(
                recievedMessage.getId(),
                recievedMessage.getContent(),
                recievedMessage.getTimestamp(),
                messageDTO.getChannelId(),
                new ChannelMessage.Sender(
                        recievedMessage.getSender().getId(),
                        recievedMessage.getSender().getUsername(),
                        avatarStorageService.getAvatarUrl(recievedMessage.getSender().getAvatarKey()),
                        recievedMessage.getSender().getStatus()
                )
        );

        messagingTemplate.convertAndSend(destination, channelMessage);
    }

    @Transactional
    public ResponseEntity<? extends ApiResponse<?>> joinChannel(User user, UUID channelUUID) {

        Optional<Channel> channelOpt = channelRepo.findById(channelUUID);
        if(channelOpt.isEmpty())
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "No channel Found");
        Channel channel = channelOpt.get();
        channel.getMembers().add(user);
        channelRepo.save(channel);
        return ApiResponse.success("Channel joined");
    }

    public ResponseEntity<? extends ApiResponse<?>> createChannel(User user, String name) {
        if(channelRepo.existsByName(name))
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Channel name already exists");

        Channel channel = new Channel(
                null,
                "group",
                name,
                avatarGenerator.createAvatar(name, true),
                ZonedDateTime.now(),
                Set.of(user)
        );

        channel = channelRepo.save(channel);
        return ApiResponse.success("Channel created", Map.of(
                "id", channel.getId(),
                "avatar", avatarStorageService.getAvatarUrl(channel.getAvatarKey())
        ));
    }
}
