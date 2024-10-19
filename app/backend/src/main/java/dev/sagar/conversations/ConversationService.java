package dev.sagar.conversations;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConversationService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ConversationService.class);
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository, UserRepository userRepository,
            MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    List<Conversation> getConversations() {
        User userIdentity = retrieveUserIdentity();
        logger.info("Getting Conversations for user: {}", userIdentity.getEmail());
        return conversationRepository.findByUser(userIdentity);
    }

    public Conversation saveConversation(String title) {
        User userIdentity = retrieveUserIdentity();
        Conversation conversation = new Conversation();
        conversation.setTitle(title);
        conversation.setUser(userIdentity);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation savedConversation = conversationRepository.save(conversation);

        // Initialize the message row with an empty list
        Message message = new Message();
        message.setConversation(conversation);
        message.setMessageText(new ArrayList<>()); // Empty conversation initially
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        messageRepository.save(message);
        return savedConversation;
    }

    public Conversation findConversationById(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    // Retrieve all messages for a conversation
    List<ChatMessage> getConversationMessages(String conversationId) {
        Conversation conversation = findConversationById(UUID.fromString(conversationId));
        Message message = messageRepository.findByConversation(conversation);
        return message.getMessageText();
    }

    void updateMessage(String conversationId, List<ChatMessage> chatMessages) {
        Conversation conversation = findConversationById(UUID.fromString(conversationId));
        Message message = messageRepository.findByConversation(conversation);

        message.setMessageText(chatMessages);
        message.setUpdatedAt(LocalDateTime.now());
        var savedMessage = messageRepository.save(message);
        logger.info("Updated message: {}", savedMessage);
    }

    private User retrieveUserIdentity() {
        OAuth2AuthenticatedPrincipal oauth2User = (OAuth2AuthenticatedPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication().getPrincipal();
        logger.debug("Authenticated user is: {}", (String) oauth2User.getAttribute("email"));
        logger.debug("User Attributes: {}", oauth2User.getAttributes());
        User user = userRepository.findByEmail((String) oauth2User.getAttribute("email")).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user;
    }

    void deleteConversation(String conversationId) {
        Conversation conversation = findConversationById(UUID.fromString(conversationId));
        conversationRepository.delete(conversation);
    }

}