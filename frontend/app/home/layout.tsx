"use client";
import { useEffect, useState } from "react";
import SideBar from "@/components/chat/sidebar/side-bar";
import { useChannelsSidebarStore } from "@/stores/useChannelsSidebarStore";

export default function HomeLayout({ children }: { children: React.ReactNode }) {
	const {activeChannelId} = useChannelsSidebarStore();

	const [isMobile, setIsMobile] = useState(false);

	useEffect(() => {
		const handleResize = () => {
			setIsMobile(window.innerWidth < 768); // Tailwind md breakpoint
		};

		handleResize();
		window.addEventListener("resize", handleResize);
		return () => window.removeEventListener("resize", handleResize);
	}, []);

	return (
		<div className="flex w-full h-full transform transition-transform duration-300 ease-in-out">
			{(!isMobile || !activeChannelId) && <SideBar />}
			{(!isMobile || activeChannelId) && children}
		</div>
	);
}
