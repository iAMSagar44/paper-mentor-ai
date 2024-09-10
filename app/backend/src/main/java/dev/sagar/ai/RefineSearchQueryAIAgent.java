package dev.sagar.ai;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
class RefineSearchQueryAIAgent {
        Logger logger = org.slf4j.LoggerFactory.getLogger(RefineSearchQueryAIAgent.class);

        private final ChatClient chatClient;
        private final ChatMemory messageHistory;

        @Value("classpath:/prompts/query-refinement-prompt.st")
        private Resource systemPrompt;

        public RefineSearchQueryAIAgent(ChatClient.Builder chatClientBuilder, ChatMemory messageHistory) {
                this.chatClient = chatClientBuilder.defaultOptions(OpenAiChatOptions.builder()
                                .withTemperature(0.3f)
                                .withMaxTokens(200)
                                .build())
                                .build();
                this.messageHistory = messageHistory;
        }

        String refineSearchQuery(String query) {
                var refinedSearchQuery = chatClient.prompt()
                                .system(systemSpec -> systemSpec
                                                .text(systemPrompt)
                                                .param("chat_history", retrieveChatHistory()))
                                .user(query)
                                .call()
                                .content();
                var updatedQuestion = !refinedSearchQuery.equals("ZERO") ? refinedSearchQuery : query;
                updateChatMemory(updatedQuestion);
                return updatedQuestion;

        }

        private void updateChatMemory(String query) {
                messageHistory.add("default", new UserMessage(query));
        }

        private String retrieveChatHistory() {
                List<Message> memoryMessages = messageHistory.get("default", 5);
                logger.debug("Retrieved chat history: {}", memoryMessages);
                return (memoryMessages != null) ? memoryMessages.stream()
                                .filter(m -> m.getMessageType() != MessageType.SYSTEM)
                                .map(m -> m.getMessageType() + ":" + m.getContent())
                                .collect(Collectors.joining(System.lineSeparator())) : "";
        }

}
