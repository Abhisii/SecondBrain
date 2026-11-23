package com.Backend.SecondBrainProject.ingestion.parser;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ParserFactory {

    private final Map<String, DocumentParser> parserMap;

    /**
     * Constructor Injection: Spring automatically provides all registered DocumentParsers.
     * We convert the List into a Map for O(1) lookups.
     */
    public ParserFactory(List<DocumentParser> parsers) {
        this.parserMap = parsers.stream()
                .collect(Collectors.toMap(
                        DocumentParser::getSupportedExtension,
                        Function.identity()
                ));
    }

    /**
     * Retrieves the correct parser based on the file extension.
     */
    public DocumentParser getParser(String extension) {
        DocumentParser parser = parserMap.get(extension.toLowerCase());

        if (parser == null) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }

        return parser;
    }
}