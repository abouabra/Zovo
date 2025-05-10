"use client";
import React, { useEffect } from "react";
import ChannelItem from "./channel";
import { useChannelsSidebarStore } from "@/stores/useChannelsSidebarStore";
import { ChannelType } from "@/constants/channel-type";
import { callApi } from "@/lib/callApi";

const ChannelsList = () => {
	const { channels, setChannels } = useChannelsSidebarStore();
	useEffect(() => {
		const getSidebarData = async () => {
			const data = await callApi<ChannelType[]>("/chat/sidebar", {
				method: "GET",
			})
			if(data.details)
				setChannels(data.details);
		};
		getSidebarData();
	}, [setChannels]);

	return (
		<div className="w-full h-full max-w-4xl flex flex-col overflow-auto">
			{channels.map((ch) => (
				<ChannelItem key={ch.id} channel={ch} isForSearch={false} />
			))}
		</div>
	);
};

export default ChannelsList;
