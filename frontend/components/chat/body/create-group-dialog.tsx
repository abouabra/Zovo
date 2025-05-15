"use client";

import { UsersRoundIcon } from "lucide-react";
import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import LoadingScreen from "@/components/loading-screen";
import { Input } from "@/components/ui/input";
import { callApi } from "@/lib/callApi";
import { useChannelsSidebarStore } from "@/stores/useChannelsSidebarStore";
import { ChannelType } from "@/constants/channel-type";
import { toast } from "sonner";

type CreateGroupResponse = {
	id: string;
	avatar: string;
};

const CreateGroupDialog = () => {
	const [open, setOpen] = useState(false);
	const [isLoading, setIsLoading] = useState(false);
	const [content, setContent] = useState<string>("");
	const { addChannel } = useChannelsSidebarStore();
	// const {} = useChatSocket();
	const handleCreateGroup = async () => {
		try {
			setIsLoading(true);
			const res = await callApi<CreateGroupResponse>("/chat/create", {
				method: "POST",
				body: JSON.stringify({
					name: content,
				}),
			});
			if (res.code == "SUCCESS" && res.details) {
				const channel = {
					id: res.details.id,
					name: content,
					type: "group",
					avatar: res.details.avatar,
					unread: 0,
					status: "online",
				} as ChannelType;

				addChannel(channel);
				setContent("");
				setOpen(false);
				toast.success("Group created successfully", {
					description: "You can now start chatting with your group.",
				});
				console.log("Group created successfully");
			}
		} catch (error) {
			toast.error("Error creating group", {
				description: "Please try again later.",
			});
			console.error("Error creating group: ", error);
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<div className="flex w-full gap-4 items-center cursor-pointer p-4 border-b bg-bars-bg hover:bg-borders/75">
					<UsersRoundIcon className="w-6 h-6" />
					<span className="select-none">New Group</span>
				</div>
			</DialogTrigger>
			<DialogContent className="sm:max-w-[425px]">
				{isLoading && <LoadingScreen />}
				<DialogHeader>
					<DialogTitle>Create new Group</DialogTitle>
					<DialogDescription>To create a new group, please enter the group name.</DialogDescription>
				</DialogHeader>
				<div className="flex flex-col w-full">
					<Input onChange={(e) => setContent(e.target.value)} value={content} className="w-full outline-0 p-2 border border-accent-primary rounded-lg resize-none" placeholder="Type your group name..." />
				</div>
				<DialogFooter>
					<div className="flex max-w-[500px] w-full gap-4">
						<Button
							className="bg-transparent text-accent-primary font-bold hover:bg-accent-primary hover:text-high-emphasis ml-auto border-2 border-accent-primary"
							type="submit"
							onClick={async () => {
								setIsLoading(true);
								await handleCreateGroup();
								setOpen(false);
								setIsLoading(false);
							}}
						>
							Create
						</Button>
					</div>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
};

export default CreateGroupDialog;
