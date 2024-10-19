package dev.sagar.ai;

import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

@Component
public class TitleGeneratorAIAgent {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TitleGeneratorAIAgent.class);
    private final ChatClient chatClient;

    public TitleGeneratorAIAgent(ChatClient.Builder chatBuilder) {
        this.chatClient = chatBuilder.defaultOptions(OpenAiChatOptions.builder()
                .withModel("gpt-4o-mini")
                .withTemperature(0.4)
                .withMaxTokens(50)
                .build())
                .defaultSystem("Generate a title for the provided user text. The title should be concise and relevant.")
                .build();
    }

    public String generateTitle(String userText) {
        logger.info("User text: {}", userText);
        return this.chatClient.prompt()
                .user(userText)
                .call()
                .content();
    }
}
