import { Button } from "@/components/ui/button";
import Link from "next/link";
import { ModeToggle } from "@/components/toggle";
import ZovoLogo from "@/components/svgs/ZovoLogo";

export default function Home() {
	return (
		<div className="flex flex-col lg:flex-row min-h-screen relative">
			<div className="absolute top-4 right-4">
				<ModeToggle />
			</div>

			<div className="w-full flex flex-col justify-center items-center lg:items-start p-6 sm:p-12 lg:p-24 gap-6 text-center lg:text-left">
				<span className="text-4xl sm:text-6xl lg:text-8xl font-bold leading-tight">
          Welcome to <span className="text-accent-primary"> Zovo</span>!
        </span>
				<p className="text-base sm:text-lg lg:text-xl mt-2 sm:mt-4 max-w-2xl">Zovo is a simple messaging app for personal chats, group conversations, communities and more. Built with privacy and usability in mind, it’s a small project inspired by platforms like Telegram and WhatsApp.</p>
				<div className="flex flex-col gap-4 mt-4 w-full max-w-xs">
					<Link href="/auth/login">
						<Button variant="default" className="w-full h-12 p-4 rounded-lg cursor-pointer bg-accent-primary hover:bg-accent-primary-pressed">
							Login
						</Button>
					</Link>
					<Link href="/auth/register">
						<Button variant="default" className="w-full h-12 p-4 rounded-lg cursor-pointer">
							Register
						</Button>
					</Link>
				</div>
			</div>

			<div className="w-full flex justify-center items-center p-6">
				<ZovoLogo className="w-3/4 sm:w-2/3 lg:w-3/4 h-auto" />
			</div>
		</div>
	);
}
