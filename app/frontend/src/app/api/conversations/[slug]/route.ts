import { cookies } from "next/headers";
import { NextRequest, NextResponse } from "next/server";

export async function DELETE(
  request: NextRequest,
  { params }: { params: { slug: string } }
) {
  const chatId = params.slug;
  const cookieStore = cookies();
  const JSSESSION = cookieStore.get("JSESSIONID");
  const csrfToken = cookieStore.get("XSRF-TOKEN");

  if (!csrfToken) {
    console.log("CSRF token not found");
    return NextResponse.json(
      { error: "CSRF token not found" },
      { status: 400 }
    );
  }

  try {
    const response = await fetch(
      `${process.env.BACK_END_BASE_URL}/api/conversations/${chatId}`,
      {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfToken.value,
          Cookie: `XSRF-TOKEN=${csrfToken?.value}; JSESSIONID=${JSSESSION?.value}`,
        },
        credentials: "include",
      }
    );

    if (response.status === 204) {
      console.log("Conversation deleted successfully");
      return NextResponse.json({ status: 204 });
    } else {
      console.log("Failed to delete conversation id :", chatId);
      return NextResponse.json(
        { error: "Failed to update messages" },
        { status: 500 }
      );
    }
  } catch (error) {
    console.error("Error deleting conversation id :", chatId, error);
    throw error;
  }
}
