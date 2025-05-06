"use client";
import { useUserStore } from "@/stores/useUserStore";
import React from "react";
import LogoutBtn from "./logout-btn";

const HomePage = () => {
	const { id, username, email } = useUserStore();

	return (
		<div className="flex w-full h-full flex-col justify-center items-center">
			<div className="flex flex-col items-start justify-center gap-4">
				<span className="text-2xl">User ID: {id}</span>
				<span className="text-2xl">Username: {username}</span>
				<span className="text-2xl">Email: {email}</span>
				<LogoutBtn />
			</div>
		</div>
	);
};

export default HomePage;
