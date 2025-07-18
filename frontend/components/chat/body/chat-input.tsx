"use client";

import { useState } from "react";
import React from "react";
import { Input } from "../../ui/input";
import { Button } from "../../ui/button";
import { SendHorizonalIcon } from "lucide-react";
import { cn } from "@/lib/utils";

interface ChatInputProps {
	channelId: string;
	sendMessage: (content: string) => void;
}

const ChatInput = ({ channelId , sendMessage}: ChatInputProps) => {
	const [isTyping, setIsTyping] = useState(false);
	const [message, setMessage] = useState("");
	const handleSendMessage = () => {
		if (message.trim() == "") return;

		console.log("Sending message:", message);
		console.log("channelId:", channelId);

		sendMessage(message);
		setMessage("");
	};

	return (
		<div className="flex w-full h-16 min-h-16 bg-bars-bg p-2 gap-2 items-center rounded-full mb-2 ">
			<Input
				type="text"
				className="w-full h-full bg-input-bg rounded-full px-4 py-2 text-low-emphasis text-subtitle1 !ring-0"
				placeholder="Type a message..."
				value={message}
				onChange={(e) => setMessage(e.target.value)}
				onFocus={() => setIsTyping(true)}
				onBlur={() => setIsTyping(false)}
				onKeyDown={(e) => {
					if (e.key === "Enter") handleSendMessage();
				}}
			/>
			<Button className={cn("h-full text-white p-4 size-12 rounded-full", isTyping ? "bg-accent-primary hover:bg-accent-primary/65" : "bg-disabled hover:bg-disabled/65")} onClick={() => handleSendMessage()}>
				<SendHorizonalIcon className="size-6" />
			</Button>
		</div>
	);
};

export default ChatInput;
