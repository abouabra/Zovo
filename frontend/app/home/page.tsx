"use client";
import { ArrowLeft } from "lucide-react";
import React, { useEffect } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { useChatStore } from "@/stores/useChatStore";
import ChatBody from "@/components/chat/chat-body";

const HomePage = () => {
	const { id, type, name, avatar, status, setIsChatOpen, setChatIdNull } = useChatStore();

	useEffect(() => {
		// handle the escape key to close the search
		const handleKeyDown = (event: KeyboardEvent) => {
			if (event.key === "Escape") {
				setChatIdNull();
			}
		};
		window.addEventListener("keydown", handleKeyDown);
		return () => {
			window.removeEventListener("keydown", handleKeyDown);
		};
	}, [setChatIdNull]);

	if (id == "") return null;
	return (
		<div className="flex w-full h-full flex-col items-center bg-app-bg/75 ">
			<div className="flex items-center w-full min-h-16 h-16 border-b gap-4 px-4 bg-bars-bg cursor-pointer">
				<div className="flex md:hidden p-2 border-0 rounded-full hover:bg-borders/75 cursor-pointer" onClick={() => setIsChatOpen(false)}>
					<ArrowLeft className="text-high-emphasis" size={24} />
				</div>
				<Avatar className="w-12 h-12">
					<AvatarImage src={avatar} />
					<AvatarFallback>{name}</AvatarFallback>
				</Avatar>
				<span className="font-bold select-none">{name}</span>
				<span className="font-bold select-none">{id}</span>
			</div>
			<ChatBody />
		</div>
	);
};

export default HomePage;
