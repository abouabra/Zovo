"use client";
import React from "react";
import GoogleLogo from "@/components/svgs/GoogleLogo";
import GithubLogo from "@/components/svgs/GithubLogo";
import { Button } from "@/components/ui/button";

type Provider = {
	name: string;
	icon: React.FC<React.SVGProps<SVGSVGElement>>;
	link: string;
};

const providers: Provider[] = [
	{
		name: "Google",
		icon: GoogleLogo,
		link: "/api/v1/auth/oauth2/authorize/google",
	},
	{
		name: "Github",
		icon: GithubLogo,
		link: "/api/v1/auth/oauth2/authorize/github",
	},
];

const OAuthLogin = () => {
	return (
		<div className="flex flex-col items-center justify-center gap-4">
			{providers.map((provider) => (
				<Button
					key={provider.name}
					variant={"ghost"}
					onClick={() => {
						window.location.href = provider.link;
					}}
					className="w-80 h-12 rounded-full border-2 border-bg-borders flex justify-start items-center gap-4"
				>
					<provider.icon className="size-8" />
					<span className="text-high-emphasis">Continue with {provider.name}</span>
				</Button>
			))}
		</div>
	);
};

export default OAuthLogin;
