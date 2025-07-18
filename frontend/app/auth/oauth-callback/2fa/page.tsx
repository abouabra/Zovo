"use client";
import React, { useEffect } from "react";
import { useSearchParams } from "next/navigation";
import { useTwoFAStore } from "@/stores/use2FAStore";
import { useRouter } from "next/navigation";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import Link from "next/link";
import { Button } from "@/components/ui/button";

const OAuth2FaCallback = () => {
	const {setTwoFAData} = useTwoFAStore();
	const searchParams = useSearchParams();
	const token = searchParams.get("token");
	const router = useRouter();

	useEffect(() => {
		if (token) {
			setTwoFAData({
				token: token,
			});
			router.push("/auth/login-2fa");
		}
	}, [token, router, setTwoFAData]);
	return (
		<div className="flex flex-col min-h-screen h-screen app-bg">
			<AuthHeader description="Already have an account?" link="/auth/login" />
			<div className="flex flex-col h-full items-center gap-8 p-4 ">
				<div className="flex flex-col items-center justify-center gap-4">
					<ZovoLogo className="w-2xs h-auto" />
					<span className="text-headline4">OAuth Login</span>
				</div>

				<div className="flex flex-col items-center justify-center gap-12 w-full pt-24">
					{!token ? (
						<>
							<div className="flex flex-col items-center justify-center gap-4">
								<span className="text-3xl">Invalid token</span>
							</div>

							<Link href="/auth/login">
								<Button variant="default" className="w-80 h-12 p-4 rounded-lg cursor-pointer bg-accent-primary hover:bg-accent-primary-pressed">
									Go to Login
								</Button>
							</Link>
						</>
					) : (
						<div className="flex flex-col items-center justify-center gap-4">
							<span className="text-3xl">Valid token</span>
						</div>
					)}
				</div>
			</div>
		</div>
	);
};

export default OAuth2FaCallback;

