"use client";

import { EllipsisVerticalIcon } from "lucide-react";
import React from "react";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { MessageType } from "@/constants/message-type";
import DeleteDialog from "../messages/delete-dialog";
import EditDialog from "../messages/edit-dialog";

interface MessageOptionsProps {
	message: MessageType;
	setIsMenuOpen: (isHovered: boolean) => void;
	isMenuOpen: boolean;
}

const MessageOptions = ({isMenuOpen, setIsMenuOpen, message }: MessageOptionsProps) => {
	return (
		<Popover open={isMenuOpen} onOpenChange={(isOpen) => setIsMenuOpen(isOpen)}>
			<PopoverTrigger asChild>
				<div className="flex p-2 border-0 rounded-full bg-reaction-bg-user hover:bg-reaction-bg-user/75 cursor-pointer">
					<EllipsisVerticalIcon className="cursor-pointer text-low-emphasis" size={24} />
				</div>
			</PopoverTrigger>
			<PopoverContent className="w-58 p-0 ml-2 bg-bars-bg">
				<div className="flex flex-col w-full">
					<EditDialog message={message} setIsMenuOpen={setIsMenuOpen} />
					<DeleteDialog message={message} setIsMenuOpen={setIsMenuOpen} />
				</div>
			</PopoverContent>
		</Popover>
	);
};

export default MessageOptions;
