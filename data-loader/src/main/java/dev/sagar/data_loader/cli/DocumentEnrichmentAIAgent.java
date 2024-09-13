package dev.sagar.data_loader.cli;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.ai.transformer.SummaryMetadataEnricher.SummaryType;

@Component
class DocumentEnrichmentAIAgent {
    private final ChatModel chatModel;
    private final JdbcClient jdbcClient;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentEnrichmentAIAgent.class);

    public static final String DEFAULT_SUMMARY_EXTRACT_TEMPLATE = """
            Here is the content of the section:
            {context_str}

            Summarize the introduction, abstract, key topics and entities of the section.

            Summary:""";

    DocumentEnrichmentAIAgent(ChatModel chatModel, JdbcClient jdbcClient) {
        this.chatModel = chatModel;
        this.jdbcClient = jdbcClient;
    }

    private List<Document> extractKeywords(List<Document> documents) {
        logger.info("Extracting keywords from the documents");
        var keywordMetadataEnricher = new KeywordMetadataEnricher(chatModel, 3);
        return keywordMetadataEnricher.apply(documents);
    }

    void storeResearchPapersInfoAndSummary(List<Document> documents) {
        logger.info("Storing research papers metadata and summary in the database");
        List<Document> keywords = extractKeywords(documents);

        var summarizesDocuments = new SummaryMetadataEnricher(chatModel, List.of(SummaryType.CURRENT),
                DEFAULT_SUMMARY_EXTRACT_TEMPLATE, MetadataMode.ALL).apply(documents);

        var title = documents.get(0).getMetadata().get("title");
        var fileName = documents.get(0).getMetadata().get("file_name");
        var enrichedMetadata = keywords.stream()
                .map(document -> (String) document.getMetadata().get("excerpt_keywords"))
                .flatMap(keyword -> Arrays.stream(keyword.split(",")))
                .distinct()
                .collect(Collectors.joining(","));
        var documentSumary = summarizesDocuments.stream()
                .map(document -> (String) document.getMetadata().get("section_summary"))
                .collect(Collectors.joining(System.lineSeparator()));

        String sql = "INSERT INTO research_papers_metadata (title, file_name, metadata, summary) VALUES (?, ?, ?, ?)";
        jdbcClient.sql(sql).params(title, fileName, enrichedMetadata, documentSumary).update();
    }
}
