package dev.sagar.data_loader.cli;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AnalyseDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseDocumentService.class);
    private final DocumentAnalysisClient documentAnalysisClient;
    private String documentTitle;

    public AnalyseDocumentService(DocumentAnalysisClient documentAnalysisClient) {
        this.documentAnalysisClient = documentAnalysisClient;
    }

    // Analyse the document using Azure Form Recognizer
    public List<Document> analyseDocument(byte[] document, String fileName) throws IOException {
        List<Document> readDocuments = new ArrayList<>();
        try {
            // Analyse the document
            SyncPoller<OperationResult, AnalyzeResult> analyzeLayoutResultPoller = documentAnalysisClient
                    .beginAnalyzeDocument("prebuilt-layout",
                            BinaryData.fromBytes(document));

            final var analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

            for (DocumentPage documentPage : analyzeLayoutResult.getPages()) {
                StringBuilder pageText = new StringBuilder();
                int pageNumber = documentPage.getPageNumber();
                logger.info("Analysing page number {} in document {}\n", pageNumber, fileName);
                final String paragraphs = analyzeLayoutResult.getParagraphs().stream()
                        .filter(paragraph -> paragraph.getBoundingRegions().getFirst().getPageNumber() == pageNumber)
                        .map(DocumentParagraph::getContent)
                        .collect(Collectors.joining("\n \n"));

                // Extract title, which could be present in the first three pages of the
                // document
                if (pageNumber >= 1 && pageNumber <= 3) {
                    Optional<DocumentParagraph> titleParagraph = analyzeLayoutResult.getParagraphs().stream()
                            .filter(paragraph -> paragraph.getBoundingRegions().getFirst()
                                    .getPageNumber() == pageNumber)
                            .filter(paragraph -> paragraph.getRole() != null
                                    && paragraph.getRole().equals(ParagraphRole.TITLE))
                            .findFirst();

                    if (titleParagraph.isPresent()) {
                        logger.info("Title: {}", titleParagraph.get().getContent());
                        String title = titleParagraph.get().getContent();
                        setDocumentTitle(title);
                    }
                }
                logger.debug("Page number: {} \t Paragraphs: {}", pageNumber, paragraphs);
                pageText.append(paragraphs);

                // Extract tables
                if (analyzeLayoutResult.getTables() != null) {
                    final List<DocumentTable> tablesOnPage = analyzeLayoutResult.getTables().stream()
                            .filter(table -> table.getBoundingRegions().getFirst().getPageNumber() == pageNumber)
                            .toList();

                    for (DocumentTable table : tablesOnPage) {
                        logger.info("Found {} table(s) on page number {} in document {}\n", tablesOnPage.size(),
                                pageNumber, fileName);
                        logger.info("Extracting tables from page number {} in document {}\n", pageNumber,
                                fileName);
                        var htmlTable = extractTableData(table);
                        pageText.append("\n[Table Data]:\n").append(htmlTable);
                    }
                }

                readDocuments.add(this.toDocument(pageText.toString(), pageNumber, fileName));
            }

            // add a new key named title to the metadata of each document
            readDocuments.forEach(doc -> doc.getMetadata().put("title", getDocumentTitle()));

            return readDocuments;

        } catch (HttpResponseException e) {
            logger.error("Error occurred while analyzing the document", e);
            throw new RuntimeException(e);
        }
    }

    private String extractTableData(DocumentTable table) {
        StringBuilder tableToHtml = new StringBuilder("<table>");
        List<List<DocumentTableCell>> rows = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            List<DocumentTableCell> rowCells = new ArrayList<>();
            for (DocumentTableCell cell : table.getCells()) {
                if (cell.getRowIndex() == i) {
                    rowCells.add(cell);
                }
            }
            rows.add(rowCells);
        }

        for (List<DocumentTableCell> rowCells : rows) {
            tableToHtml.append("<tr>");
            for (DocumentTableCell cell : rowCells) {
                String tag = (cell.getKind().equals("columnHeader") || cell.getKind().equals("rowHeader")) ? "th"
                        : "td";
                String cellSpans = "";
                if (cell.getColumnSpan() != null && cell.getColumnSpan() > 1) {
                    cellSpans += " colSpan=" + cell.getColumnSpan();
                }
                if (cell.getRowSpan() != null && cell.getRowSpan() > 1) {
                    cellSpans += " rowSpan=" + cell.getRowSpan();
                }
                tableToHtml.append("<").append(tag).append(cellSpans).append(">")
                        .append(cell.getContent())
                        .append("</").append(tag).append(">");
            }
            tableToHtml.append("</tr>");
        }
        tableToHtml.append("</table>");
        return tableToHtml.toString();
    }

    private String getDocumentTitle() {
        if (documentTitle == null) {
            return "Untitled Document";
        } else {
            return documentTitle;
        }
    }

    private void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    private Document toDocument(String docText, int startPageNumber, String resourceFileName) {
        Document doc = new Document(docText);
        doc.getMetadata().put("page_number", startPageNumber);
        doc.getMetadata().put("file_name", resourceFileName);
        return doc;
    }
}