'use client';

import { ModeToggle } from "@/components/toggle";
import TestBlock from "@/components/test-block";
import { Button } from "@/components/ui/button";
import Link from "next/link";


export default function Home() {

  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <h1 className="text-4xl font-bold">Welcome to Next.js!</h1>
      <p className="mt-4 text-lg">This is a simple Next.js application.</p>
      <ModeToggle />
      
      
      <Button variant="outline" className="mt-4">
          <Link href="/auth/login" className="text-blue-500">
            Go to Login Page
          </Link>
        </Button>


      <TestBlock />
    </main>
  );
}
