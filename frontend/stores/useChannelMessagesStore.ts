import { MessageType } from "@/constants/message-type";
import { create } from "zustand";

type MessagesState = {
  messages: MessageType[];
  setMessages: (messages: MessageType[]) => void;

  addMessage: (message: MessageType) => void;
  updateMessage: (id: string, data: MessageType) => void;
  removeMessage: (id: string) => void;
};

export const useMessagesStore = create<MessagesState>((set) => ({
  messages: [],

  setMessages: (messages) => set({ messages }),

  addMessage: (message) =>
    set((state) => ({
      messages: [...state.messages, message],
    })),

  updateMessage: (id, data) =>
    set((state) => ({
      messages: state.messages.map((message) =>
        message.id === id ? { ...message, ...data } : message
      ),
    })),

  removeMessage: (id) =>
    set((state) => ({
      messages: state.messages.filter((message) => message.id !== id),
    })),
}));
