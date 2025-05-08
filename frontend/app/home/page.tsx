"use client";
import { ArrowLeft } from "lucide-react";
import React, { useEffect } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import ChatBody from "@/components/chat/chat-body";
import { useChannelsSidebarStore } from "@/stores/useChannelsSidebarStore";

const HomePage = () => {
	const {activeChannelId, getActiveChannel, setActiveChannelId} = useChannelsSidebarStore();

	useEffect(() => {
		// handle the escape key to close the search
		const handleKeyDown = (event: KeyboardEvent) => {
			if (event.key === "Escape") {
				setActiveChannelId(null);
			}
		};
		window.addEventListener("keydown", handleKeyDown);
		return () => {
			window.removeEventListener("keydown", handleKeyDown);
		};
	}, [setActiveChannelId]);

	if (activeChannelId == "") return null;

	const channel = getActiveChannel();
	if (!channel) return null;
	return (
		<div className="flex w-full h-full flex-col items-center bg-app-bg/75 ">
			<div className="flex items-center w-full min-h-16 h-16 border-b gap-4 px-4 bg-bars-bg cursor-pointer">
				<div className="flex md:hidden p-2 border-0 rounded-full hover:bg-borders/75 cursor-pointer" onClick={() => setActiveChannelId(channel.id)}>
					<ArrowLeft className="text-high-emphasis" size={24} />
				</div>
				<Avatar className="w-12 h-12">
					<AvatarImage src={channel.avatar} />
					<AvatarFallback>{channel.name}</AvatarFallback>
				</Avatar>
				<span className="font-bold select-none">{channel.name}</span>
				<span className="font-bold select-none">{channel.id}</span>
			</div>
			<ChatBody />
		</div>
	);
};

export default HomePage;
