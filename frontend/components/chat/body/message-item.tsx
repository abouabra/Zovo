"use client";

import { MessageType } from "@/constants/message-type";
import React from "react";
import { Avatar, AvatarFallback, AvatarImage } from "../../ui/avatar";
import { useUserStore } from "@/stores/useUserStore";
import { cn } from "@/lib/utils";
import { format } from "date-fns";

interface MessageItemProps {
	message: MessageType;
	showAvatar: boolean;
	showTimestamp: boolean;
}

const MessageItem = ({ message, showAvatar, showTimestamp }: MessageItemProps) => {
	const { user } = useUserStore();

	if (!user) return null;
	const isMine = user.id === message.sender.id;

	return (
		<div className={cn("flex gap-2", isMine ? "justify-end" : "justify-start")}>
			{!isMine && showAvatar ? (
				<Avatar className="w-9 h-9">
					<AvatarImage src={message.sender.avatar} />
					<AvatarFallback>{message.sender.username[0]}</AvatarFallback>
				</Avatar>
			) : (
				<div className="w-9 h-9" />
			)}

			<div className={cn("flex flex-col gap-1 max-w-[75%]", isMine ? "items-end" : "items-start")}>
				<div className="flex gap-2">
					<div className={cn("bg-input-bg px-4 py-2 rounded-xl text-body2 text-high-emphasis", isMine ? "rounded-br-none" : "rounded-bl-none")}>{message.content}</div>
				</div>

				{showTimestamp && <span className="text-caption text-low-emphasis text-xs">{format(new Date(message.timestamp), "hh:mm a")}</span>}
			</div>
		</div>
	);
};

export default MessageItem;
