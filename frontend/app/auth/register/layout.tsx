import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "Register",
	description: "Messaging app for the modern world",
};

export default function RegisterLayout({ children }: { children: React.ReactNode }) {
	return <>{children}</>;
}
