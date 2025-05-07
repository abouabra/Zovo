"use client";

const ChatBody = () => {
	const randomMessages = Array.from({ length: 30 }, () => ({
		side: Math.random() > 0.5 ? 1 : 0,
		text: Math.random()
			.toString(36)
			.substring(2, 15)
			.toUpperCase()
			.repeat(Math.floor(Math.random() * 20) + 1),
	}));

	return (
		<div className="w-full h-full  max-w-4xl flex flex-col overflow-auto px-4">
			{randomMessages.map((size, i) => (
				<div
					key={i}
					className="flex p-4 border bg-sky-700 mb-2 break-all"
					style={{
						alignSelf: size.side ? "flex-end" : "flex-start",
					}}
				>
					{size.text}
				</div>
			))}
		</div>
	);
};

export default ChatBody;
