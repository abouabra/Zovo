import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "Home",
	description: "Messaging app for the modern world",
};

export default function HomeLayout({ children }: { children: React.ReactNode }) {
	return <>{children}</>;
}
