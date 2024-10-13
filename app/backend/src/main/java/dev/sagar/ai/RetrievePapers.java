package dev.sagar.ai;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Description;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonClassDescription;

// AI Tool: findPapers
@Description("Find list of research and academic papers")
@Component("findPapers")
class RetrievePapers
                implements Function<RetrievePapers.UserQuery, RetrievePapers.Papers> {
        private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RetrievePapers.class);
        private final JdbcClient jdbcClient;
        private final ChatClient chatClient;
        private static final String USER_PROMPT = """
                        You are a research assistant helping the user find relevant academic papers.
                        Based on the user's question, infer which papers from the provided list are relevant.
                        If no papers match the user's question, inform them that no relevant papers were found.

                        Output Guidelines:
                        Provide the titles of the relevant papers based on the user's query.
                        If none of the papers are relevant, respond with: “No relevant papers were found for your question.”
                        ---------------------
                        User question: {user_query}
                        ---------------------
                        List of papers is below.
                        ---------------------
                        {paper_list}
                        ---------------------
                        """;

        public RetrievePapers(JdbcClient jdbcClient, ChatClient.Builder chatClientBuilder) {
                this.jdbcClient = jdbcClient;
                this.chatClient = chatClientBuilder.defaultOptions(OpenAiChatOptions.builder()
                                .withModel("gpt-4o-2024-08-06")
                                .withTemperature(0.2)
                                .withMaxTokens(200)
                                .build())
                                .build();
        }

        @Override
        public Papers apply(UserQuery request) {
                logger.info("Retrieving list of papers. User query is: {} ",
                                request.userQuery());
                List<String> products = jdbcClient.sql("SELECT title FROM research_papers_metadata").query(String.class)
                                .list();

                return this.chatClient.prompt()
                                .user(userSpec -> userSpec
                                                .text(USER_PROMPT)
                                                .param("user_query", request.userQuery())
                                                .param("paper_list", String.join("\n", products)))
                                .call()
                                .entity(Papers.class);
        }

        @JsonClassDescription("The user query")
        record UserQuery(String userQuery) {
        }

        @JsonClassDescription("List of papers")
        record Papers(List<String> paperNames) {
        }
}
