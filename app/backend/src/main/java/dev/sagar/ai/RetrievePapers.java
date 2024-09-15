package dev.sagar.ai;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
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

    public RetrievePapers(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Papers apply(UserQuery request) {
        logger.info("Retrieving list of papers. User query is: {} ",
                request.userQuery());
        List<String> products = jdbcClient.sql("SELECT title FROM research_papers_metadata").query(String.class).list();
        return new Papers(products);
    }

    @JsonClassDescription("The user query")
    record UserQuery(String userQuery) {
    }

    @JsonClassDescription("List of papers")
    record Papers(List<String> paperNames) {
    }
}
