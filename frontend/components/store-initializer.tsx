"use client";

import { useEffect } from "react";
import { useUserStore } from "@/stores/useUserStore";
import { API_BASE_URL } from "@/lib/callApi";

export default function StoreInitializer() {
	const { setUserData } = useUserStore();
	useEffect(() => {
		const fetchUserData = async () => {
			try {
				const res = await fetch(`${API_BASE_URL}/users/me`, {
					method: "GET",
					credentials: "include",
					headers: {
						"Content-Type": "application/json",
					},
				});

				if (res.status === 401) {
					setUserData(null);
					return;
				}

				const data = await res.json();

				if (data.code === "SUCCESS") {
					setUserData(data.details);
				} else {
					setUserData(null);
				}
			} catch (error) {
				console.error("Error fetching user data:", error);
				setUserData(null);
			}
		};

		fetchUserData();
	}, [setUserData]);

	return null; // this component renders nothing
}
