"use client";

import { useEffect, useRef, useCallback } from "react";
import SockJS from "sockjs-client";
import { Client, Message as StompMessage } from "@stomp/stompjs";
import type { MessageType } from "@/constants/message-type";
import { useUserStore } from "@/stores/useUserStore";

interface UseChatSocketResult {
	sendMessage: (content: string) => void;
}

export function useChatSocket(channelId: string, addMessage: (msg: MessageType) => void): UseChatSocketResult {
	const clientRef = useRef<Client | null>(null);
    const { user } = useUserStore();
	useEffect(() => {
		if (!channelId) return;
		const socket = new SockJS("https://localhost/ws-chat");
		const client = new Client({
			webSocketFactory: () => socket as WebSocket,
			reconnectDelay: 5000,
			onConnect: () => {
				clientRef.current = client;
				client.subscribe(`/topic/channel.${channelId}`, (stompMsg: StompMessage) => {
					try {
                        const message = JSON.parse(stompMsg.body) as MessageType;
						if (message.channelId === channelId) {
							addMessage(message);
						}
					} catch (err) {
						console.error("[STOMP] parse error", err);
					}
				});
			},
			onStompError: (frame) => {
				console.error("[STOMP] broker error", frame.headers["message"]);
			},
		});

		client.activate();

		return () => {
			client.deactivate();
			clientRef.current = null;
		};
	}, [channelId, addMessage]);


	const sendMessage = useCallback(
		(content: string) => {
			const client = clientRef.current;
			if (!client?.connected) {
				console.warn("[STOMP] not connected yet");
				return;
			}
            if(!user) return;

			const payload = {
				channelId,
				content,
                sender: {
                    id: user.id,
                    username: user.username,
                    avatar: user.avatar,
                    status: user.status,
                }
			};

			client.publish({
				destination: "/app/chat.sendMessage",
				body: JSON.stringify(payload),
			});
		},
		[channelId, user]
	);

	return { sendMessage };
}
