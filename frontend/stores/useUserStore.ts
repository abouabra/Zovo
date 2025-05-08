import { UserType } from "@/constants/user-type";
import { create } from "zustand";

type UserState = {
  user: UserType | null;
  setUserData: (data: UserType) => void;
  clear: () => void;
};

export const useUserStore = create<UserState>()((set) => ({
      user: null,
      setUserData: (data) => set({ user: data }),
      clear: () => set({ user: null }),
    })
);