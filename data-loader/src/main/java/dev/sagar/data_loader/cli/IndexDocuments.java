package dev.sagar.data_loader.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class IndexDocuments {

    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final AnalyseDocumentService analyseDocumentService;
    private final JdbcClient jdbcClient;

    private static final Logger logger = LoggerFactory.getLogger(IndexDocuments.class);

    public IndexDocuments(VectorStore vectorStore, AnalyseDocumentService analyseDocumentService, ChatModel chatModel,
            JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.analyseDocumentService = analyseDocumentService;
        this.chatModel = chatModel;
        this.jdbcClient = jdbcClient;
    }

    void load(Path folderPath) {
        String extension = getFileExtension(folderPath);
        if (extension.equals("pdf")) {
            logger.info("Processing pdf file {}", folderPath.getFileName().toString());

            // get the parent directory of the file
            logger.debug("Parent directory of the file is: {}", folderPath.getParent().toString());

            try {
                byte[] fileContent = Files.readAllBytes(folderPath);
                List<Document> analysedDocuments = analyseDocumentService.analyseDocument(fileContent,
                        folderPath.getFileName().toString());

                var tokenTextSplitter = new TokenTextSplitter();
                final var splitDocuments = tokenTextSplitter.apply(analysedDocuments);

                logger.info(
                        "Parsing document, creating embeddings and storing in vector store.... this might take a while.");
                vectorStore.add(splitDocuments);
                logger.info("Vector Store indexing completed");

                KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(chatModel, 3);
                List<Document> enrichedDocuments = keywordMetadataEnricher
                        .apply(analysedDocuments.stream().limit(2).toList());

                var enrichedMetadata = enrichedDocuments.stream()
                        .map(document -> (String) document.getMetadata().get("excerpt_keywords"))
                        .flatMap(keyword -> Arrays.stream(keyword.split(",")))
                        .distinct()
                        .collect(Collectors.joining(","));

                var title = enrichedDocuments.get(0).getMetadata().get("title");
                var fileName = enrichedDocuments.get(0).getMetadata().get("file_name");

                String sql = "INSERT INTO research_papers_metadata (title, file_name, metadata) VALUES (?, ?, ?)";
                jdbcClient.sql(sql).params(title, fileName, enrichedMetadata).update();

            } catch (IOException e) {
                logger.error("Error while reading file content: {}", e.getMessage());
            }
        } else {
            logger.info("The following file is not indexed as only pdf files are supported: {}\n",
                    folderPath.getFileName().toString());
        }
    }

    private String getFileExtension(Path folderPath) {
        try {
            String mimeType = Files.probeContentType(folderPath);
            logger.debug("Mime type of the file is: {}", mimeType);
            if (mimeType == null) {
                return "";
            }
            return switch (mimeType) {
                case "application/pdf" -> "pdf";
                case "text/plain" -> "txt";
                case "application/msword" -> "doc";
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx";
                case "text/markdown" -> "md";
                // Add more cases as needed
                default -> mimeType;
            };
        } catch (IOException e) {
            logger.error("Error while getting file extension: {}", e.getMessage());
            return "";
        }

    }

}
