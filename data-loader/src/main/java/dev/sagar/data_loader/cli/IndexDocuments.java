package dev.sagar.data_loader.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class IndexDocuments {

    private final VectorStore vectorStore;
    private final AnalyseDocumentService analyseDocumentService;
    private final DocumentEnrichmentAIAgent documentEnrichmentAIAgent;

    private static final Logger logger = LoggerFactory.getLogger(IndexDocuments.class);

    public IndexDocuments(VectorStore vectorStore, AnalyseDocumentService analyseDocumentService,
            DocumentEnrichmentAIAgent documentEnrichmentAIAgent) {
        this.vectorStore = vectorStore;
        this.analyseDocumentService = analyseDocumentService;
        this.documentEnrichmentAIAgent = documentEnrichmentAIAgent;
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

                var tokenTextSplitter = new TokenTextSplitter(1200, 350, 5, 10000, true);
                final var splitDocuments = tokenTextSplitter.apply(analysedDocuments);

                // Add the documents to the vector store
                logger.info(
                        "Creating embeddings and storing in vector store....");
                vectorStore.add(splitDocuments);
                logger.info("Vector Store indexing completed");

                // Store the document metadata and summary in the database
                documentEnrichmentAIAgent.storeResearchPapersInfoAndSummary(
                        analysedDocuments.subList(0, Math.min(2, analysedDocuments.size())));

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
