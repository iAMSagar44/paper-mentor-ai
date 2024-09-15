package dev.sagar.ai;

import java.util.List;
import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class AnalyzeUserQueryAIAgent {
        Logger logger = org.slf4j.LoggerFactory.getLogger(AnalyzeUserQueryAIAgent.class);
        private final ChatClient chatClient;
        private final JdbcClient jdbcClient;

        public static final String KEYWORDS_TEMPLATE = """
                        Given the user question, extract 3 unique keywords to filter the database about article sources.
                        The keywords can only be from the list: {keywords}.
                        Format the keywords as comma-separated values.
                        Exclude any stop words.
                        Only extract the keywords and return it.
                        DO NOT answer the user question or provide any additional information.
                        """;

        AnalyzeUserQueryAIAgent(ChatClient.Builder chatClientBuilder, JdbcClient jdbcClient) {
                this.chatClient = chatClientBuilder.defaultOptions(OpenAiChatOptions.builder()
                                .withModel("gpt-4o-2024-08-06")
                                .withTemperature(0.1f)
                                .withMaxTokens(200)
                                .build())
                                .build();
                this.jdbcClient = jdbcClient;
        }

        AnalyzedSearchQuery analyzeSearchQuery(String userQuery) {
                var keywords = this.chatClient.prompt()
                                .system(sysSpec -> sysSpec
                                                .text(KEYWORDS_TEMPLATE)
                                                .param("keywords", retrieveMetadat()))
                                .user(userQuery)
                                .call()
                                .content();

                var titles = retrieveTitle(keywords);
                return new AnalyzedSearchQuery(userQuery, keywords, titles);
        }

        // Retrieve the top 3 titles based on the keywords search
        private List<String> retrieveTitle(String keywords) {
                String[] split = keywords.split(",");
                String sql = """
                                SELECT title FROM research_papers_metadata
                                WHERE to_tsvector('english', metadata) @@ (plainto_tsquery(:kw1)
                                || plainto_tsquery(:kw2)
                                || plainto_tsquery(:kw3))
                                ORDER BY ts_rank(to_tsvector('english', metadata), plainto_tsquery(:kw1)
                                || plainto_tsquery(:kw2)
                                || plainto_tsquery(:kw3)) DESC LIMIT 3
                                                """;
                return this.jdbcClient.sql(sql)
                                .param("kw1", split[0].trim())
                                .param("kw2", split[1].trim())
                                .param("kw3", split[2].trim())
                                .query(String.class)
                                .list();
        }

        // Retrieve the metadata for the LLM to select the keywords
        private List<String> retrieveMetadat() {
                return this.jdbcClient.sql("SELECT metadata FROM research_papers_metadata")
                                .query(String.class)
                                .list();
        }

        record AnalyzedSearchQuery(String userQuery, String keywords, List<String> titles) {
        }

}
