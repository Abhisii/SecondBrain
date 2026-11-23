package com.Backend.SecondBrainProject.api;

import com.Backend.SecondBrainProject.api.dto.AskQueryRequest;
import com.Backend.SecondBrainProject.api.dto.AskQueryResponse;
import com.Backend.SecondBrainProject.retrieval.AskYourQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brain")
public class QueryController {

    private final AskYourQueryService queryService;

    public QueryController(AskYourQueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AskQueryResponse> askBrain(@RequestBody AskQueryRequest request) {
        if (request.query() == null || request.query().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String conversationId = request.conversationId() != null ? request.conversationId() : "default-session";
        AskQueryResponse response = queryService.ask(request.query(), conversationId);

        return ResponseEntity.ok(response);
    }
}