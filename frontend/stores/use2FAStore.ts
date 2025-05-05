import { create } from "zustand";

type TwoFAState = {
	token: string;
	setTwoFAData: (data: { token: string }) => void;
	clear: () => void;
};

export const useTwoFAStore = create<TwoFAState>((set) => ({
	token: "",
	setTwoFAData: (data) => set({ token: data.token }),
	clear: () => set({ token: "" }),
}));
