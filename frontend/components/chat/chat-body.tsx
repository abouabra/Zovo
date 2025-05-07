"use client";
import { ArrowLeft } from "lucide-react";
import React, { useState } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

const ChatBody = () => {
	const [isBackActive, setIsBackActive] = useState(false);
	const randomMessages = Array.from({ length: 30 }, () => ({
		side: Math.random() > 0.5 ? 1 : 0,
		text: Math.random()
			.toString(36)
			.substring(2, 15)
			.toUpperCase()
			.repeat(Math.floor(Math.random() * 20) + 1),
	}));
	return (
		<div className="flex w-full h-full flex-col items-center bg-app-bg/75">
			<div className="flex items-center w-full min-h-16 h-16 border-b gap-4 px-4">
				<div className="flex md:hidden p-2 border-0 rounded-full hover:bg-borders/75 cursor-pointer" onClick={() => setIsBackActive(!isBackActive)}>
					<ArrowLeft className="text-high-emphasis" size={24} />
				</div>
				<Avatar className="w-8 h-8">
					<AvatarImage src="https://github.com/shadcn.png" />
					<AvatarFallback>CN</AvatarFallback>
				</Avatar>
				<span className="font-bold select-none">UserXXXX</span>
			</div>
			<div className="w-full h-full  max-w-4xl flex flex-col overflow-auto px-4">
				{randomMessages.map((size, i) => (
					<div
						key={i}
						className="flex p-4 border bg-rose-700 mb-2 break-all"
						style={{
							alignSelf: size.side ? "flex-end" : "flex-start",
						}}
					>
						{size.text}
					</div>
				))}
			</div>
		</div>
	);
};

export default ChatBody;
