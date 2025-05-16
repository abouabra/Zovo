package me.abouabra.zovo.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.abouabra.zovo.dtos.MessageDTO;
import me.abouabra.zovo.enums.ApiCode;
import me.abouabra.zovo.security.UserPrincipal;
import me.abouabra.zovo.services.chat.ChatService;
import me.abouabra.zovo.utils.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@AllArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/sidebar")
    public ResponseEntity<? extends ApiResponse<?>> getSidebarData(@AuthenticationPrincipal UserPrincipal loggedInUser) {
        return chatService.getSidebarData(loggedInUser.getUser());
    }

    @GetMapping("/messages/{channelUUID}")
    public ResponseEntity<? extends ApiResponse<?>> getChannelMessages(@AuthenticationPrincipal UserPrincipal loggedInUser, @PathVariable UUID channelUUID) {
        return chatService.getChannelMessages(channelUUID, loggedInUser.getUser());
    }

    @GetMapping("/search")
    public ResponseEntity<? extends ApiResponse<?>> getSearchResult(@RequestParam String keyword, @AuthenticationPrincipal UserPrincipal loggedInUser) {
        return chatService.getSearchResult(keyword, loggedInUser.getUser());
    }

    @PostMapping("/join/{channelUUID}")
    public ResponseEntity<? extends ApiResponse<?>> joinChannel(@AuthenticationPrincipal UserPrincipal loggedInUser, @PathVariable UUID channelUUID) {
        return chatService.joinChannel(loggedInUser.getUser(), channelUUID);
    }

    @PostMapping("/create")
    public ResponseEntity<? extends ApiResponse<?>> createChannel(@AuthenticationPrincipal UserPrincipal loggedInUser, @RequestBody Map<String, String> body) {
        if (body == null || body.isEmpty() || !body.containsKey("name"))
            return ApiResponse.failure(ApiCode.BAD_REQUEST, "Missing channel name");
        return chatService.createChannel(loggedInUser.getUser(), body.get("name"));
    }











    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO messageDTO) {
        chatService.sendMessage(messageDTO);
    }
}
