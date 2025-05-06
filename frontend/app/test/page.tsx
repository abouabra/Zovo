import React from "react";
import { ModeToggle } from "@/components/toggle";
import { Button } from "@/components/ui/button";
import Link from "next/link";

const TestPage = () => {
	return (
		<main className="flex min-h-screen flex-col items-center justify-between p-24">
			<h1 className="text-4xl font-bold">Welcome to Next.js!</h1>
			<p className="mt-4 text-lg">This is a simple Next.js application.</p>
			<ModeToggle />

			<Link href="/auth/login">
				<Button variant="outline" className="mt-4">
					Go to Login Page
				</Button>
			</Link>
		</main>
	);
};

export default TestPage;
