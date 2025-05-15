"use client";
import { LogOutIcon, Menu, Moon } from "lucide-react";
import React, { useState } from "react";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Switch } from "@/components/ui/switch";
import { useTheme } from "next-themes";
import { callApi } from "@/lib/callApi";
import { useRouter } from "next/navigation";
import { AppRouterInstance } from "next/dist/shared/lib/app-router-context.shared-runtime";
import LoadingScreen from "@/components/loading-screen";
import EditProfile from "../body/edit-profile-sheet";
import CreateGroupDialog from "../body/create-group-dialog";

const MenuIconMenu = () => {
	const { theme, setTheme } = useTheme();
	const handleThemeChange = (checked: boolean) => {
		setTheme(checked ? "dark" : "light");
	};
	const router = useRouter();

	return (
		<Popover>
			<PopoverTrigger asChild>
				<div className="flex p-2 border-0 rounded-full hover:bg-borders/75 cursor-pointer transition-all duration-300 ease-in-out">
					<Menu className="text-high-emphasis cursor-pointer animate-rotate-fade" size={24} />
				</div>
			</PopoverTrigger>
			<PopoverContent className="w-64 p-0 ml-2">
				<div className="flex flex-col w-full">
					<EditProfile />
					<div className="flex w-full gap-4 items-center p-4 border-b bg-bars-bg hover:bg-borders/75">
						<Moon className="w-6 h-6" />
						<span className="select-none">Dark Theme</span>
						<Switch className="ml-auto focus:!ring-0 cursor-pointer" checked={theme == "dark"} onCheckedChange={handleThemeChange} />
					</div>
					<CreateGroupDialog />
					
					<LogoutMenuOption router={router} />
				</div>
			</PopoverContent>
		</Popover>
	);
};

interface LogoutMenuOptionProps {
	router: AppRouterInstance;
}

const LogoutMenuOption = ({router}: LogoutMenuOptionProps) => {
	const [isLoading, setIsLoading] = useState(false);
	
	const handleLogout = async (router: AppRouterInstance) => {
		const res = await callApi("/auth/logout", {
			method: "POST",
		});
		if(res.code == "SUCCESS") {
			router.push("/login");
		}
	}

	return (
		<>
		{ isLoading && <LoadingScreen /> }
		<div className="flex w-full gap-4 items-center cursor-pointer p-4 border-b bg-bars-bg hover:bg-borders/75" onClick={async () => {
			setIsLoading(true);
			await handleLogout(router);
			setIsLoading(false);
		}}>
			<LogOutIcon className="w-6 h-6" />
			<span className="select-none">{isLoading ? "Logging out..." : "Logout"}</span>
		</div>
		</>
	);
}
export default MenuIconMenu;
