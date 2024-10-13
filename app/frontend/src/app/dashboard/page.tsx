import Link from "next/link";
import { headers } from "next/headers";
import { extractUserName } from "@/app/lib/extractUserName";

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-around p-24 space-y-2">
      <div className="place-self-center font-mono text-sm flex flex-col lg:flex-row lg:space-x-12 space-y-4 lg:space-y-0 items-center">
        <Link
          href="/chat"
          className="bg-blue-500 text-white px-6 py-3 rounded-full shadow-md hover:bg-blue-600 transition duration-300"
        >
          Continue to Chat
        </Link>
      </div>

      <div className="flex-col space-y-2 place-content-center before:absolute before:h-[300px] before:w-full before:-translate-x-1/2 before:rounded-full before:bg-gradient-radial before:from-white before:to-transparent before:blur-2xl before:content-[''] after:absolute after:-z-20 after:h-[180px] after:w-full after:translate-x-1/3 after:bg-gradient-conic after:from-sky-200 after:via-blue-200 after:blur-2xl after:content-[''] before:dark:bg-gradient-to-br before:dark:from-transparent before:dark:to-blue-700 before:dark:opacity-10 after:dark:from-sky-900 after:dark:via-[#0141ff] after:dark:opacity-40 sm:before:w-[480px] sm:after:w-[240px] before:lg:h-[360px]">
        <h1 className="text-4xl font-bold text-black place-self-center">
          Retrieval Augmented Generation based Chat Application
        </h1>
      </div>
      <div className="mt-8 mx-20">
        <h2 className="text-2xl font-semibold text-black text-center">
          Key Information About This Project:
        </h2>
        <ul className="list-disc list-inside text-lg text-gray-500 mt-4">
          <li>
            <strong>Data-Loader App:</strong> Use this app to upload and index
            your documents in the Vector Database. This app uses Azure Document
            Intelligence service to analyze the documents and then indexes them
            to a vector store. It handles the document processing and indexing
            tasks, ensuring that the data is ready for analysis. Please consult
            the instructions provided in the project&#39;s README file for
            guidance on running the application.
          </li>
          <br />
          <li>
            <strong>AI Chat Application:</strong> This is the main interface for
            users to interact with the system. It allows users to search,
            analyze, and extract insights from academic papers and research
            documents. Powered by AI, this application provides a seamless and
            intuitive experience for researchers, students, and professionals.
            Please consult the instructions provided in the project&#39;s README
            file for guidance on running the application.
          </li>
        </ul>
      </div>
    </main>
  );
}
