import React from "react";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import TwoFaOtpInput from "./two-fa-otp-input";

const TwoFaPage = () => {
	return (
		<div className="flex flex-col min-h-screen h-screen app-bg">
			<AuthHeader description="Create an account" link="/auth/register" />
			<div className="flex flex-col h-full items-center gap-12 p-8">
				<div className="flex flex-col items-center justify-center gap-8">
					<ZovoLogo className="w-2xs h-auto" />
					<span className="text-headline4">Two Factor Authentication</span>
				</div>
				<div className="flex justify-center items-center gap-4">
					<div className="flex flex-col items-center justify-center gap-2">
						<TwoFaOtpInput />
					</div>
				</div>
			</div>
		</div>
	);
};

export default TwoFaPage;
