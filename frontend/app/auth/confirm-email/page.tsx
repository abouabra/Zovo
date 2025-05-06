"use client";

import React, { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { callApi } from "@/lib/callApi";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import AuthHeader from "@/components/auth/auth-header";
import ZovoLogo from "@/components/svgs/ZovoLogo";
import LoadingScreen from "@/components/loading-screen";

const ConfirmEmailPage = () => {
	const searchParams = useSearchParams();
	const token = searchParams.get("token");

	const [isLoading, setIsLoading] = useState(false);
	const [isSuccess, setIsSuccess] = useState(false);

	useEffect(() => {
		if (!token) return;

		const confirmEmail = async () => {
			try {
				setIsLoading(true);
				const res = await callApi("/auth/confirm-email", {
					method: "POST",
                    body: JSON.stringify({ token })
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

		confirmEmail();
	}, [token]);

	return (
		<>
			{isLoading && <LoadingScreen />}

			<div className="flex flex-col min-h-screen h-screen app-bg">
				<AuthHeader description="Already have an account?" link="/auth/login" />
				<div className="flex flex-col h-full items-center gap-8 p-4 ">
					<div className="flex flex-col items-center justify-center gap-4">
						<ZovoLogo className="w-2xs h-auto" />
						<span className="text-headline4">Email Confirmation</span>
					</div>

                    <div className="flex flex-col items-center justify-center gap-12 w-full pt-24">
                        {isSuccess ? (
                           <div className="flex flex-col items-center justify-center gap-4">
                                <span className="text-3xl">
                                    Email confirmation successful.
                                </span>
                                <span className="text-xl ">
                                    You can now log in to your account.
                                </span>
                            </div>
                        ) : (
                            <div className="flex flex-col items-center justify-center gap-4">
                                <span className="text-3xl">
                                    Email confirmation failed.
                                </span>
                                <span className="text-xl ">
                                    because the token is invalid or expired.
                                </span>
                            </div>
                        )}
                    
                        <Link href="/auth/login">
                            <Button variant="default" className="w-80 h-12 p-4 rounded-lg cursor-pointer bg-accent-primary hover:bg-accent-primary-pressed">
                                Go to Login
                            </Button>
                        </Link>
                    </div>
					
				</div>
			</div>
		</>
	);
};

export default ConfirmEmailPage;
