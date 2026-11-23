package com.Backend.SecondBrainProject.ingestion.parser;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class PdfParserStrategy implements DocumentParser {

    @Override
    public List<Document> parse(MultipartFile file) {
        try {
            // 1. Convert MultipartFile to a Spring Resource
            Resource resource = new ByteArrayResource(file.getBytes());

            // 2. Configure the PDF Reader (1 page per Document is standard for RAG)
            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource, config);

            // 3. Extract and return the text as a list of Spring AI Documents
            return pdfReader.get();

        } catch (IOException e) {
            // In a real app, throw your custom FileParsingException here
            throw new RuntimeException("Failed to parse PDF file: " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public String getSupportedExtension() {
        return "pdf";
    }
}