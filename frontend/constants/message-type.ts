export type MessageType = {
	id: string;
	content: string;
	timestamp: string;
	sender: {
		id: number;
		username: string;
		avatar: string;
		status: string;
	};
};
