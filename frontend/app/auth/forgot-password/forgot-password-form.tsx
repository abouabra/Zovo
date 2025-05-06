"use client";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useState } from "react";
import { callApi } from "@/lib/callApi";
import LoadingScreen from "@/components/loading-screen";

const FormSchema = z.object({
	email: z.string().email({
		message: "Please enter a valid email address.",
	}),
});

interface RegisterFormProps {
	setIsEmailSent: (isRegistered: boolean) => void;
}

const ForgotPasswordForm = ({ setIsEmailSent }: RegisterFormProps) => {
	const [isLoading, setIsLoading] = useState(false);

	const form = useForm<z.infer<typeof FormSchema>>({
		resolver: zodResolver(FormSchema),
		defaultValues: {
			email: "",
		},
	});

	const onSubmit = async (data: z.infer<typeof FormSchema>) => {
		try {
			setIsLoading(true);

			console.log("Form submitted:", data);
			const res = await callApi("/auth/send-password-reset", {
				method: "POST",
				body: JSON.stringify({
					email: data.email,
				}),
			});
			if (res.code === "SUCCESS") {
				console.log("Register successful: ", res);
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
					<Button type="submit" className="w-80 h-12 p-4 my-2 rounded-lg cursor-pointer">
						Send Reset Link
					</Button>
				</form>
			</Form>
		</>
	);
};

export default ForgotPasswordForm;
