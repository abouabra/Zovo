"use client";

import { useEffect } from "react";
import { useUserStore } from "@/stores/useUserStore";
import { UserType } from "@/constants/user-type";
import { callApi } from "@/lib/callApi";

export default function StoreInitializer() {
	const setUserData = useUserStore((s) => s.setUserData);

	useEffect(() => {
		callApi<UserType>("/users/me", {
			method: "GET",
		}).then((response) => {
			if (response.details) setUserData(response.details);
		});
	}, [setUserData]);

	return null; // this component renders nothing
}
