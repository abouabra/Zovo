import React from 'react'
import GoogleLogo from "@/components/svgs/GoogleLogo";
import GithubLogo from "@/components/svgs/GithubLogo";
import { Button } from '@/components/ui/button';

const providers = [
    {
        name: "Google",
        icon: GoogleLogo,
    },
    {
        name: "Github",
        icon: GithubLogo,
    }
];

const OAuthLogin = () => {
  return (
    <div className="flex flex-col items-center justify-center gap-4">
        {providers.map((provider) => (
            <Button key={provider.name} className="w-80 h-12 rounded-full border-2 border-bg-borders flex justify-start items-center gap-4" variant={'ghost'}>
                <provider.icon className="size-8" />
                <span className='text-high-emphasis'>
                    Continue with {provider.name}
                </span>
            </Button>
        ))}
    </div>
  )
}

export default OAuthLogin