"use client";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { callApi } from "@/lib/callApi";
import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { useRouter } from "next/navigation";
import LoadingScreen from "@/components/loading-screen";
import { useTwoFAStore } from "@/stores/use2FAStore";
import { useUserStore } from "@/stores/useUserStore";

interface TwoFaResponse {
	token: string;
	provider: string;
}

export interface UserResponse {
	id: number;
	username: string;
	email: string;
}

const FormSchema = z.object({
	email: z.string().email({
		message: "Please enter a valid email address.",
	}),
	password: z.string().regex(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/, {
		message: "Password must contain at least one number, one lowercase letter, one uppercase letter, and be at least 8 characters long.",
	}),
});

export default function LoginForm() {
	const [showPassword, setShowPassword] = useState<boolean>(false);
	const router = useRouter();
	const [isLoading, setIsLoading] = useState<boolean>(false);
	const { setUserData } = useUserStore();
	const form = useForm<z.infer<typeof FormSchema>>({
		resolver: zodResolver(FormSchema),
		defaultValues: {
			email: "",
			password: "",
		},
	});

	const onSubmit = async (data: z.infer<typeof FormSchema>) => {
		try {
			setIsLoading(true);

			const res = await callApi<TwoFaResponse | UserResponse>("/auth/login", {
				method: "POST",
				body: JSON.stringify({
					email: data.email,
					password: data.password,
				}),
			});
			console.log("Login response:", res);
			if (res.code == "LOGIN_NEEDS_2FA") {
				const twoFA = res.details as TwoFaResponse;
				useTwoFAStore.getState().setTwoFAData({ token: twoFA.token });
				router.push("/auth/login-2fa");
			} else {
				const user = res.details as UserResponse;
				setUserData({
					id: user.id,
					username: user.username,
					email: user.email,
					avatar: "https://github.com/shadcn.png",
				});
				router.push("/home");
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
				<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 flex flex-col max-w-80">
					<FormField
						control={form.control}
						name="email"
						render={({ field }) => (
							<FormItem className="gap-4 flex flex-col">
								<FormLabel>Email</FormLabel>
								<FormControl>
									<Input placeholder="Enter your email" {...field} className="w-80 h-12 p-4 rounded-lg" />
								</FormControl>
								<FormMessage />
							</FormItem>
						)}
					/>
					<FormField
						control={form.control}
						name="password"
						render={({ field }) => (
							<FormItem>
								<FormLabel>Password</FormLabel>
								<FormControl>
									<div className="relative w-80 mb-1.5">
										<Input placeholder="Enter your password" type={showPassword ? "text" : "password"} {...field} className="h-12 p-4 pr-10 rounded-lg" />
										<div className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer text-muted-foreground" onClick={() => setShowPassword((prev) => !prev)}>
											{showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
										</div>
									</div>
								</FormControl>
								<FormMessage />
							</FormItem>
						)}
					/>
					<Button type="submit" className="w-80 h-12 p-4 rounded-lg cursor-pointer">
						Log in
					</Button>
				</form>
			</Form>
		</>
	);
}
