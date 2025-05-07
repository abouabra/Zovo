import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "Password Reset",
	description: "Messaging app for the modern world",
};

export default function PasswordResetLayout({ children }: { children: React.ReactNode }) {
	return <>{children}</>;
}
