"use client";

import { Trash } from "lucide-react";
import React, { useState } from "react";
import { MessageType } from "@/constants/message-type";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import LoadingScreen from "@/components/loading-screen";
import { useMessagesStore } from "@/stores/useChannelMessagesStore";

const handleDeleteMessage = async (message: MessageType, removeMessage: (id: string) => void ) => {
	console.log("Delete message: ", message);
	await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate an API call
	removeMessage(message.id);
};

interface DeleteDialogProps {
	message: MessageType;
	setIsMenuOpen: (isHovered: boolean) => void;
}

const DeleteDialog = ({ message, setIsMenuOpen }: DeleteDialogProps) => {
	const {removeMessage} = useMessagesStore();
	const [open, setOpen] = useState(false);
	const [isLoading, setIsLoading] = useState(false);

	return (
		<Dialog open={open} onOpenChange={setOpen}>
			<DialogTrigger asChild>
				<div className="flex w-full gap-4 items-center cursor-pointer p-3 hover:bg-borders/75">
					<Trash className="size-6 text-accent-error" />
					<span className="text-accent-error text-subtitle1 select-none">Delete Message</span>
				</div>
			</DialogTrigger>
			<DialogContent className="sm:max-w-[425px]">
				{isLoading && <LoadingScreen />}
				<DialogHeader>
					<DialogTitle>Delete Message</DialogTitle>
					<DialogDescription>Are you sure you want to delete this message? This action cannot be undone.</DialogDescription>
				</DialogHeader>
				<DialogFooter>
					<div className="flex max-w-[500px] w-full gap-4">
						<DialogTrigger asChild>
							<Button className="ml-auto bg-transparent text-accent-primary font-bold hover:bg-accent-primary hover:text-high-emphasis">Cancel</Button>
						</DialogTrigger>
						<Button
							className="bg-transparent text-accent-error font-bold hover:bg-accent-error hover:text-high-emphasis"
							type="submit"
							onClick={async () => {
								setIsLoading(true);
								await handleDeleteMessage(message, removeMessage);
								setOpen(false);
								setIsLoading(false);
								setIsMenuOpen(false);
							}}
						>
							Delete
						</Button>
					</div>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
};

export default DeleteDialog;
