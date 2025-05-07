// useChatStore.ts
import { create } from "zustand";

type ChatState = {
	id: number | null;
	isChatOpen: boolean;
	setChatData: (data: { id: number }) => void;
	setIsChatOpen: (open: boolean) => void;
	setChatIdNull: () => void;
};

export const useChatStore = create<ChatState>((set) => ({
	id: null,
	isChatOpen: false,
	setChatData: ({ id }) => set({ id, isChatOpen: true }),
	setChatIdNull: () => set({ id: null }),
	setIsChatOpen: (open) => set({ isChatOpen: open }),
}));
