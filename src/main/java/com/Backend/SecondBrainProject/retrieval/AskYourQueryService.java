package com.Backend.SecondBrainProject.retrieval;

import com.Backend.SecondBrainProject.api.dto.AskQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AskYourQueryService {

    private static final Logger log = LoggerFactory.getLogger(AskYourQueryService.class);

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    // We inject ChatClient.Builder because Spring AI prefers building clients per-service
    public AskYourQueryService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;

        // 1. Initialize memory storage
        ChatMemory chatMemory = new InMemoryChatMemory();

        // 2. Attach the Memory Advisor to the ChatClient
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * @Cacheable automatically checks Redis. If the query exists in Redis, it returns the cached answer instantly.
     * If not, it executes the method, calls the LLM, and saves the result to Redis automatically.
     */
    @Cacheable(value = "brain_answers", key = "#conversationId + '-' + #query")
    public AskQueryResponse ask(String query, String conversationId) {
        log.info("Cache miss for session: '{}', query: '{}'. Searching...", conversationId, query);

        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(query).withTopK(3)
        );

        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<String> sources = similarDocuments.stream()
                .map(doc -> (String) doc.getMetadata().getOrDefault("filename", "Unknown"))
                .distinct()
                .collect(Collectors.toList());

        String systemPrompt = """
            You are a highly intelligent 'Second Brain' assistant.
            Use the following retrieved context to answer the user's question accurately.
            If the context does not contain the answer, politely state that you do not know.
            
            CONTEXT:
            {context}
            """;

        // 3. Pass the conversationId to the Advisor during the call
        String answer = chatClient.prompt()
                .system(sp -> sp.text(systemPrompt).param("context", context))
                .user(query)
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .content();

        return new AskQueryResponse(answer, sources);
    }
}