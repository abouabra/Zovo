import ChatBody from "@/components/chat/chat-body";
import SideBar from "@/components/chat/side-bar";
import React from "react";

const HomePage = () => {

	return (
		<div className="flex w-full h-full flex-col md:flex-row">
			<SideBar />
			<ChatBody />
		</div>
	);
};

export default HomePage;
