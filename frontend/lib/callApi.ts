import { toast } from "sonner"

// const API_BASE_URL = process.env.NEXT_PUBLIC_BACKEND_URL as string;
export const BASE_URI = "https://localhost" as string;
export const API_BASE_URL = "/api/v1" as string;

export interface ApiResponse<T = unknown> {
  message: string | null;
  code: string;
  details: T | null;
}

export async function callApi<T>(path: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    credentials: "include",
  });

  const data = (await res.json()) as ApiResponse<T>;

  // await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate a delay

  if (!res.ok) {
    toast(data.code, {
      description: data.message
    })
    throw new Error(data.message || "Something went wrong");
    // if (Array.isArray(data.details)) {
    //   data.details.forEach(element => {
    //     toast(data.code, {
    //       description: element,
    //     })
    //   });
    // }
  }
  return data;
}
