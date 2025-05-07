"use client";
import { useChatStore } from "@/stores/useChatStore";
import { useEffect, useState } from "react";
import SideBar from "@/components/chat/side-bar";

export default function HomeLayout({ children }: { children: React.ReactNode }) {
	const { isChatOpen } = useChatStore();

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

			{(!isMobile || !isChatOpen) && <SideBar />}
			{(!isMobile || isChatOpen) && children}
		</div>
	);
}
