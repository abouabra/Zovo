// useChatStore.ts
import { create } from "zustand";

// id: string;
// 	type: "personal" | "group";
// 	name: string;
// 	avatar: string;
// 	status: "online" | "offline" | "";

type ChatState = {
	id: string;
	type: "personal" | "group";
	name: string;
	avatar: string;
	status: "online" | "offline" | "";
	isChatOpen: boolean;
	setChatData: ({ id, type, name, avatar, status }: { id: string; type: "personal" | "group"; name: string; avatar: string; status: "online" | "offline" | "", isChatOpen: boolean }) => void;
	setIsChatOpen: (open: boolean) => void;
	clear: () => void;
	setChatIdNull: () => void;
};

export const useChatStore = create<ChatState>((set) => ({
	id: "",
	type: "personal",
	name: "",
	avatar: "",
	status: "",
	isChatOpen: false,
	setChatData: ({ id, type, name, avatar, status }) => set({ id, type, name, avatar, status, isChatOpen: true  }),
	setIsChatOpen: (open) => set({ isChatOpen: open }),
	clear: () => set({ id: "", type: "personal", name: "", avatar: "", status: "", isChatOpen: false  }),
	setChatIdNull: () => set({ id: "", type: "personal", name: "", avatar: "", status: "", isChatOpen: false}),
}));
