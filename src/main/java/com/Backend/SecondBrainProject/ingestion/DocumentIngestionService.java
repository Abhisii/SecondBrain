package com.Backend.SecondBrainProject.ingestion;

import com.Backend.SecondBrainProject.ingestion.parser.DocumentParser;
import com.Backend.SecondBrainProject.ingestion.parser.ParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final ParserFactory parserFactory;
    private final VectorStore vectorStore;

    // SDE-1 Flex: Constructor injection (better for testing than @Autowired)
    public DocumentIngestionService(ParserFactory parserFactory, VectorStore vectorStore) {
        this.parserFactory = parserFactory;
        this.vectorStore = vectorStore;
    }

    /**
     * Orchestrates the ingestion of a document into the Vector Database.
     */
    public void ingestDocument(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String extension = getFileExtension(filename);
        log.info("Starting ingestion for file: {} with extension: {}", filename, extension);

        // 1. Get the correct parser using the Strategy Factory
        DocumentParser parser = parserFactory.getParser(extension);

        // 2. Parse the file into raw Spring AI Documents
        List<Document> rawDocuments = parser.parse(file);
        log.info("Extracted {} raw documents/pages from {}", rawDocuments.size(), filename);

        // --> ADD THIS LINE TO PREVENT NULL CHUNKS <--
        rawDocuments.removeIf(doc -> doc.getContent() == null || doc.getContent().trim().isEmpty());


        // --> NEW: Sanitize metadata to prevent TextSplitter crashes
        List<Document> sanitizedDocuments = rawDocuments.stream()
                .map(doc -> {
                    Map<String, Object> cleanMetadata = new java.util.HashMap<>();
                    if (doc.getMetadata() != null) {
                        doc.getMetadata().forEach((key, value) -> {
                            if (key != null && value != null) {
                                cleanMetadata.put(key, value);
                            }
                        });
                    }
                    // Rebuild the document with clean metadata
                    return new Document(doc.getContent(), cleanMetadata);
                })
                .toList();

        // 3. Chunk the documents
        // We split text into 500-token chunks with a 100-token overlap to preserve context across boundaries
        TokenTextSplitter textSplitter = new TokenTextSplitter(500, 100, 5, 10000, true);
        List<Document> chunkedDocuments = textSplitter.apply(sanitizedDocuments);
        log.info("Split into {} chunks for vectorization", chunkedDocuments.size());

        // 4. Save to PostgreSQL (pgvector)
        // Spring AI automatically calls your local embedding model to vectorize the text,
        // then saves both the vectors and the text chunks into Postgres.
        vectorStore.add(chunkedDocuments);
        log.info("Successfully ingested {} into the vector store.", filename);
    }

    /**
     * Helper method to safely extract the file extension.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            throw new IllegalArgumentException("File must have an extension");
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}