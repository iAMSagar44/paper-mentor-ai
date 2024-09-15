package dev.sagar.ai;

import java.util.function.Function;

import org.slf4j.Logger;
import org.springframework.context.annotation.Description;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

// AI Tool: summarizePaper
@Description("Provide a summary or abstract of a research paper only when the user explicitly requests it.")
@Component("summarizePaper")
public class SummarizePaper implements Function<SummarizePaper.Paper, SummarizePaper.Summary> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SummarizePaper.class);
    private final JdbcClient jdbcClient;

    public SummarizePaper(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Summary apply(Paper request) {
        logger.info("Summarizing paper. Paper name is: {} ", request.paperName());
        String summary = jdbcClient.sql("SELECT summary FROM research_papers_metadata WHERE title = :title")
                .param("title", request.paperName()).query(String.class).optional()
                .orElse("No summary found for the paper");
        return new Summary("Summary: " + summary);
    }

    record Paper(String paperName) {
    }

    record Summary(String summary) {
    }

}
