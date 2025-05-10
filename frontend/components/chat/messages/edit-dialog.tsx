"use client";

import React, { useState } from "react";
import { PenIcon } from "lucide-react";
import { MessageType } from "@/constants/message-type";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import LoadingScreen from "@/components/loading-screen";
import { useMessagesStore } from "@/stores/useChannelMessagesStore";

const handleEditMessage = async (message: MessageType, content: string, updateMessage: (id: string, data: MessageType) => void, messages: MessageType[]) => {
	console.log("Edit message: ", message);
	console.log("New content: ", content);
	await new Promise((resolve) => setTimeout(resolve, 1000));
	message.content = content;
	updateMessage(message.id, { ...message });
	console.log("Updated message: ", messages);
};

interface EditDialogProps {
	message: MessageType;
	setIsMenuOpen: (isHovered: boolean) => void;
}

const EditDialog = ({ message, setIsMenuOpen }: EditDialogProps) => {
	const { messages, updateMessage } = useMessagesStore();
	const [content, setContent] = useState<string>(message.content);
	const [isOpen, setIsOpen] = useState(false);
	const [isLoading, setIsLoading] = useState(false);

	return (
		<Dialog
			open={isOpen}
			onOpenChange={(open) => {
				setIsOpen(open);
				setIsMenuOpen(open);
			}}
		>
			<DialogTrigger asChild>
				<div className="flex w-full gap-4 items-center cursor-pointer p-3 hover:bg-borders/75">
					<PenIcon className="size-6 text-low-emphasis" />
					<span className="text-high-emphasis text-subtitle1 select-none">Edit Message</span>
				</div>
			</DialogTrigger>
			<DialogContent className="sm:max-w-[425px]">
				{isLoading && <LoadingScreen />}
				<DialogHeader>
					<DialogTitle>Edit Message</DialogTitle>
					<DialogDescription>Make changes to your message here. Click save when you&apos;re done.</DialogDescription>
				</DialogHeader>
				<div className="flex w-full gap-4 py-4">
					<div className="flex flex-col w-full">
						<textarea value={content} onChange={(e) => setContent(e.target.value)} className="w-full outline-0 max-w-[500px] h-32 p-2 border border-accent-primary rounded-lg resize-none" placeholder="Type your message here..." />
					</div>
				</div>
				<DialogFooter>
					<div className="flex max-w-[500px] w-full gap-4">
						<DialogTrigger asChild>
							<Button className="ml-auto bg-transparent text-accent-primary font-bold hover:bg-accent-primary hover:text-high-emphasis">Cancel</Button>
						</DialogTrigger>
						<Button
							className="bg-transparent text-accent-primary font-bold hover:bg-accent-primary hover:text-high-emphasis"
							type="submit"
							onClick={async () => {
								setIsLoading(true);
								await handleEditMessage(message, content, updateMessage, messages);
								setIsLoading(false);
								setIsOpen(false);
								setIsMenuOpen(false);
							}}
						>
							Save changes
						</Button>
					</div>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
};

export default EditDialog;
