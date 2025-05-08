import { ChannelType } from "@/constants/channel-type";
import { create } from "zustand";

type ChannelsSidebarState = {
  channels: ChannelType[];
  activeChannelId: string | null;
  setActiveChannelId: (id: string | null) => void;
  getActiveChannel: () => ChannelType | undefined;
  setChannels: (channels: ChannelType[]) => void;
  addChannel: (channel: ChannelType) => void;
  updateChannel: (id: string, data: Partial<ChannelType>) => void;
  removeChannel: (id: string) => void;
  clearChannels: () => void;
};

export const useChannelsSidebarStore = create<ChannelsSidebarState>((set, get) => ({
  channels: [],
  activeChannelId: null,
  getActiveChannel: () => {
    return get().channels.find(
      (channel) => channel.id === get().activeChannelId
    );
  },

  setActiveChannelId: (id) => set({ activeChannelId: id }),

  setChannels: (channels) => set({ channels }),

  addChannel: (channel) =>
    set((state) => {
      if (!state.channels.some((existingChannel) => existingChannel.id === channel.id)) {
        return { channels: [...state.channels, channel] };
      }
      return state;
    }),

  updateChannel: (id, data) =>
    set((state) => ({
      channels: state.channels.map((channel) =>
        channel.id === id ? { ...channel, ...data } : channel
      ),
    })),

  removeChannel: (id) =>
    set((state) => ({
      channels: state.channels.filter((channel) => channel.id !== id),
    })),

  clearChannels: () => set({ channels: [] }),
}));
