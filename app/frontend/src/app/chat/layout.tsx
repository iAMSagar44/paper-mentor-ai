import Navbar from "@/app/ui/chat-landing/navbar";
import { extractUserName } from "../lib/extractUserName";
import { headers } from "next/headers";

export default function Layout({ children }: { children: React.ReactNode }) {
  const clientPrincipal: string | null = headers().get("X-MS-CLIENT-PRINCIPAL");
  let claims;
  if (!clientPrincipal) {
    console.log("clientPrincipal is not set in the assistant layout page");
  } else {
    console.log("clientPrincipal is set in the assistant layout page");
    claims = extractUserName(clientPrincipal);
  }
  return (
    <div className="flex-col">
      <div>
        <Navbar userName={claims ? claims.preferred_username : "Guest User"} />
      </div>
      <div>{children}</div>
    </div>
  );
}
