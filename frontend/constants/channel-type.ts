export type ChannelType = {
	id: string;
	type: "personal" | "group";
	name: string;
	avatar: string;
	status?: "online" | "offline" | "";
	unread?: number;
	members?: number;
	lastMessage?: {
		content: string;
		timestamp: string;
	};
};
