"use client";
import React, { useState } from "react";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import RegisterForm from "@/app/auth/register/RegisterForm";
import OAuthLogin from "@/app/auth/login/oauth-login";

const RegisterPage = () => {
	const [isEmailSent, setIsEmailSent] = useState(false);
	return (
		<div className="flex flex-col min-h-screen h-screen app-bg">
			<AuthHeader description="Already have an account?" link="/auth/login" />
			<div className="flex flex-col h-full items-center gap-8 p-4 ">
				<div className="flex flex-col items-center justify-center gap-4">
					<ZovoLogo className="w-2xs h-auto" />
					<span className="text-headline4">Register</span>
				</div>
				{isEmailSent ? (
					<div className="flex flex-col items-center pt-32">
						<span className="text-4xl font-bold">We&apos;ve sent you an email to verify your account</span>
						<span className="text-2xl text-muted-foreground">Please check your inbox and follow the instructions to complete your registration.</span>
					</div>
				) : (
					<div className="flex justify-center items-center gap-4 flex-col md:flex-row relative">
						<RegisterForm setIsEmailSent={setIsEmailSent} />
						<div className="flex md:flex-col items-center justify-center gap-2">
							<div className="bg-borders h-0.5 min-w-35  md:w-0.5 md:min-w-0.5 md:min-h-35" />
							<span className="text-subtitle1">or</span>
							<div className="bg-borders h-0.5 min-w-35  md:w-0.5 md:min-w-0.5 md:min-h-35" />
						</div>
						<OAuthLogin />
					</div>
				)}
			</div>
		</div>
	);
};

export default RegisterPage;
