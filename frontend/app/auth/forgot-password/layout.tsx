import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "Forgot Password",
	description: "Messaging app for the modern world",
};

export default function ForgotPasswordLayout({ children }: { children: React.ReactNode }) {
	return <>{children}</>;
}
