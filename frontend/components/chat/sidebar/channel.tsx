import React from "react";
import { useChatStore } from "@/stores/useChatStore";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { cn } from "@/lib/utils";

/*
{
    "id": "channel-uuid",
    "type": "personal", // or "group"
    "name": "John Doe", // for personal, it's the other user's name
    "avatar": "https://example.com/avatar.jpg",
    "status": "online", // only for personal
    "unread": 3,
    "lastMessage": {
      "content": "Hey, how are you?",
      "timestamp": "2025-05-07T12:30:00Z"
    }
  },
*/

export interface Channel {
    id: string;
    type: "personal" | "group";
    name: string;
    avatar: string;
    status: "online" | "offline" | "";
    unread: number;
    lastMessage: {
      content: string;
      timestamp: string;
    };
  }
  
  interface ChannelProps {
    channel: Channel;
    isActive: boolean;
    onClick: () => void;
  }

const convertTimestampToAppropriateFormat = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 86400) {
        // less than a day
        if (now.getDate() === date.getDate() + 1) {
            // yesterday
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

const ChannelItem = ({ channel, isActive, onClick }: ChannelProps) => {
    const { setChatData } = useChatStore();

    const clickHandler = () => {
        setChatData({ ...channel, isChatOpen: true });
        onClick();
    };
    return (
        <div className={cn("flex gap-2 items-center w-full h-16 min-h-16 p-4 cursor-pointer select-none hover:bg-input-bg", isActive ? "bg-borders" : "bg-bars-bg" )} onClick={clickHandler}>
            <div className="w-12 h-12 relative">
                {channel.status != "" && <div className={cn("w-3 h-3 rounded-full absolute top-0.5 right-0.5 z-2 outline-4 outline-bars-bg", status === "online" ? "bg-accent-info" : "bg-accent-error")} />}
                <Avatar className="w-12 h-12">
                    <AvatarImage src={channel.avatar} />
                    <AvatarFallback>{channel.name}</AvatarFallback>
                </Avatar>
            </div>
            <div className="flex flex-col w-full">
                <div className="flex justify-between w-full relative">
                    <span className="text-subtitle1-bold">{channel.name}</span>
                    {channel.unread > 0 && (
                        <span className="absolute -top-1 -right-1 bg-accent-error rounded-full w-5 h-5 flex items-center justify-center text-body2 text-white">
                            {channel.unread}
                        </span>
                    )}
                </div>
                <div className="flex items-center">
                    <span className="truncate max-w-[14rem]">
                        {channel.lastMessage.content}
                    </span>
                    <span className="text-body2 min-w-[4rem] ml-auto text-right">{convertTimestampToAppropriateFormat(channel.lastMessage.timestamp)}</span>
                </div>
            </div>
        </div>
    );
};

export default ChannelItem;
