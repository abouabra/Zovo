import React from "react";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import EmailPasswordForm from "./EmailPasswordForm";
import OAuthLogin from "./OAuthLogin";
import Link from "next/link";

const LoginPage = () => {
	return (
		<div className="flex flex-col min-h-screen h-screen app-bg">
			<AuthHeader description="Create an account" link="/auth/register" />
			<div className="flex flex-col h-full items-center gap-12 p-8">
				<div className="flex flex-col items-center justify-center gap-8">
					<ZovoLogo className="w-2xs h-auto" />
					<span className="text-headline4">Log in</span>
				</div>
				<div className="flex justify-center items-center gap-4">
					<EmailPasswordForm />
					<div className="flex flex-col items-center justify-center gap-2">
						<div className="bg-borders w-0.5 min-h-35" />
						<span className="text-subtitle1">or</span>
						<div className="bg-borders w-0.5 min-h-35" />
					</div>
					<OAuthLogin />
				</div>
				<div className="flex flex-col items-center justify-center gap-2">
					<Link href="/auth/forgot-password">
						<span className="text-subtitle1 underline">
							Forgot password ?
						</span>
					</Link>
				</div>
			</div>
		</div>
	);
};

export default LoginPage;
