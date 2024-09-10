# Backend Application

This is a demo project for a Spring Boot backend application.

## Prerequisites

Before running the application, make sure you have the following:

- Java 17 or higher installed
- Maven installed

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

3. Build the project using Maven:

    ```shell
    mvn clean install
    ```

4. Run the application:

    ```shell
    mvn spring-boot:run
    ```

## Configuration

The application uses the following key configuration properties:

- `spring.ai.openai.api-key`: The API key for OpenAI integration.
- `spring.ai.openai.embedding.api-key`: The API key for OpenAI embedding.
- `spring.ai.vectorstore.pgvector.schema-name`: The schema name for PGVector store.
- `spring.ai.vectorstore.pgvector.table-name`: The table name for PGVector store.


Please make sure to configure these properties accordingly in the `application.properties` file.

## Dependencies

This project has the following dependencies:

- Spring Boot Starter Actuator
- Spring Boot Starter Web
- Spring Boot DevTools
- Spring Boot Starter Test
- Spring AI OpenAI Spring Boot Starter
- Spring AI PGVector Store Spring Boot Starter
