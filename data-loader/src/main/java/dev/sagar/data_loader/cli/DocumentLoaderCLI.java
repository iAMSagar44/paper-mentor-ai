package dev.sagar.data_loader.cli;

import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Command(command = "data", description = "Load data from a file")
public class DocumentLoaderCLI {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentLoaderCLI.class);
    private final JdbcClient jdbcClient;
    private final IndexDocuments indexDocuments;

    public DocumentLoaderCLI(JdbcClient jdbcClient, IndexDocuments indexDocuments) {
        this.jdbcClient = jdbcClient;
        this.indexDocuments = indexDocuments;
    }

    @Command(command = "load", description = "Index documents to the Vector Store")
    public void loadData(
            @Option(longNames = "path", shortNames = 'p', required = true, label = "the path of the directory or file") String path)
            throws IOException {
        Path folderPath = Path.of(path);
        logger.info("Loading documents from the path {}", folderPath.toAbsolutePath());
        validateFolderPath(folderPath);
    }

    @Command(command = "delete", description = "Deletes documents from the Vector Store")
    public void deleteAllData(
            @Option(longNames = "file", shortNames = 'f', label = "the file name to delete from the vector store") String fileName)
            throws IOException {
        if (fileName != null) {
            logger.info("Deleting the document with file name {}", fileName);
            var value = """
                    '{"file_name":"%s"}'
                    """.formatted(fileName);
            String sqlStatement = "DELETE FROM vector_store WHERE metadata::jsonb @>" + value;
            final var count = jdbcClient.sql(sqlStatement)
                    .update();
            logger.debug("Number of indexed documents deleted: {}", count);
            if (count == 0) {
                logger.info("No document found with the file name {}", fileName);
            } else {
                logger.info("Deleted indexed documents with file name {}", fileName);
            }
            return;
        }
        logger.info("Deleting all documents from the Vector Store");
        jdbcClient.sql("DELETE FROM vector_store").update();
    }

    @Command(command = "list", description = "List files in the Vector store")
    public List<String> listFileNames() throws IOException {
        return jdbcClient.sql("SELECT DISTINCT metadata->>'title' FROM research_papers_local")
                .query(String.class)
                .list();
    }

    private void validateFolderPath(Path folderPath) throws IOException {
        if (Files.isDirectory(folderPath)) {
            logger.info("The path provided is a directory");
            processDirectory(folderPath);
        } else {
            if (Files.exists(folderPath)) {
                loadDocuments(folderPath);
            } else {
                throw new FileNotFoundException(
                        String.format("The file path provided does not exist. File Path %s", folderPath.toString()));
            }
        }
    }

    private void processDirectory(Path folderPath) throws IOException {
        try (Stream<Path> pathStream = Files.walk(folderPath)) {
            final var files = pathStream.filter(Files::isRegularFile).toList();
            logger.info("Found {} files in the directory. Indexing each file separately. \n", files.size());
            files.forEach(file -> loadDocuments(file));
        }
    }

    private void loadDocuments(Path filePath) {
        indexDocuments.load(filePath);
    }
}
