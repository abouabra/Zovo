package me.abouabra.zovo.dtos;

import lombok.*;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private String content;
    private UUID channelId;
    private MessageSender sender;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageSender {
        private Long id;
        private String username;
        private String avatar;
        private String status;
    }
}
