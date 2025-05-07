"use client";
import React, { useState } from "react";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import ForgotPasswordForm from "./forgot-password-form";

const RegisterPage = () => {
	const [isEmailSent, setIsEmailSent] = useState(false);
	return (
		<div className="flex flex-col min-h-screen h-screen app-bg">
			<AuthHeader description="Already have an account?" link="/auth/login" />
			<div className="flex flex-col h-full items-center gap-8 p-4 ">
				<div className="flex flex-col items-center justify-center gap-4">
					<ZovoLogo className="w-2xs h-auto" />
					<span className="text-headline4">Forgot Password</span>
				</div>
				{isEmailSent ? (
					<div className="flex flex-col items-center pt-32">
						<span className="text-4xl font-bold">We&apos;ve sent you an email to reset your password</span>
						<span className="text-2xl text-muted-foreground">Please check your inbox and follow the instructions to reset your password.</span>
						<span className="text-2xl text-muted-foreground">If you don&apos;t see the email, check your spam folder.</span>
					</div>
				) : (
					<div className="flex justify-center items-center gap-4 flex-col md:flex-row relative pt-32">
						<ForgotPasswordForm setIsEmailSent={setIsEmailSent} />
					</div>
				)}
			</div>
		</div>
	);
};

export default RegisterPage;
