import { NextRequest, NextResponse } from "next/server";
import { BASE_URI } from "@/lib/callApi";


export const config = {
	matcher: [
		'/((?!_next/|public/|.*\\.(?:ico|png|jpg|jpeg|svg|css|js|json)).*)',
	],
};


const isPublicPath = (pathname: string): boolean => {
	return (
		pathname === "/" ||
		pathname.startsWith("/auth")
	);
};

export async function middleware(req: NextRequest) {
	const pathname = req.nextUrl.pathname;

	if (isPublicPath(pathname))
		return NextResponse.next();

	console.log("Protected path, checking authentication:", pathname);

	const token = req.cookies.get("ZSESSIONID");
	if (!token) {
		const loginUrl = req.nextUrl.clone();
		loginUrl.pathname = "/auth/login";
		return NextResponse.redirect(loginUrl);
	}

	try {
		const validateUrl = new URL(`${BASE_URI}/api/v1/auth/is-authenticated`, BASE_URI);
		const res = await fetch(validateUrl.toString(), {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
				Cookie: `ZSESSIONID=${token.value}`,
			},
		});

		if (!res.ok) {
			const loginUrl = req.nextUrl.clone();
			loginUrl.pathname = "/auth/login";
			return NextResponse.redirect(loginUrl);
		}
		return NextResponse.next();
	} catch {
		const loginUrl = req.nextUrl.clone();
		loginUrl.pathname = "/auth/login";
		return NextResponse.redirect(loginUrl);
	}
}
