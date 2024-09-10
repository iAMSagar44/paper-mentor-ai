# Paper Mentor AI

Paper Mentor AI is an intelligent research assistant designed to help students, researchers, and professionals streamline their research process. This AI-powered tool helps users search, analyze, and extract insights from academic papers, journals, and research documents with ease.

## Features

- AI-Powered Search: Refine complex queries and retrieve relevant results from indexed documents.
- Document Analysis: Automatically analyze academic papers, research journals, and other documents.
- User-Friendly Interface: Simple and intuitive design for a smooth research experience.
- Backend: Built with Java and Spring framework for handling document processing and query generation.
- Get Started with Ease: A quick and easy process to begin exploring research papers.

## Application Structure

This application is divided into two main parts:

1. Data-Loader App: This app is responsible for analyzing and indexing documents to a vector store. It handles the document processing and indexing tasks, ensuring that the data is ready for analysis.
Please consult the instructions provided in the project's README file for guidance on running the application. 

2. AI Chat Application: The AI chat application is the main interface for users to interact with the system. It allows users to search, analyze, and extract insights from academic papers and research documents. Powered by AI, this application provides a seamless and intuitive experience for researchers, students, and professionals.
Please consult the instructions provided in the project's README file for guidance on running the application. 

By separating the data-loading and AI chat functionalities, this application ensures efficient and scalable processing of documents while providing a user-friendly interface for research tasks.

## Tech Stack

The tech stack for this application includes:

- Frontend: Next.js (React framework)
- Styling: Tailwind CSS for responsive and modern UI
- Backend: Java and Spring for backend services and API endpoints
- LLM Orchestration: Spring AI for orchestrating calls with LLMs and Vector Store
- Vector Store: Integration with Postgres vector store for document indexing
