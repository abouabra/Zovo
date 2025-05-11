export type MessageType = {
	id: string;
	channelId: string;
	content: string;
	timestamp: string;
	sender: {
		id: number;
		username: string;
		avatar: string;
		status: string;
	};
};
