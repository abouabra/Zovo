"use client";
import { useEffect, useState } from "react";
import { useTheme } from "next-themes";

type AppBackgroundProps = {
	patternId?: number; // between 1 and 30
	color?: string;
	children?: React.ReactNode;
};

const AppBackground = ({ patternId = 1, color = "var(--borders)", children }: AppBackgroundProps) => {
	const { resolvedTheme } = useTheme();
	const [isMounted, setIsMounted] = useState(false);


	useEffect(() => {
		setIsMounted(true);
	}, []);

	if (!isMounted)
		return null;

	const clampedId = Math.max(1, Math.min(30, patternId));
	const isDarkMode = resolvedTheme === "dark" ? "" : "-dark";

	return (
		<div
			className="w-screen h-screen relative"
			style={{
				backgroundImage: `url('/patterns/pattern-${clampedId}${isDarkMode}.svg')`,
				backgroundRepeat: "repeat-x",
				backgroundSize: "auto 120%",
				backgroundPosition: "left center",
				backgroundColor: color,
			}}
		>
			<div
				className="absolute inset-0"
				style={{
					backgroundColor: isDarkMode ? "rgba(255,255,255,0.8)" : "rgba(0,0,0,0.8)",
					zIndex: 0,
				}}
			/>

			<div className="relative z-2 flex w-screen h-screen flex-col overflow-auto">
				{children}
			</div>
		</div>
	);
};

export default AppBackground;
