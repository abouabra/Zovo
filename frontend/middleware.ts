import { NextRequest, NextResponse } from "next/server";


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
		const res = await fetch("http://backend-container:8080/api/v1/auth/is-authenticated", {
			method: "GET",
			headers: {
				"Content-Type": "application/json",
				Cookie: `ZSESSIONID=${token.value}`,
			},
		});
		
		if (!res.ok) {
			console.log("Token validation failed, redirecting to login");
			const loginUrl = req.nextUrl.clone();
			loginUrl.pathname = "/auth/login";
			return NextResponse.redirect(loginUrl);
		}
		return NextResponse.next();
	} catch {
		console.error("Error during token validation ");
		const loginUrl = req.nextUrl.clone();
		loginUrl.pathname = "/auth/login";
		return NextResponse.redirect(loginUrl);
	}
}
