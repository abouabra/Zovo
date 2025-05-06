import { create } from "zustand";

type UserState = {
	id: number;
	username: string;
	email: string;
	setUserData: (data: { id: number; username: string; email: string}) => void;
	clear: () => void;
};

export const useUserStore = create<UserState>((set) => ({
	id: 0,
	username: "",
	email: "",
	setUserData: (data) => set({ id: data.id, username: data.username, email: data.email }),
	clear: () => set({ id: 0, username: "", email: "" }),
}));