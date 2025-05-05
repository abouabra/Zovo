import { NextRequest, NextResponse } from "next/server";
import { BASE_URI, API_BASE_URL } from "@/lib/callApi";

const publicPaths = [
    "/",
	"/auth/login",
	"/auth/register",
	"/auth/forgot-password",
];

export const config = {
	matcher: [
		"/((?!_next/|public/).*)",
	],
};

export async function middleware(req: NextRequest) {
	const pathname = req.nextUrl.pathname;

	if (publicPaths.some((path) => pathname === path || pathname.startsWith(path + "/"))) {
		console.log("Public path, no authentication required:", pathname);
		return NextResponse.next();
	}

	console.log("Protected path, checking authentication:", pathname);

	const token = req.cookies.get("ZSESSIONID");
	if (!token) {
		const loginUrl = req.nextUrl.clone();
		loginUrl.pathname = "/auth/login";
		return NextResponse.redirect(loginUrl);
	}

	try {
		const validateUrl = new URL(`${API_BASE_URL}/api/v1/auth/is-authenticated`, BASE_URI);
		const res = await fetch(validateUrl.toString(), {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
				Cookie: `ZSESSIONID=${token.value}`,
			},
		});

		if (!res.ok) {
			console.log("Authentication failed, redirecting to login");
			const loginUrl = req.nextUrl.clone();
			loginUrl.pathname = "/auth/login";
			return NextResponse.redirect(loginUrl);
		}
		console.log("Authentication successful, proceeding to:", pathname);
		return NextResponse.next();
	} catch (error) {
		console.error("Authentication error:", error);
		const loginUrl = req.nextUrl.clone();
		loginUrl.pathname = "/auth/login";
		return NextResponse.redirect(loginUrl);
	}
}
