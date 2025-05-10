import { MessageType } from "@/constants/message-type";
import { callApi } from "@/lib/callApi";
import { format, differenceInMinutes } from "date-fns";
import { useEffect, useRef } from "react";
import DateSeparator from "./date-separator";
import MessageStack from "./message-stack";
import ChatInput from "./chat-input";
import { useMessagesStore } from "@/stores/useChannelMessagesStore";

interface GroupedMessage {
	date: string;
	messages: {
		senderId: number;
		items: MessageType[];
	}[];
}

function groupMessagesByDate(messages: MessageType[]): GroupedMessage[] {
	const grouped: Record<string, MessageType[]> = {};

	messages.forEach((msg) => {
		const date = new Date(msg.timestamp);
		const dateKey = format(date, "yyyy-MM-dd");
		if (!grouped[dateKey]) grouped[dateKey] = [];
		grouped[dateKey].push(msg);
	});

	return Object.entries(grouped).map(([date, messages]) => ({
		date,
		messages: groupConsecutiveMessages(messages),
	}));
}

function groupConsecutiveMessages(messages: MessageType[]) {
	const groups: { senderId: number; items: MessageType[] }[] = [];

	for (let i = 0; i < messages.length; i++) {
		const current = messages[i];
		const prevGroup = groups[groups.length - 1];

		if (prevGroup && prevGroup.senderId === current.sender.id && differenceInMinutes(new Date(current.timestamp), new Date(prevGroup.items.at(-1)!.timestamp)) <= 2) {
			prevGroup.items.push(current);
		} else {
			groups.push({ senderId: current.sender.id, items: [current] });
		}
	}

	return groups;
}

type ChatBodyProps = {
	channelId: string;
};

const ChatBody = ({ channelId }: ChatBodyProps) => {
	const { messages, setMessages } = useMessagesStore();
	const containerRef = useRef<HTMLDivElement>(null);

	useEffect(() => {
		const getMessagesData = async () => {
			const data = await callApi<MessageType[]>(`/chat/messages/${channelId}`);
			if (data.details) setMessages(data.details);
		};
		getMessagesData();
	}, [channelId, setMessages]);

	useEffect(() => {
		containerRef.current?.scrollTo(0, containerRef.current.scrollHeight);
	}, [messages, setMessages]);

	const groupedByDate = groupMessagesByDate(messages);

	return (
		<div className="flex flex-col w-full h-[calc(100%-4rem)]">
			<div ref={containerRef} className="flex flex-col w-full overflow-auto flex-1 justify-center">
				<div className="flex flex-col w-full max-w-4xl min-h-full mx-auto px-4">
					<div className="flex flex-col justify-end space-y-4 py-2 mt-auto">
						{groupedByDate.map((group) => (
							<div key={group.date}>
								<DateSeparator date={group.date} />
								{group.messages.map((stack, idx) => (
									<MessageStack key={idx} messages={stack.items} />
								))}
							</div>
						))}
					</div>
				</div>
			</div>
			<div className="flex  w-full max-w-4xl mx-auto">
				<ChatInput channelId={channelId} />
			</div>
		</div>
	);
};

export default ChatBody;
