# Backend Application

## Introduction

This is a Spring Boot application built using the Spring AI project, designed to facilitate interaction between a Vector Database and Generative AI models.

### Key Features

1. **Retrieval Augmented Generation (RAG):** Enhances response accuracy by fetching relevant information from a vector store before generating answers.
2. **Function Calling:** Allows the AI to execute specific tasks or retrieve structured data via dynamic function invocations.
3. **AI Response Chat Streaming:** Delivers real-time, streaming AI responses for a seamless chat experience.


## Prerequisites

Before running the application, make sure you have the following:

1. Java 17 or higher installed
2. Maven installed
3. A local vector store is set up. This application uses a Postgres database with the pgvector extension.
4. The following tables need to be present in the Postgres database before running the application. Refer to the README.md file in the `data-loader` project for details around setting up these tables.
       - `research_papers` and `research_papers_metadata`

## Getting Started

To run the application, follow these steps:

1. Clone the repository:

    ```shell
    git clone https://github.com/iAMSagar44/paper-mentor-ai.git
    ```

2. Navigate to the project directory:

    ```shell
    cd app/backend
    ```
3. Set up environment variables for OpenAI:
    ```sh
    export SPRING_AI_OPENAI_API_KEY=<your_Spring_AI_OpenAI_api_key>
    ```

4. Ensure that your Postgres database with the pgvector extension is running and accessible.

5. Update the `application.properties` file with the specific profile

6. Build the project using Maven:

    ```shell
    mvn clean install
    ```

7. Run the application:

    ```shell
    mvn spring-boot:run
    ```

## Configuration

The application uses the following key configuration properties:

- `spring.ai.openai.api-key`: The API key for OpenAI integration.
- `spring.ai.openai.embedding.api-key`: The API key for OpenAI embedding.
- `spring.ai.openai.chat.options.model`: The Open AI LLM to use.
- `spring.ai.vectorstore.pgvector.schema-name`: The schema name for PGVector store.
- `spring.ai.vectorstore.pgvector.table-name`: The table name for PGVector store.


Please make sure to configure these properties accordingly in the `application.properties` and the `application-openai.properties` file.

## Dependencies

This project has the following dependencies:

- Spring Boot Starter Actuator
- Spring Boot Starter Web
- Spring Boot DevTools
- Spring Boot Starter Test
- Spring AI OpenAI Spring Boot Starter
- Spring AI PGVector Store Spring Boot Starter
