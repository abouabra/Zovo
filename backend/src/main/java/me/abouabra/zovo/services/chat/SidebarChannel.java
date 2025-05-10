package me.abouabra.zovo.services.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
public class SidebarChannel {
    private UUID id;
    private String type; // Consider using an enum 
    private String name;
    private String avatar; // Already resolved avatar URL (not just avatarKey)
    private String status; // Optional: online, offline, or "" – consider enum
    private Integer unread; // nullable
    private Integer members; // nullable
    private LastMessage lastMessage; // encapsulated message object


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LastMessage {
        private String content;
        private ZonedDateTime timestamp;
    }
}

