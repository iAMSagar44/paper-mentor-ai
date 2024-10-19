package dev.sagar.ai;

import java.util.List;
import java.util.UUID;

record AssistantResponse(String token, String title, UUID conversation_id, List<String> fileNames) {
}