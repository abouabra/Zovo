"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { callApi } from "@/lib/callApi";
import LoadingScreen from "@/components/loading-screen";

const FormSchema = z
	.object({
		username: z.string().min(4, {
			message: "Username must be at least 4 characters long.",
		}),
		email: z.string().email({
			message: "Please enter a valid email address.",
		}),
		password: z.string().regex(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/, {
			message: "Password must contain at least one number, one lowercase letter, one uppercase letter, and be at least 8 characters long.",
		}),
		passwordConfirmation: z.string().regex(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/, {
			message: "Password confirmation must contain at least one number, one lowercase letter, one uppercase letter, and be at least 8 characters long.",
		}),
	})
	.refine((data) => data.password === data.passwordConfirmation, {
		message: "Passwords do not match.",
		path: ["passwordConfirmation"], // show the error under the confirmPassword field
	});

interface RegisterFormProps {
	setIsEmailSent: (isRegistered: boolean) => void;
}

export default function EmailPasswordForm({ setIsEmailSent }: RegisterFormProps) {
	const [isLoading, setIsLoading] = useState(false);
	const [showPassword, setShowPassword] = useState<boolean>(false);
	const [showPasswordConfirmation, setShowPasswordConfirmation] = useState<boolean>(false);

	const form = useForm<z.infer<typeof FormSchema>>({
		resolver: zodResolver(FormSchema),
		defaultValues: {
			username: "",
			email: "",
			password: "",
			passwordConfirmation: "",
		},
	});

	const onSubmit = async (data: z.infer<typeof FormSchema>) => {
		try {
			setIsLoading(true);
			const res = await callApi("/auth/register", {
				method: "POST",
				body: JSON.stringify({
					username: data.username,
					email: data.email,
					password: data.password,
					passwordConfirmation: data.passwordConfirmation,
				}),
			});
			if (res.code === "SUCCESS") {
				setIsEmailSent(true);
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
						name="username"
						render={({ field }) => (
							<FormItem className="gap-4 flex flex-col">
								<FormLabel>Username</FormLabel>
								<FormControl>
									<Input placeholder="Enter your username" {...field} className="w-80 h-12 p-4 rounded-lg" />
								</FormControl>
								<FormMessage />
							</FormItem>
						)}
					/>
					<FormField
						control={form.control}
						name="email"
						render={({ field }) => (
							<FormItem className="gap-4 flex flex-col">
								<FormLabel>Email</FormLabel>
								<FormControl>
									<Input placeholder="Enter your email" type="email" {...field} className="w-80 h-12 p-4 rounded-lg" />
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
					<FormField
						control={form.control}
						name="passwordConfirmation"
						render={({ field }) => (
							<FormItem>
								<FormLabel>Password Confirmation</FormLabel>
								<FormControl>
									<div className="relative w-80 mb-1.5">
										<Input placeholder="Confirm your password" type={showPasswordConfirmation ? "text" : "password"} {...field} className="h-12 p-4 pr-10 rounded-lg" />
										<div className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer text-muted-foreground" onClick={() => setShowPasswordConfirmation((prev) => !prev)}>
											{showPasswordConfirmation ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
										</div>
									</div>
								</FormControl>
								<FormMessage />
							</FormItem>
						)}
					/>
					<Button type="submit" className="w-80 h-12 p-4 my-2 rounded-lg cursor-pointer">
						Register
					</Button>
				</form>
			</Form>
		</>
	);
}
