"use client";

import React, { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import LoadingScreen from "@/components/loading-screen";
import { z } from "zod";
import { callApi } from "@/lib/callApi";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Input } from "@/components/ui/input";

const TwoFaFormSchema = z
	.object({
		code: z.string().regex(/^\d+$/, "Only numbers allowed.").length(6, { message: "OTP must be exactly 6 digits." }),
	})
	.refine(
		async (data) => {
			try {
				const res = await callApi("/auth/2fa/verify", {
					method: "POST",
					body: JSON.stringify({
						code: data.code,
					}),
				});
				if (res.code === "SUCCESS") {
					return true;
				}
				return false;
			} catch {
				return false;
			}
		},
		{
			message: "The code is invalid or expired.",
			path: ["code"], // attach error to the `code` field
		}
	);

type TwoFaResponse = {
	uri: string;
	recoveryCodes: string[];
};

const EnabelTwoFaDialog = () => {
	const [open, setOpen] = useState(false);
	const [isLoading, setIsLoading] = useState(false);
	const [isTwoFaEnabled, setIsTwoFaEnabled] = useState(false);
	const [twoFaUri, setTwoFaUri] = useState("");
	const [recoveryCodes, setRecoveryCodes] = useState<string[]>([]);

	useEffect(() => {
		setIsLoading(true);
		const fetchTwoFaStatus = async () => {
			const res = await callApi("/auth/2fa/status");
			if (res.code !== "SUCCESS") {
				setIsLoading(false);
				return;
			}
			setIsTwoFaEnabled(res.message === "Enabled");
			setIsLoading(false);
		};

		fetchTwoFaStatus();
	}, []);

	const handleTwoFaClick = async () => {
		if (isTwoFaEnabled == false) {
			const res = await callApi<TwoFaResponse>("/auth/2fa/generate", {
				method: "GET",
			});
			if (res.code === "SUCCESS" && res.details) {
				setTwoFaUri(res.details.uri);
				setRecoveryCodes(res.details.recoveryCodes);
			}
		} else {
			const res = await callApi("/auth/2fa/disable", {
				method: "DELETE",
			});
			if (res.code === "SUCCESS") {
				setIsTwoFaEnabled(false);
				setTwoFaUri("");
				setRecoveryCodes([]);
			}
		}
	};
	const twoFaForm = useForm<z.infer<typeof TwoFaFormSchema>>({
		resolver: zodResolver(TwoFaFormSchema),
		defaultValues: {
			code: "",
		},
	});

	const TwoFaonSubmit = async (data: z.infer<typeof TwoFaFormSchema>) => {
		try {
			setIsLoading(true);

			const res = await callApi("/auth/2fa/enable", {
				method: "POST",
				body: JSON.stringify({
					code: data.code,
				}),
			});
			if (res.code === "SUCCESS") {
				setIsTwoFaEnabled(true);
				setTwoFaUri("");
				setRecoveryCodes([]);
				setOpen(false);
			}
		} catch (err) {
			console.error("Login error:", err);
		} finally {
			setIsLoading(false);
		}
	};
	const constructTwoFaQrCode = () => {
		if (!twoFaUri) return undefined;
		return `https://api.qrserver.com/v1/create-qr-code/?data=${encodeURIComponent(twoFaUri)}&size=256x256`;
	};

	const shouldShowDialog = open == true && isTwoFaEnabled == false;

	return (
		<>
			<Button
				onClick={() => {
					handleTwoFaClick();
                    if(isTwoFaEnabled == false) { setOpen(true) }
				}}
				className="w-80 h-12 p-4 mb-8 rounded-lg cursor-pointer"
			>
				{isTwoFaEnabled ? "Disable 2FA" : "Enable 2FA"}
			</Button>
			<Dialog open={shouldShowDialog} onOpenChange={setOpen}>
				<DialogContent className="sm:max-w-[425px]">
					{isLoading && <LoadingScreen />}
					<DialogHeader>
						<DialogTitle>Enable Two-Factor Authentication</DialogTitle>
						<DialogDescription>Two-Factor Authentication (2FA) adds an extra layer of security to your account. Please scan the QR code with your authenticator app and enter the code below.</DialogDescription>
					</DialogHeader>
					<DialogFooter className="overflow-y-auto max-h-[calc(100vh-200px)]">
						<Form {...twoFaForm}>
							<form onSubmit={twoFaForm.handleSubmit(TwoFaonSubmit)} className="space-y-6 flex flex-col max-w-80 m-4 h-full">
								<Avatar className="w-64 h-64 rounded-none self-center">
									<AvatarImage src={constructTwoFaQrCode()} />
									<AvatarFallback>{twoFaUri}</AvatarFallback>
								</Avatar>

								<div className="flex flex-col items-center">
									<p className="text-center text-sm text-muted-foreground">Bellow are your recovery codes. Store them in a safe place. You can use them to access your account if you lose access to your authenticator app.</p>
									<div className="flex flex-wrap justify-center gap-2 mt-4">
										{recoveryCodes.map((code, index) => (
											<div key={index} className="bg-muted p-2 rounded-md">
												<p className="text-sm text-muted-foreground">{code}</p>
											</div>
										))}
									</div>
								</div>

								<FormField
									control={twoFaForm.control}
									name="code"
									render={({ field }) => (
										<FormItem className="gap-4 flex flex-col">
											<FormLabel>Code</FormLabel>
											<FormControl>
												<Input placeholder="Enter your code" {...field} type="text" className="w-80 h-12 p-4 rounded-lg" />
											</FormControl>
											<FormMessage />
										</FormItem>
									)}
								/>
								<div className="flex items-center gap-2   justify-between">
									<DialogTrigger asChild className="flex ">
										<Button className="w-36 h-12 p-4 mb-2 rounded-lg cursor-pointer border-2 border-accent-primary bg-transparent text-high-emphasis hover:bg-accent-primary">Cancel</Button>
									</DialogTrigger>
									<Button type="submit" className="w-36 h-12 p-4 mb-2 rounded-lg cursor-pointer">
										Check
									</Button>
								</div>
							</form>
						</Form>
					</DialogFooter>
				</DialogContent>
			</Dialog>
		</>
	);
};

export default EnabelTwoFaDialog;
