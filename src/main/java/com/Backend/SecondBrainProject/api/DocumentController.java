package com.Backend.SecondBrainProject.api;


import com.Backend.SecondBrainProject.ingestion.DocumentIngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    // Constructor Injection
    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Endpoint to upload and ingest a document into the Second Brain.
     * Expects a form-data request with a key named "file".
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String safeFilename = (originalFilename != null && !originalFilename.isEmpty()) ? originalFilename : "uploaded_file";

            // Hand off the file to our orchestrator service
            ingestionService.ingestDocument(file);

            // Return a clean JSON success response
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Document '" + safeFilename + "' successfully ingested and vectorized."
            ));

        } catch (IllegalArgumentException e) {
            // Handle known bad inputs (like unsupported file types) with a 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            // Print the exact error and line number to your IntelliJ console
            e.printStackTrace();
            // Handle unexpected server errors with a 500 Internal Server Error
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "An unexpected error occurred during ingestion.",
                    "details", errorMessage
            ));
        }
    }
}