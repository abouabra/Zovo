"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { InputOTP, InputOTPGroup, InputOTPSlot } from "@/components/ui/input-otp";
import { REGEXP_ONLY_DIGITS, REGEXP_ONLY_DIGITS_AND_CHARS } from "input-otp";
import { Switch } from "@/components/ui/switch";
import { useState } from "react";
import { useTwoFAStore } from "@/stores/use2FAStore";
import { callApi } from "@/lib/callApi";
import { useRouter } from "next/navigation";
import LoadingScreen from "@/components/loading-screen";

const OTP_SCHEMA = z.object({
	code: z
		.string()
		.length(6, {
			message: "OTP must be exactly 6 digits.",
		})
		.regex(/^\d+$/, "Only numbers allowed."),
});

const RECOVERY_SCHEMA = z.object({
	code: z
		.string()
		.length(8, {
			message: "Recovery code must be 8 characters.",
		})
		.regex(/^[a-zA-Z0-9]+$/, "Only alphanumeric characters."),
});

const TwoFaOtpInput = () => {
	const [useRecoveryCode, setUseRecoveryCode] = useState(false);
	const [isLoading, setIsLoading] = useState(false);
	const { token } = useTwoFAStore();
	const router = useRouter();

	const form = useForm<z.infer<typeof OTP_SCHEMA | typeof RECOVERY_SCHEMA>>({
		resolver: zodResolver(useRecoveryCode ? RECOVERY_SCHEMA : OTP_SCHEMA),
		defaultValues: {
			code: "",
		},
	});

	const onSubmit = async (data: z.infer<typeof OTP_SCHEMA | typeof RECOVERY_SCHEMA>) => {
		try {
			setIsLoading(true);
			console.log("Form submitted:", data);
			const res = await callApi("/auth/login-2fa", {
				method: "POST",
				body: JSON.stringify({
					token: token,
					code: data.code,
				}),
			});
			if (res.code === "SUCCESS") {
				console.log("2FA login successful:", res);
				useTwoFAStore.getState().clear();
				router.push("/home");
			} else {
			}
		} catch (err) {
			console.error("Login error:", err);
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<>
			{isLoading && <LoadingScreen />}
			<Form {...form}>
				<form onSubmit={form.handleSubmit(onSubmit)} className="min-w-96 flex flex-col justify-start space-y-6">
					<FormField
						control={form.control}
						name="code"
						render={({ field }) => (
							<FormItem className="flex flex-col items-center gap-6">
								<FormLabel className="text-xl">{useRecoveryCode ? "Recovery Code" : "One-Time Password"}</FormLabel>
								<FormControl>
									{useRecoveryCode ? (
										<InputOTP maxLength={8} pattern={REGEXP_ONLY_DIGITS_AND_CHARS} {...field}>
											<InputOTPGroup>
												{[...Array(8)].map((_, i) => (
													<InputOTPSlot key={i} index={i} className="h-12 w-12 text-xl" />
												))}
											</InputOTPGroup>
										</InputOTP>
									) : (
										<InputOTP maxLength={6} pattern={REGEXP_ONLY_DIGITS} {...field}>
											<InputOTPGroup>
												{[...Array(6)].map((_, i) => (
													<InputOTPSlot key={i} index={i} className="h-12 w-12 text-xl" />
												))}
											</InputOTPGroup>
										</InputOTP>
									)}
								</FormControl>
								<FormDescription>{useRecoveryCode ? "Enter one of the 8-character recovery code you saved." : "Enter the 6-digit code in your authenticator app."}</FormDescription>
								<FormMessage />
							</FormItem>
						)}
					/>
					<div className="flex items-center gap-2">
						<Switch checked={useRecoveryCode} onCheckedChange={setUseRecoveryCode} />
						<span className="text-sm">{useRecoveryCode ? "Use One-Time Password" : "Use Recovery Code"}</span>
					</div>

					<Button type="submit" className="w-full h-12 p-4 my-2 rounded-lg cursor-pointer">
						Submit
					</Button>					
				</form>
			</Form>
		</>
	);
};

export default TwoFaOtpInput;
