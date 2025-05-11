package me.abouabra.zovo.mappers;

import me.abouabra.zovo.dtos.MessageDTO;
import me.abouabra.zovo.models.Channel;
import me.abouabra.zovo.models.Message;
import me.abouabra.zovo.models.User;
import me.abouabra.zovo.repositories.ChannelRepository;
import me.abouabra.zovo.repositories.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class MessageMapper {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ChannelRepository channelRepository;

    @Mappings({
            @Mapping(source = "channelId", target = "channel", qualifiedByName = "mapChannel"),
            @Mapping(source = "sender.id", target = "sender", qualifiedByName = "mapSender"),
            @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())"),
            @Mapping(target = "timestamp", ignore = true) // DB generated
    })
    public abstract Message toEntity(MessageDTO dto);

    @Named("mapSender")
    protected User mapSender(Long senderId) {
        return userRepository.findById(senderId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + senderId));
    }

    @Named("mapChannel")
    protected Channel mapChannel(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));
    }
}
