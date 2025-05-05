import React from "react";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import RegisterForm from "@/app/auth/register/RegisterForm";
import OAuthLogin from "@/app/auth/login/OAuthLogin";

const RegisterPage = () => {
	return (
		<div className="flex flex-col min-h-screen h-screen app-bg">
			<AuthHeader description="Already have an account?" link="/auth/login" />
			<div className="flex flex-col h-full items-center gap-8 p-4">
				<div className="flex flex-col items-center justify-center gap-4">
					<ZovoLogo className="w-2xs h-auto" />
					<span className="text-headline4">Register</span>
				</div>
				<div className="flex justify-center items-center gap-4">
					<RegisterForm />
					<div className="flex flex-col items-center justify-center gap-2">
						<div className="bg-borders w-0.5 min-h-35" />
						<span className="text-subtitle1">or</span>
						<div className="bg-borders w-0.5 min-h-35" />
					</div>
					<OAuthLogin />
				</div>
			</div>
		</div>
	);
};

export default RegisterPage;
