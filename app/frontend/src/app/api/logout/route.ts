import { NextResponse } from "next/server";
import { cookies } from "next/headers";

export async function POST() {
  console.log("Logging out user - route handler");
  const cookieStore = cookies();
  const csrfToken = cookieStore.get("XSRF-TOKEN");
  const JSSESSION = cookieStore.get("JSESSIONID");

  if (!csrfToken) {
    console.log("CSRF token not found");
    return NextResponse.json(
      { error: "CSRF token not found" },
      { status: 400 }
    );
  }

  const response = await fetch(`${process.env.BACK_END_BASE_URL}/logout`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-XSRF-TOKEN": csrfToken.value,
      Cookie: `XSRF-TOKEN=${csrfToken?.value}; JSESSIONID=${JSSESSION?.value}`,
    },
    credentials: "include",
  });

  if (!response.ok) {
    return NextResponse.json(
      { error: "Failed to logout" },
      { status: response.status }
    );
  }

  if (response.redirected) {
    cookieStore.delete("JSESSIONID");
    cookieStore.delete("XSRF-TOKEN");
    return NextResponse.json({ redirectUrl: response.url });
  }
}
