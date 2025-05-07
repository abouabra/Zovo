import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "About",
	description: "Messaging app for the modern world",
};

export default function AboutLayout({ children }: { children: React.ReactNode }) {
	return <>{children}</>;
}
