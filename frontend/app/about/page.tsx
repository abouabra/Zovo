"use client";
import { useUserStore } from "@/stores/useUserStore";
import React from "react";
import LogoutBtn from "./logout-btn";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

const AboutPage = () => {
	const { user } = useUserStore();
	if (!user) return null;

	return (
		<div className="flex w-full h-full flex-col justify-center items-center">
			<div className="flex flex-col items-start justify-center gap-4">
				<span className="text-2xl">User ID: {user.id}</span>
				<span className="text-2xl">Username: {user.username}</span>
				<span className="text-2xl">Email: {user.email}</span>
				<Avatar className="w-48 h-48 rounded-none">
					<AvatarImage src={user.avatar} />
					<AvatarFallback>{user.username}</AvatarFallback>
				</Avatar>
				<LogoutBtn />
			</div>
		</div>
	);
};

export default AboutPage;
