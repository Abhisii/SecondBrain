package com.Backend.SecondBrainProject.ingestion.parser;

import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DocumentParser {
    /**
     * Extracts text from the uploaded file and converts it into Spring AI Documents.
     */
    List<Document> parse(MultipartFile file);

    /**
     * Returns the file extension this parser is responsible for (e.g., "pdf", "txt").
     */
    String getSupportedExtension();
}