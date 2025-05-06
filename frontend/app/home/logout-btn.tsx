"use client"
import { Button } from '@/components/ui/button'
import { callApi } from '@/lib/callApi';
import { useTwoFAStore } from '@/stores/use2FAStore';
import { useUserStore } from '@/stores/useUserStore';
import { useRouter } from 'next/navigation';
import React from 'react'

const LogoutBtn = () => {
    const router = useRouter();

    const handleClick = async () => {
            try {    
                const res = await callApi("/auth/logout", {
                    method: "POST"
                });
                if (res.code === "SUCCESS") {
                    useUserStore.getState().clear();
                    useTwoFAStore.getState().clear();
                    router.push("/auth/login");
                }
            } catch (err) {
                console.error("Login error:", err);
            }
        };
    return (
    <Button type="submit" onClick={handleClick} className="w-80 h-12 p-4 rounded-lg cursor-pointer">
        Logout
    </Button>
  )
}

export default LogoutBtn