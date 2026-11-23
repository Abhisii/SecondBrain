package com.Backend.SecondBrainProject.ingestion.parser;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class TextParserStrategy implements DocumentParser {

    @Override
    public List<Document> parse(MultipartFile file) {
        try {
            // 1. Extract the raw text from the file using UTF-8 encoding
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            // 2. Build metadata map (Critical for Vector DB filtering and citations)
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.txt";
            Map<String, Object> metadata = Map.of(
                    "filename", filename,
                    "file_type", "txt"
            );

            // 3. Create a single Spring AI Document containing the text and metadata
            Document document = new Document(content, metadata);

            // 4. Return as a list to satisfy the DocumentParser interface
            return List.of(document);

        } catch (IOException e) {
            // In a real app, replace with custom FileParsingException
            throw new RuntimeException("Failed to parse Text file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public String getSupportedExtension() {
        return "txt";
    }
}