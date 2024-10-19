package dev.sagar.conversations;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class ConversationController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConversationController.class);
    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/conversations")
    public List<Conversation> getConversations() {
        logger.debug("Getting Conversations");
        return conversationService.getConversations();
    }

    @GetMapping("/conversations/{id}/messages")
    public List<ChatMessage> getMethodName(@PathVariable String id) {
        logger.debug("Getting Conversation with id: {}", id);
        return conversationService.getConversationMessages(id);
    }

    @PutMapping("/conversations/{id}/messages")
    public void updateConversation(@PathVariable String id, @RequestBody @Valid List<ChatMessage> messages) {
        logger.debug("Updating Conversation with id: {}", id);
        conversationService.updateMessage(id, messages);
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<?> deleteConversation(@PathVariable String id) {
        logger.debug("Deleting Conversation with id: {}", id);
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
