import type { Metadata } from "next";
import { Roboto } from "next/font/google";
import "./globals.css";
import { ThemeProvider } from "@/components/theme-provider";
import { Toaster } from "@/components/ui/sonner";
import AppBackground from "@/components/app-background";
import StoreInitializer from "@/components/store-initializer";

const roboto = Roboto({
	variable: "--font-roboto",
	subsets: ["latin"],
	weight: ["400", "500"],
});

export const metadata: Metadata = {
	title: "Zovo",
	description: "Messaging app for the modern world",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
	return (
		<html lang="en" suppressHydrationWarning>
			<body className={`${roboto.variable} font-sans antialiased`}>
				<ThemeProvider attribute="class" defaultTheme="system" enableSystem>
					<AppBackground>
						{children}
						<StoreInitializer />
						<Toaster />
					</AppBackground>
				</ThemeProvider>
			</body>
		</html>
	);
}
