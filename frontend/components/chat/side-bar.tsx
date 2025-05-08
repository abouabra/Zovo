"use client";
import React, { useEffect, useRef, useState } from "react";
import { ArrowLeft, SearchIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";
import MenuIconMenu from "./search/menu-icon";
import ChannelsList from "./sidebar/channels-list";
import ChannelItem from "./sidebar/channel";
import { ChannelType } from "@/constants/channel-type";

const fakeSearchChannels: ChannelType[] = [
	{
		id: "a1b2c3d4",
		type: "personal",
		name: "Olivia Martinez",
		avatar: "https://randomuser.me/api/portraits/women/21.jpg",
		status: "online",
		lastMessage: {
			content: "Hey! Are we still on for tomorrow?",
			timestamp: "2025-05-07T14:22:00Z",
		},
	},
	{
		id: "e5f6g7h8",
		type: "group",
		name: "Design Squad",
		avatar: "https://placehold.co/64x64/FF6B6B/FFFFFF/png?text=DS",
		members: 4,
	},
	{
		id: "i9j0k1l2",
		type: "personal",
		name: "Liam Johnson",
		avatar: "https://randomuser.me/api/portraits/men/34.jpg",
		status: "offline",
		lastMessage: {
			content: "Just pushed the update — let me know if you see any bugs.",
			timestamp: "2025-05-06T22:10:00Z",
		},
	},
	{
		id: "m3n4o5p6",
		type: "group",
		name: "Weekend Hikers",
		avatar: "https://placehold.co/64x64/4ECDC4/000000/png?text=WH",
		members: 3622,
	},
	{
		id: "q7r8s9t0",
		type: "personal",
		name: "Emma Williams",
		avatar: "https://randomuser.me/api/portraits/women/45.jpg",
		status: "online",
		lastMessage: {
			content: "Can you review my PR when you get a chance?",
			timestamp: "2025-05-07T11:05:00Z",
		},
	},
	{
		id: "u1v2w3x4",
		type: "group",
		name: "Marketing Team",
		avatar: "https://placehold.co/64x64/FFCE00/333333/png?text=MT",
		members: 2971,
	},
	{
		id: "y5z6a7b8",
		type: "personal",
		name: "Noah Brown",
		avatar: "https://randomuser.me/api/portraits/men/52.jpg",
		status: "offline",
		lastMessage: {
			content: "Got it, thanks!",
			timestamp: "2025-05-05T17:45:00Z",
		},
	},
	{
		id: "c9d0e1f2",
		type: "group",
		name: "Book Club",
		avatar: "https://placehold.co/64x64/557A95/FFFFFF/png?text=BC",
		members: 20191,
	},
	{
		id: "g3h4i5j6",
		type: "personal",
		name: "Sophia Davis",
		avatar: "https://randomuser.me/api/portraits/women/67.jpg",
		status: "",
		lastMessage: {
			content: "👍🏻",
			timestamp: "2025-05-07T13:30:00Z",
		},
	},
	{
		id: "k7l8m9n0",
		type: "group",
		name: "Project Alpha",
		avatar: "https://placehold.co/64x64/33658A/FFFFFF/png?text=PA",
		members: 539218,
	},
	{
		id: "o1p2q3r4",
		type: "personal",
		name: "Mason Clark",
		avatar: "https://randomuser.me/api/portraits/men/65.jpg",
		status: "online",
		lastMessage: {
			content: "Let’s sync up later today.",
			timestamp: "2025-05-07T16:05:00Z",
		},
	},
	{
		id: "s5t6u7v8",
		type: "group",
		name: "Fitness Buddies",
		avatar: "https://placehold.co/64x64/008080/ffffff/png?text=FB",
		members: 0,
	},
	{
		id: "w9x0y1z2",
		type: "personal",
		name: "Isabella Torres",
		avatar: "https://randomuser.me/api/portraits/women/72.jpg",
		status: "offline",
		lastMessage: {
			content: "Received—thanks for sending!",
			timestamp: "2025-05-06T20:12:00Z",
		},
	},
	{
		id: "a3b4c5d6",
		type: "group",
		name: "Startup Founders",
		avatar: "https://placehold.co/64x64/ff5722/ffffff/png?text=SF",
		members: 6161,
	},
	{
		id: "e7f8g9h0",
		type: "personal",
		name: "Lucas Green",
		avatar: "https://randomuser.me/api/portraits/men/88.jpg",
		status: "",
		lastMessage: {
			content: "🚀",
			timestamp: "2025-05-07T09:55:00Z",
		},
	},
	{
		id: "i1j2k3l4",
		type: "group",
		name: "Cooking Club",
		avatar: "https://placehold.co/64x64/ffeb3b/000000/png?text=CC",
		members: 4262,
	},
	{
		id: "m5n6o7p8",
		type: "personal",
		name: "Harper Lee",
		avatar: "https://randomuser.me/api/portraits/women/30.jpg",
		status: "online",
		lastMessage: {
			content: "I’ll be there in 10 mins.",
			timestamp: "2025-05-07T14:50:00Z",
		},
	},
	{
		id: "q9r0s1t2",
		type: "group",
		name: "Movie Night",
		avatar: "https://placehold.co/64x64/673ab7/ffffff/png?text=MN",
		members: 113,
	},
	{
		id: "u3v4w5x6",
		type: "personal",
		name: "Chloe Kim",
		avatar: "https://randomuser.me/api/portraits/women/13.jpg",
		status: "offline",
		lastMessage: {
			content: "Got it!",
			timestamp: "2025-05-05T19:30:00Z",
		},
	},
	{
		id: "y7z8a9b0",
		type: "group",
		name: "Weekend Gamers",
		avatar: "https://placehold.co/64x64/3f51b5/ffffff/png?text=WG",
		members: 999,
	},
];

const SideBar = () => {
	const [searchValue, setSearchValue] = useState("");
	const [isSearchActive, setIsSearchActive] = useState(false);
	const [isGlobalSearchSeeAll, setIsGlobalSearchSeeAll] = useState(false);
	const [isMessagesSeeAll, setIsMessagesSeeAll] = useState(false);
	const inputRef = useRef<HTMLInputElement>(null);


	useEffect(() => {
		const handleKeyDown = (event: KeyboardEvent) => {
			if (event.key === "Escape") {
				setIsSearchActive(false);
				setSearchValue("");
				if (inputRef.current) {
					inputRef.current.blur();
				}
				setIsGlobalSearchSeeAll(false);
				setIsMessagesSeeAll(false);
			}
		};
		window.addEventListener("keydown", handleKeyDown);
		return () => {
			window.removeEventListener("keydown", handleKeyDown);
		};
	}, []);

	return (
		<div className=" w-full max-w-full md:max-w-[24rem] flex-col justify-start items-center bg-bars-bg border-r zoom-out-fade">
			<div className="flex items-center w-full min-h-16 h-16 border-b justify-between gap-4 px-4">
				{isSearchActive ? (
					<div className="flex p-2 border-0 rounded-full hover:bg-borders/75 cursor-pointer transition-all duration-300 ease-in-out" onClick={() => setIsSearchActive(false)}>
						<ArrowLeft className="text-high-emphasis cursor-pointer animate-rotate-fade" size={24} />
					</div>
				) : (
					<MenuIconMenu />
				)}

				<div className="relative flex w-full">
					<div className={cn("absolute left-3 top-1/2 -translate-y-1/2", isSearchActive ? "text-accent-primary" : "text-muted-foreground")}>
						<SearchIcon className="h-5 w-5" />
					</div>
					<Input
						ref={inputRef}
						placeholder="Search"
						value={searchValue}
						onChange={(e) => setSearchValue(e.target.value)}
						onFocus={() => setIsSearchActive(true)}
						className={cn("flex h-10 pl-10 rounded-full focus:!ring-0 focus:!border-accent-primary", isSearchActive ? "border-accent-primary" : "")}
					/>
				</div>
			</div>

			<div className="relative w-full h-full" style={{ height: "calc(100% - 4rem)" }}>
				<div className={cn("absolute inset-0 flex flex-col items-center overflow-auto transition-opacity duration-300 ease-in-out", isSearchActive ? "opacity-100" : "opacity-0 pointer-events-none")}>
					<div className="flex flex-col w-full">
						<div className="flex w-full justify-between items-center p-4 border-b">
							<span className="text-muted-foreground font-bold">Global Search</span>
							<span className="text-accent-primary cursor-pointer" onClick={() => setIsGlobalSearchSeeAll(!isGlobalSearchSeeAll)}>
								{isGlobalSearchSeeAll ? "Show Less" : "Show More"}
							</span>
						</div>
						{fakeSearchChannels.filter((channel) => channel.type === "group")
							.slice(0, isGlobalSearchSeeAll ? 10 : 5)
							.map((channel) => (
								<ChannelItem
									key={channel.id}
									channel={channel}
								/>
							))
						}

						<div className="flex w-full justify-between items-center p-4 border-b">
							<span className="text-muted-foreground font-bold">Contacts</span>
							<span className="text-accent-primary cursor-pointer" onClick={() => setIsMessagesSeeAll(!isMessagesSeeAll)}>
								{isMessagesSeeAll ? "Show Less" : "Show All"}
							</span>
						</div>
						{fakeSearchChannels.filter((channel) => channel.type === "personal")
							.slice(0, isMessagesSeeAll ? 10 : 5)
							.map((channel) => (
								<ChannelItem
									key={channel.id}
									channel={channel}
								/>
							))
						}
					</div>
				</div>

				<div className={cn("absolute inset-0 overflow-auto transition-opacity duration-300 ease-in-out", isSearchActive ? "opacity-0 pointer-events-none" : "opacity-100")}>
					<ChannelsList />
				</div>
			</div>
		</div>
	);
};

export default SideBar;
