import { Button } from "@/components/ui/button";
import { ArrowLeft, ArrowUpRight } from "lucide-react";
import Link from "next/link";
import React from "react";

interface AuthHeaderProps {
	description: string;
	link: string;
}

const AuthHeader = ({ description, link }: AuthHeaderProps) => {
	return (
		<div className="flex items-center w-full h-16 border-b justify-between flex-wrap">
			<div className="flex p-4 items-center">
				<Link href="/">
					<Button variant="ghost" className="p-2 cursor-pointer">
						<ArrowLeft className="size-5" />
						<span className="text-subtitle1 ">Back</span>
					</Button>
				</Link>
			</div>
			<div className="flex p-4 items-center">
				<Link href={link}>
					<Button variant="ghost" className="p-2 cursor-pointer">
						<span className="text-subtitle1 ">{description}</span>
						<ArrowUpRight className="size-5" />
					</Button>
				</Link>
			</div>
		</div>
	);
};

export default AuthHeader;
