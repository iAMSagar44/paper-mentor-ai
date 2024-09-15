package dev.sagar.ai;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import dev.sagar.ai.AnalyzeUserQueryAIAgent.AnalyzedSearchQuery;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import java.util.concurrent.atomic.AtomicReference;

@Service
class AIChatService {
        private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AIChatService.class);
        private final StreamingChatModel chatModel;
        private final VectorStore vectorStore;
        private final ChatMemory messageHistory;
        private final AnalyzeUserQueryAIAgent analyzeUserQueryAIAgent;

        @Value("classpath:/prompts/system-prompt-template.st")
        private Resource systemPrompt;
        @Value("classpath:/prompts/user-prompt-template.st")
        private Resource userPrompt;

        private SystemPromptTemplate systemPromptTemplate;

        @PostConstruct
        private void initializeSystemMessage() {
                this.systemPromptTemplate = new SystemPromptTemplate(this.systemPrompt);
        }

        public AIChatService(StreamingChatModel chatModel, VectorStore vectorStore, ChatMemory messageHistory,
                        AnalyzeUserQueryAIAgent analyzeUserQueryAIAgent) {
                this.chatModel = chatModel;
                this.vectorStore = vectorStore;
                this.messageHistory = messageHistory;
                this.analyzeUserQueryAIAgent = analyzeUserQueryAIAgent;
        }

        Flux<AssistantResponse> generateResponse(String userQuestion) throws IOException {
                logger.info("Generating response for message: {}", userQuestion);

                SystemMessage systemMessage = (SystemMessage) systemPromptTemplate
                                .createMessage();
                logger.debug("System Message: {}", systemMessage.getContent());

                List<Document> documents = retrieveDocumentContent(userQuestion);
                logger.info("Retrieved {} similar documents", documents.size());

                UserMessage userMessage = (UserMessage) retrieveUserMessage(documents, userQuestion);
                logger.debug("User Message: {}", userMessage.getContent());

                Set<String> fileNames = retrieveFileNames(documents);
                logger.debug("File names: {}", fileNames);

                Prompt prompt = new Prompt(List.of(systemMessage, userMessage),
                                OpenAiChatOptions.builder().withTemperature(0.7f)
                                                .withModel("gpt-4o-2024-08-06")
                                                .withFunction("findPapers")
                                                .withFunction("summarizePaper")
                                                .withParallelToolCalls(false)
                                                .build());
                Flux<ChatResponse> chatResponseStream = chatModel.stream(prompt);

                updateChatMemory(userQuestion, chatResponseStream);

                // Extract tool calls used by the LLM
                chatResponseStream.map(response -> response.getResult().getOutput().getToolCalls())
                                .doOnNext(toolCalls -> {
                                        logger.info("Tool calls: {}", toolCalls);
                                        boolean hasFindPapers = toolCalls.stream()
                                                        .anyMatch(toolCall -> "findPapers".equals(toolCall.name()));
                                        if (hasFindPapers) {
                                                logger.info("The chat response includes a 'findPapers' tool call.");
                                        }
                                })
                                .onErrorContinue((e, o) -> logger.error("Error occurred while processing chat response",
                                                e))
                                .subscribe();

                Flux<String> chatResponse = chatResponseStream
                                .map(response -> response.getResult().getOutput().getContent())
                                .onErrorComplete();
                Flux<AssistantResponse> assistantResponse = chatResponse
                                .map(response -> new AssistantResponse(response, List.of()));

                return assistantResponse
                                .concatWith(Flux.just(new AssistantResponse("DONE", List.copyOf(fileNames))))
                                .onBackpressureBuffer()
                                .delayElements(Duration.ofMillis(20))
                                .onErrorComplete();
        }

        private void updateChatMemory(String userQuestion, Flux<ChatResponse> chatResponseStream) {
                logger.info("Updating chat memory with user and assistant messages");

                AtomicReference<StringBuilder> stringBufferRef = new AtomicReference<>(new StringBuilder());
                chatResponseStream.doOnSubscribe(s -> {
                        stringBufferRef.set(new StringBuilder());
                }).doOnNext(chatResponse -> {
                        if (chatResponse.getResult() != null) {
                                if (chatResponse.getResult().getOutput().getContent() != null) {
                                        stringBufferRef.get().append(chatResponse.getResult().getOutput().getContent());
                                }
                        }
                }).doOnComplete(() -> {
                        logger.info("Adding assistant message to chat memory");
                        messageHistory.add("default", new AssistantMessage(stringBufferRef.get().toString()));
                        stringBufferRef.set(new StringBuilder());
                }).doOnError(e -> {
                        logger.error("Error occurred while updating chat memory", e);
                }).subscribe();
        }

        private List<Document> retrieveDocumentContent(String userQuestion) {
                logger.info("Retrieving documents from the Vector Store");

                // Analyze the user query to extract keywords and titles of documents
                AnalyzedSearchQuery analyzeSearchQuery = analyzeUserQueryAIAgent.analyzeSearchQuery(userQuestion);
                logger.info("Analyzed search query: Keywords::{}, \nTitle of documents::{}",
                                analyzeSearchQuery.keywords(), analyzeSearchQuery.titles());

                if (analyzeSearchQuery.titles().isEmpty()) {
                        return List.of();
                }
                analyzeSearchQuery.titles().replaceAll(title -> "'" + title + "'");
                String filterQueryString = String.format("title in %s", analyzeSearchQuery.titles());
                logger.info("Filter query string: {}", filterQueryString);

                // Search for similar documents filtered by the titles of the documents in the
                // vector store
                return vectorStore
                                .similaritySearch(SearchRequest.query(userQuestion)
                                                .withSimilarityThreshold(0.7)
                                                .withTopK(4)
                                                .withFilterExpression(filterQueryString));
        }

        private Message retrieveUserMessage(List<Document> documents, String userQuestion) throws IOException {
                String documentContext = documents.stream()
                                .map(document -> document.getContent())
                                .collect(Collectors.joining(System.lineSeparator()));

                var userPromptText = userQuestion + System.lineSeparator()
                                + userPrompt.getContentAsString(Charset.defaultCharset());

                return new PromptTemplate(userPromptText)
                                .createMessage(Map.of("question_answer_context", documentContext));
        }

        private Set<String> retrieveFileNames(List<Document> documents) {
                return documents.stream()
                                .map(document -> {
                                        String title = (String) document.getMetadata().get("title");
                                        String fileName = (String) document.getMetadata().get("file_name");
                                        return title + "##" + fileName;
                                })
                                .collect(Collectors.toSet());
        }

}
