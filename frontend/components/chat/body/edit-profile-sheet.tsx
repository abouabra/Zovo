"use client";
import React, { useState } from "react";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { API_BASE_URL, callApi } from "@/lib/callApi";
import LoadingScreen from "@/components/loading-screen";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Eye, EyeOff } from "lucide-react";
import { useUserStore } from "@/stores/useUserStore";
import { toast } from "sonner";
import EnabelTwoFaDialog from "./enable-two-fa-dialog";

const passwordRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/;

const FormSchema = z
	.object({
		avatar: z.null(),
		username: z.string().min(4, { message: "Username must be at least 4 characters long." }),
		email: z.string().email({ message: "Please enter a valid email address." }),
		password: z.string().optional(),
		passwordConfirmation: z.string().optional(),
	})
	.superRefine((data, ctx) => {
		const { password, passwordConfirmation } = data;

		if (!password && !passwordConfirmation) return;

		if (!password) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: "Password is required when confirming.",
				path: ["password"],
			});
		}
		if (!passwordConfirmation) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: "Please confirm your password.",
				path: ["passwordConfirmation"],
			});
		}

		if (!password || !passwordConfirmation) return;

		if (!passwordRegex.test(password)) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: "Password must contain at least one number, one lowercase letter, one uppercase letter, and be at least 8 characters long.",
				path: ["password"],
			});
		}
		if (!passwordRegex.test(passwordConfirmation)) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: "Password confirmation must contain at least one number, one lowercase letter, one uppercase letter, and be at least 8 characters long.",
				path: ["passwordConfirmation"],
			});
		}

		if (password !== passwordConfirmation) {
			ctx.addIssue({
				code: z.ZodIssueCode.custom,
				message: "Passwords do not match.",
				path: ["passwordConfirmation"],
			});
		}
	});


const EditProfile = () => {
	const { user, setUserData } = useUserStore();
	const [isLoading, setIsLoading] = useState<boolean>(false);
	const [showPassword, setShowPassword] = useState<boolean>(false);
	const [showPasswordConfirmation, setShowPasswordConfirmation] = useState<boolean>(false);

	const form = useForm<z.infer<typeof FormSchema>>({
		resolver: zodResolver(FormSchema),
		defaultValues: {
			avatar: null,
			username: user?.username,
			email: user?.email,
			password: "",
			passwordConfirmation: "",
		},
	});

	const MAX_SIZE = 3 * 1024 * 1024; // 3 MB

	const onFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
		const file = e.target.files?.[0];
		if (!file || !user) return;
		setIsLoading(true);

		if (!["image/jpeg", "image/png"].includes(file.type)) {
			toast("BAD_FILE_TYPE", {
				description: "Only JPG and PNG images are allowed.",
			});
			setIsLoading(false);
			return;
		}

		if (file.size > MAX_SIZE) {
			toast("FILE_TOO_LARGE", {
				description: "File size exceeds 3MB limit.",
			});
			setIsLoading(false);
			return;
		}

		const formData = new FormData();
		formData.append("avatar", file);

		const res = await fetch(`${API_BASE_URL}/users/update-avatar`, {
			method: "POST",
			body: formData,
		});

		const data = await res.json();
		if (data.code === "SUCCESS") {
			setUserData({
				...user,
				avatar: URL.createObjectURL(file),
			});
		} else {
			toast(data.code, {
				description: data.message,
			});
		}
		setIsLoading(false);
	};

	const onSubmit = async (data: z.infer<typeof FormSchema>) => {
		try {
			setIsLoading(true);

			const res = await callApi("/users/update", {
				method: "PUT",
				body: JSON.stringify({
					username: data.username,
					email: data.email,
					password: data.password,
					passwordConfirmation: data.passwordConfirmation,
				}),
			});
			if (res.code === "SUCCESS") {
				if (user) {
					setUserData({
						...user,
						username: data.username,
						email: data.email,
					});
				}
			}
		} catch (err) {
			console.error("Login error:", err);
		} finally {
			setIsLoading(false);
		}
	};


	return (
		<>
			<Sheet>
				<SheetTrigger asChild>
					<div className="flex w-full gap-4 items-center cursor-pointer py-3 px-4 border-b bg-bars-bg hover:bg-borders/75">
						<Avatar className="w-8 h-8">
							<AvatarImage src={user?.avatar} />
							<AvatarFallback>{user?.username}</AvatarFallback>
						</Avatar>
						<span className="font-bold select-none">{user?.username}</span>
					</div>
				</SheetTrigger>
				<SheetContent className="overflow-y-auto  min-h-screen">
					<SheetHeader>
						<SheetTitle>Edit profile</SheetTitle>
						<SheetDescription>Make changes to your profile here. Click save when youre done.</SheetDescription>
					</SheetHeader>
					{isLoading && <LoadingScreen />}
					<Form {...form}>
						<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 flex flex-col max-w-80 m-4 h-full">
							<FormField
								control={form.control}
								name="avatar"
								render={() => (
									<FormItem className="gap-4 flex flex-col">
										<FormLabel>avatar</FormLabel>
										<FormControl>
											<label className="relative w-24 h-24 group cursor-pointer">
												<input type="file" accept=".jpg,.jpeg,.png" className="hidden" onChange={onFileChange} />

												<Avatar className="w-full h-full">
													<AvatarImage src={user?.avatar} />
													<AvatarFallback>{user?.username}</AvatarFallback>
												</Avatar>

												<div className=" absolute inset-0 bg-app-bg/75 flex flex-col items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200 rounded-full">
													<span className="text-white text-sm">Change</span>
													<span className="text-white text-sm">Picture</span>
												</div>
											</label>
										</FormControl>
										<FormMessage />
									</FormItem>
								)}
							/>
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
							
							<EnabelTwoFaDialog />
							<Button type="submit" className="w-80 h-12 p-4 mb-2 mt-auto rounded-lg cursor-pointer">
								Update
							</Button>
						</form>
					</Form>
				</SheetContent>
			</Sheet>
		</>
	);
};

export default EditProfile;
