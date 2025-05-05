import type { Metadata } from "next";

export const metadata: Metadata = {
	title: "Login",
	description: "Messaging app for the modern world",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
	return <>
		{children}
	</>;
}
