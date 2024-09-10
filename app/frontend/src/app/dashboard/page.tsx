import Image from "next/image";
import Link from "next/link";
import { headers } from "next/headers";
import { extractUserName } from "@/app/lib/extractUserName";

export default function Home() {

  const clientPrincipal: string | null = headers().get("X-MS-CLIENT-PRINCIPAL");
  let claims;
  if (!clientPrincipal) {
    console.log("clientPrincipal is not set in the home page");
  }
  else {
    console.log("clientPrincipal is set in the home page");
    claims = extractUserName(clientPrincipal);
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-around p-24 space-y-2">
      <div className="place-self-center font-mono text-sm flex flex-col lg:flex-row lg:space-x-12 space-y-4 lg:space-y-0 items-center">
        {/* <Link
          href="/assistant"
          className="rounded-md bg-indigo-500 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-400 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        >
          Chat
        </Link> */}
        <Link
          href="/chat"
          className="rounded-md bg-indigo-500 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-400 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        >
          Enter Application
        </Link>
      </div>

      <div className="flex-col space-y-2 place-content-center before:absolute before:h-[300px] before:w-full before:-translate-x-1/2 before:rounded-full before:bg-gradient-radial before:from-white before:to-transparent before:blur-2xl before:content-[''] after:absolute after:-z-20 after:h-[180px] after:w-full after:translate-x-1/3 after:bg-gradient-conic after:from-sky-200 after:via-blue-200 after:blur-2xl after:content-[''] before:dark:bg-gradient-to-br before:dark:from-transparent before:dark:to-blue-700 before:dark:opacity-10 after:dark:from-sky-900 after:dark:via-[#0141ff] after:dark:opacity-40 sm:before:w-[480px] sm:after:w-[240px] before:lg:h-[360px]">
        <h1 className="text-4xl font-bold text-black place-self-center">Generative AI Chat Application</h1>
        <div>
          {claims && (
            <p className="mx-20 pt-5 text-lg text-gray-500 text-center font-semibold">Welcome {claims["name"]}</p>
          )}
        </div>
      </div>
      <div className="mt-8 mx-20">
          <h2 className="text-2xl font-semibold text-black text-center">Important Information:</h2>
          <ul className="list-disc list-inside text-lg text-gray-500 mt-4">
            <li>Your prompts (inputs) and completions (outputs), your embeddings, and your training data:</li>
            <ul className="list-disc list-inside ml-5">
              <li>are NOT available to other customers.</li>
              <li>are NOT available to OpenAI.</li>
              <li>are NOT used to improve OpenAI models.</li>
              <li>are NOT used to improve any Microsoft or 3rd party products or services.</li>
              <li>are NOT used for automatically improving Azure OpenAI models for your use in your resource (The models are stateless, unless you explicitly fine-tune models with your training data).</li>
            </ul>
            <li>The Azure OpenAI Service is fully controlled by Microsoft; Microsoft hosts the OpenAI models in their Azure environment and the Service does NOT interact with any services operated by OpenAI (e.g. ChatGPT, or the OpenAI API).</li>
          </ul>
        </div>
    </main>
  );
}
