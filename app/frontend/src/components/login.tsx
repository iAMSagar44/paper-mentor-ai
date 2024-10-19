"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Github, Laptop } from "lucide-react";
import Link from "next/link";

export function Login() {
  const [isLoading, setIsLoading] = useState<boolean>(false);

  return (
    <Card className="w-[350px]">
      <CardHeader>
        <CardTitle>Login</CardTitle>
        <CardDescription>Choose a provider to login with.</CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4 justify-center">
        <Link
          href={`${process.env.NEXT_PUBLIC_BACK_END_BASE_URL}/oauth2/authorization/azure`}
        >
          <Button variant="outline" disabled={isLoading}>
            <Laptop className="mr-2 h-4 w-4" />
            Login with Microsoft
          </Button>
        </Link>
        <Link
          href={`${process.env.NEXT_PUBLIC_BACK_END_BASE_URL}/oauth2/authorization/github`}
        >
          <Button variant="outline" disabled={isLoading}>
            <Github className="mr-2 h-4 w-4" />
            Login with GitHub
          </Button>
        </Link>
      </CardContent>
      <CardFooter>
        <p className="text-sm text-muted-foreground">
          By clicking continue, you agree to our Terms of Service and Privacy
          Policy.
        </p>
      </CardFooter>
    </Card>
  );
}
