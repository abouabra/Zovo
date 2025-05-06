"use client";

import React, { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { callApi } from "@/lib/callApi";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import LoadingScreen from "@/components/loading-screen";
import PasswordResetForm from "./password-reset-form";

const PasswordResetPage = () => {
	const searchParams = useSearchParams();
	const token = searchParams.get("token");

	const [isLoading, setIsLoading] = useState(false);
	const [isSuccess, setIsSuccess] = useState(false);
	const [isPasswordChanged, setIsPasswordChanged] = useState(false);

	useEffect(() => {
		if (!token) return;

		const passwordReset = async () => {
			try {
				setIsLoading(true);
				const res = await callApi("/auth/verify-password-reset-token", {
					method: "POST",
					body: JSON.stringify({ token }),
				});
				if (res.code === "SUCCESS") {
					setIsSuccess(true);
				}
			} catch (err) {
				console.error("Confirm email error:", err);
				setIsSuccess(false);
			} finally {
				setIsLoading(false);
			}
		};

		passwordReset();
	}, [token]);

	if (!token) return null;

	return (
		<>
			{isLoading && <LoadingScreen />}

			<div className="flex flex-col min-h-screen h-screen app-bg">
				<AuthHeader description="Already have an account?" link="/auth/login" />
				<div className="flex flex-col h-full items-center gap-8 p-4 ">
					<div className="flex flex-col items-center justify-center gap-4">
						<ZovoLogo className="w-2xs h-auto" />
						<span className="text-headline4">Password Reset</span>
					</div>

					<div className="flex flex-col items-center justify-center gap-12 w-full pt-24">
						{isSuccess ? (
							<>
								{isPasswordChanged ? (
									<>
										<div className="flex flex-col items-center justify-center gap-4">
											<span className="text-3xl">Password Changed Successfully.</span>
											<span className="text-xl ">You can now log in to your account.</span>
										</div>

										<Link href="/auth/login">
											<Button variant="default" className="w-80 h-12 p-4 rounded-lg cursor-pointer bg-accent-primary hover:bg-accent-primary-pressed">
												Go to Login
											</Button>
										</Link>
									</>
								) : (
									<PasswordResetForm token={token as string} setIsPasswordChanged={setIsPasswordChanged} />
								)}
							</>
						) : (
							<>
								<div className="flex flex-col items-center justify-center gap-4">
									<span className="text-3xl">Password reset failed.</span>
									<span className="text-xl ">The password reset link is invalid or has expired.</span>
								</div>

								<Link href="/auth/login">
									<Button variant="default" className="w-80 h-12 p-4 rounded-lg cursor-pointer bg-accent-primary hover:bg-accent-primary-pressed">
										Go to Login
									</Button>
								</Link>
							</>
						)}
					</div>
				</div>
			</div>
		</>
	);
};

export default PasswordResetPage;
