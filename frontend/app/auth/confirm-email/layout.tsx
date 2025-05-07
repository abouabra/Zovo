import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "Confirm Email",
	description: "Messaging app for the modern world",
};

export default function ConfirmEmailLayout({ children }: { children: React.ReactNode }) {
	return <>{children}</>;
}
