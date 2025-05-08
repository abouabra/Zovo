"use client";
import { Menu, Moon, Settings } from "lucide-react";
import React from "react";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Switch } from "@/components/ui/switch";
import { useTheme } from "next-themes";
import { useUserStore } from "@/stores/useUserStore";

const MenuIconMenu = () => {
	const { theme, setTheme } = useTheme();
	const { user } = useUserStore();
	const handleThemeChange = (checked: boolean) => {
		setTheme(checked ? "dark" : "light");
	};

	if (!user) return null;

	return (
		<Popover>
			<PopoverTrigger asChild>
				<div className="flex p-2 border-0 rounded-full hover:bg-borders/75 cursor-pointer transition-all duration-300 ease-in-out">
					<Menu className="text-high-emphasis cursor-pointer animate-rotate-fade" size={24} />
				</div>
			</PopoverTrigger>
			<PopoverContent className="w-64 p-0 ml-2">
				<div className="flex flex-col w-full border-green-700">
					<div className="flex w-full gap-4 items-center cursor-pointer p-4 border-b hover:bg-borders/75">
						<Settings className="w-6 h-6" />
						<span className="font-bold select-none">Settings</span>
					</div>

					<div className="flex w-full gap-4 items-center cursor-pointer p-4 border-b hover:bg-borders/75">
						<Moon className="w-6 h-6" />
						<span className="font-bold select-none">Dark Theme</span>
						<Switch className="ml-auto focus:!ring-0" checked={theme == "dark"} onCheckedChange={handleThemeChange} />
					</div>

					<div className="flex w-full gap-4 items-center cursor-pointer p-4 border-b hover:bg-borders/75">
						<Avatar className="w-6 h-6">
							<AvatarImage src={user.avatar} />
							<AvatarFallback>{user.username}</AvatarFallback>
						</Avatar>
						<span className="font-bold select-none">{user.username}</span>
					</div>
				</div>
			</PopoverContent>
		</Popover>
	);
};

export default MenuIconMenu;

