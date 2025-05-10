import React from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { cn } from "@/lib/utils";
import { useChannelsSidebarStore } from "@/stores/useChannelsSidebarStore";
import { ChannelType } from "@/constants/channel-type";


interface ChannelProps {
	channel: ChannelType;
	isForSearch: boolean;
}

export const convertTimestampToAppropriateFormat = (timestamp: string) => {
	const date = new Date(timestamp);
	const now = new Date();
	const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

	if (diffInSeconds < 86400) {
		if (now.getDate() === date.getDate() + 1) {
			return "Yesterday";
		}
		return date.toLocaleTimeString(undefined, { hour: "numeric", minute: "2-digit", hour12: true }).toUpperCase();
	} else if (diffInSeconds < 604800) {
		// less than a week
		return date.toLocaleDateString(undefined, { weekday: "long" });
	} else {
		return date.toLocaleDateString(undefined, { month: "2-digit", day: "2-digit", year: "2-digit" });
	}
};

const ChannelItem = ({ channel, isForSearch }: ChannelProps) => {
	const { activeChannelId, setActiveChannelId, addChannel } = useChannelsSidebarStore();

	const handleChannelClick = () => {
		if (activeChannelId !== channel.id) {
			setActiveChannelId(channel.id);
			addChannel(channel);
		}
	};

	return (
		<div className={cn("flex gap-2 items-center w-full h-16 min-h-16 p-4 cursor-pointer select-none hover:bg-input-bg", activeChannelId == channel.id ? "bg-borders" : "bg-bars-bg")} onClick={handleChannelClick}>
			<div className="w-12 h-12 relative">
				{channel.status && <div className={cn("w-3 h-3 rounded-full absolute top-0.5 right-0.5 z-2 outline-4 outline-bars-bg", channel.status === "online" ? "bg-accent-info" : "bg-accent-error")} />}
				<Avatar className="w-12 h-12">
					<AvatarImage src={channel.avatar} />
					<AvatarFallback>{channel.name}</AvatarFallback>
				</Avatar>
			</div>
			<div className="flex flex-col w-full">
				<div className="flex justify-between w-full relative">
					<span className="text-subtitle1-bold">{channel.name}</span>
					{channel.unread != null && channel.unread > 0 && <span className="absolute -top-1 -right-1 bg-accent-error rounded-full w-5 h-5 flex items-center justify-center text-body2 text-white">{channel.unread}</span>}
				</div>
				<div className="flex items-center">
					{channel.lastMessage && (
						<>
							<span className="truncate max-w-[14rem]">{channel.lastMessage.content}</span>
							<span className="text-body2 min-w-[4rem] ml-auto text-right">{convertTimestampToAppropriateFormat(channel.lastMessage.timestamp)}</span>
						</>
					)}
					{channel.type == "group" && isForSearch == true &&  channel.members && <span className="text-body2">{channel.members} members</span>}
				</div>
			</div>
		</div>
	);
};

export default ChannelItem;
