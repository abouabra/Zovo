package me.abouabra.zovo.services.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
@Data
public class ChannelMessage {
    private UUID id;
    private String content;
    private ZonedDateTime timestamp;
    private Sender sender;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Sender {
        private Long id;
        private String username;
        private String avatar;
        private String status;
    }
}