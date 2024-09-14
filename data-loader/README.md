# Indexing Documents to a Vector Store with Azure Document Intelligence and Spring AI

## Overview
This application uses Azure Document Intelligence to analyze documents and index the data into a vector store using Spring AI.

## Dependencies
The following key dependencies are used in this project:

- Spring Shell
- Azure AI Form Recognizer (version 4.1.9)
- Spring AI PGVector Store Spring Boot Starter
- Spring AI OpenAI Spring Boot Starter


## Prerequisites
1. Azure Document Intelligence service is configured in Azure.
2. A local vector store is set up. This application uses a Postgres database with the pgvector extension.
3. The following tables need to be created in the Postgres database before running the application. Refer to the [DDL Scripts](#ddl-scripts) section at the end of this document for the necessary scripts.
       - `research_papers` and `research_papers_metadata`

## Steps to Run the Application
1. Clone the repository:
    ```sh
    git clone <repository-url>
    cd data-loader
    ```

2. Set up environment variables for Azure Document Intelligence and OpenAI:
    ```sh
    export DOCUMENT_INTELLIGENCE_ENDPOINT=<your-endpoint>
    export DOCUMENT_INTELLIGENCE_KEY=<your-key>
    export SPRING_AI_OPENAI_API_KEY=<your_Spring_AI_OpenAI_api_key>
    ```

3. Ensure that your Postgres database with the pgvector extension is running and accessible.

4. Update the `application.properties` file with the specific profile

5. Build and run the application using Maven:
    ```sh
    ./mvnw clean package -DskipTests
    java -jar target/data-loader-0.0.1-SNAPSHOT.jar
    ```

6. Upon running the application, you will see a shell prompt where you can type commands to interact with the application using the Terminal. Typing 'help' in the shell prompt will provide you with the available commands.
```
indexer:>help
AVAILABLE COMMANDS

Built-In Commands
       help: Display help about available commands
       stacktrace: Display the full stacktrace of the last error.
       clear: Clear the shell screen.
       quit, exit: Exit the shell.
       history: Display or save the history of previously run commands
       version: Show version info
       script: Read and execute commands from a file.

Default
       data load: Index documents to the Vector Store
       data delete: Deletes documents from the Vector Store
       data list: List files in the Vector store
```

You can know more about what each command does by typing ```help <command_name>``` or by typing ```<command_name> -h```.

Example -
```
indexer:>data load -h
NAME
       data load - Index documents to the Vector Store

SYNOPSIS
       data load [--path the path of the directory or file] --help

OPTIONS
       --path or -p the path of the directory or file
       [Mandatory]

       --help or -h
       help for data load
       [Optional]
```
## DDL-scripts

### `research_papers` table

```
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE research_papers(
    id uuid NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536)
);

CREATE INDEX ON research_papers USING HNSW (embedding vector_cosine_ops);
```

### `research_papers_metadata` table

```
CREATE TABLE research_papers_metadata(
    id SERIAL NOT NULL,
    title varchar(255) NOT NULL,
    file_name varchar(255) NOT NULL,
    metadata text,
    summary text,
    PRIMARY KEY(id)
);
```