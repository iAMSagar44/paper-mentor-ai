package dev.sagar.ai;

import java.io.IOException;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class AssistantController {
    Logger logger = org.slf4j.LoggerFactory.getLogger(AssistantController.class);
    private final RefineSearchQueryAIAgent refineSearchQueryAIAgent;
    private final AIChatService aiChatService;

    public AssistantController(RefineSearchQueryAIAgent refineSearchQueryAIAgent, AIChatService aiChatService) {
        this.refineSearchQueryAIAgent = refineSearchQueryAIAgent;
        this.aiChatService = aiChatService;
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<AssistantResponse> streamingChat(@RequestBody UserQuestion userQuestion) throws IOException {
        String refineSearchQuery = refineSearchQueryAIAgent.refineSearchQuery(userQuestion.question());
        logger.info("Refined search query returned from the AI refiner is: {}", refineSearchQuery);
        return aiChatService.generateResponse(refineSearchQuery);
    }
}