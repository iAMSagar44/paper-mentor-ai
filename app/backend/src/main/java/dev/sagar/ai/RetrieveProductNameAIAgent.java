package dev.sagar.ai;

import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
class RetrieveProductNameAIAgent {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RetrieveProductNameAIAgent.class);

    private final ChatClient chatClient;

    public RetrieveProductNameAIAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultOptions(OpenAiChatOptions.builder()
                        .withModel("gpt-4o")
                        .withTemperature(0.1f)
                        .withMaxTokens(100)
                        .build())
                .build();
    }

    AzureProductName retrieveProductName(String query) {
        return chatClient.prompt()
                .system("""
                        You are tasked with inferring the appropriate Azure product name from the provided product names list based on the user query.
                        Use string comparison techniques to find the closest match. Follow these guidelines:
                        1. Do not answer the user's query directly.
                        2. Only return the inferred product name from the product list.
                        3. Use the provided product names list exclusively for inference—do not infer or suggest any products that are not in the list.
                        """)
                .user(query)
                .functions("azureProductNames")
                .call()
                .entity(AzureProductName.class);
    }
}
