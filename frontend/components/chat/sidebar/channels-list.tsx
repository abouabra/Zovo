import React, { useState } from "react";
import ChannelItem, { Channel } from "./channel";

const fakeChannels: Channel[] = [
	{
		id: "1",
		type: "personal",
		name: "Alice Johnson",
		avatar: "https://randomuser.me/api/portraits/women/1.jpg",
		status: "online",
		unread: 2,
		lastMessage: {
			content: "See you soon!",
			timestamp: "2025-05-07T10:45:00Z",
		},
	},
	{
		id: "2",
		type: "group",
		name: "Dev Team Chat",
		avatar: "https://placehold.co/64x64?text=DT",
		status: "",
		unread: 5,
		lastMessage: {
			content: "Don't forget the deployment. Don't forget the deployment. Don't forget the deployment. Don't forget the deployment. Don't forget the deployment. Don't forget the deployment. Don't forget the deployment.",
			timestamp: "2025-05-07T09:30:00Z",
		},
	},
	{
		id: "3",
		type: "personal",
		name: "Bob Smith",
		avatar: "https://randomuser.me/api/portraits/men/2.jpg",
		status: "offline",
		unread: 0,
		lastMessage: {
			content: "Talk later.",
			timestamp: "2025-05-06T18:20:00Z",
		},
	},
	{
		id: "4",
		type: "group",
		name: "Marketing",
		avatar: "https://placehold.co/64x64?text=MKT",
		status: "",
		unread: 1,
		lastMessage: {
			content: "Meeting rescheduled.",
			timestamp: "2025-05-06T15:10:00Z",
		},
	},
	{
		id: "5",
		type: "personal",
		name: "Charlie Davis",
		avatar: "https://randomuser.me/api/portraits/men/3.jpg",
		status: "online",
		unread: 3,
		lastMessage: {
			content: "Cool, thanks!",
			timestamp: "2025-05-07T12:05:00Z",
		},
	},
	{
		id: "6",
		type: "group",
		name: "Book Club",
		avatar: "https://placehold.co/64x64?text=BC",
		status: "",
		unread: 0,
		lastMessage: {
			content: "Next chapter is amazing!",
			timestamp: "2025-05-05T20:40:00Z",
		},
	},
	{
		id: "7",
		type: "personal",
		name: "Dana White",
		avatar: "https://randomuser.me/api/portraits/women/4.jpg",
		status: "online",
		unread: 6,
		lastMessage: {
			content: "I want to discuss the project.",
			timestamp: "2025-05-07T12:10:00Z",
		},
	},
	{
		id: "8",
		type: "group",
		name: "Weekend Trip",
		avatar: "https://placehold.co/64x64?text=WT",
		status: "",
		unread: 2,
		lastMessage: {
			content: "Who's bringing snacks?",
			timestamp: "2025-05-07T08:50:00Z",
		},
	},
	{
		id: "9",
		type: "personal",
		name: "Eve Adams",
		avatar: "https://randomuser.me/api/portraits/women/5.jpg",
		status: "offline",
		unread: 0,
		lastMessage: {
			content: "I'll call you later.",
			timestamp: "2025-05-06T21:15:00Z",
		},
	},
	{
		id: "10",
		type: "group",
		name: "Hackathon Team",
		avatar: "https://placehold.co/64x64?text=HT",
		status: "",
		unread: 4,
		lastMessage: {
			content: "Code freeze at midnight!",
			timestamp: "2025-03-07T01:00:00Z",
		},
	},
];

const ChannelsList = () => {
	const [activeId, setActiveId] = useState<string | null>(null);

	return (
		<div className="w-full h-full max-w-4xl flex flex-col overflow-auto">
			{fakeChannels.map((ch) => (
				<ChannelItem key={ch.id} channel={ch} isActive={ch.id === activeId} onClick={() => setActiveId(ch.id)} />
			))}
		</div>
	);
};

export default ChannelsList;
