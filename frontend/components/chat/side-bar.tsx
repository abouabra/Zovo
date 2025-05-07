"use client";
import React, { useEffect, useRef, useState } from "react";
import { ArrowLeft, SearchIcon } from "lucide-react";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";
import MenuIconMenu from "./search/menu-icon";
import ChannelsList from "./sidebar/channels-list";

const SideBar = () => {
	const [searchValue, setSearchValue] = useState("");
	const [isSearchActive, setIsSearchActive] = useState(false);
	const [isGlobalSearchSeeAll, setIsGlobalSearchSeeAll] = useState(false);
	const [isMessagesSeeAll, setIsMessagesSeeAll] = useState(false);
	const inputRef = useRef<HTMLInputElement>(null);


	useEffect(() => {
		// handle the escape key to close the search
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
						{Array.from({ length: isGlobalSearchSeeAll ? 10 : 5 }, (_, i) => (
							<div key={i} className="w-full h-16 min-h-16 p-4 border bg-red-400 mb-2" />
						))}

						<div className="flex w-full justify-between items-center p-4 border-b">
							<span className="text-muted-foreground font-bold">Messages</span>
							<span className="text-accent-primary cursor-pointer" onClick={() => setIsMessagesSeeAll(!isMessagesSeeAll)}>
								{isMessagesSeeAll ? "Show Less" : "Show All"}
							</span>
						</div>
						{Array.from({ length: isMessagesSeeAll ? 10 : 5 }, (_, i) => (
							<div key={i} className="w-full h-16 min-h-16 p-4 border bg-red-700 mb-2" />
						))}
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
