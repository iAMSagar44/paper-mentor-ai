import Head from "next/head";
import Link from "next/link";
import Image from "next/image";
import landing_image from "/public/mentor_image.png";

export default function Home() {
  return (
    <div className="flex flex-col min-h-screen">
      <Head>
        <title>Paper Mentor AI</title>
        <meta
          name="description"
          content="Your personal AI-powered research assistant."
        />
      </Head>

      <main className="flex-grow flex flex-col items-center justify-center min-h-screen bg-gray-50">
        <h1 className="text-6xl font-bold text-gray-800 mb-4">
          Paper Mentor AI
        </h1>
        <p className="text-xl font-light text-gray-600 mb-8">
          Empowering your research journey with AI.
        </p>

        <div className="mb-8">
          <Image
            src={landing_image}
            alt="AI-generated research assistant illustration"
            className="h-20 w-20 sm:h-40 sm:w-40 rounded-lg shadow-lg"
          />
          <p className="pl-4 text-sm text-gray-500 mt-2">Image created by AI</p>
        </div>

        <section className="max-w-4xl text-center mb-8">
          <h2 className="text-2xl font-semibold mb-4">
            What is Paper Mentor AI?
          </h2>
          <p className="text-lg text-gray-700">
            Paper Mentor AI is your intelligent research companion. It helps you
            search, analyze, and extract insights from thousands of academic
            papers, journals, and research documents. Whether you are a student
            working on a thesis or a researcher looking for in-depth analysis,
            Paper Mentor AI refines your search queries and provides relevant
            results in an easy-to-digest format.
          </p>
        </section>

        <Link
          href="/dashboard"
          className="bg-blue-500 text-white px-6 py-3 rounded-full shadow-md hover:bg-blue-600 transition duration-300"
        >
          Get Started
        </Link>
      </main>

      <footer className="w-full bg-gray-100 text-center py-4 text-gray-500">
        <p>&copy; {new Date().getFullYear()} Paper Mentor AI</p>
      </footer>
    </div>
  );
}
