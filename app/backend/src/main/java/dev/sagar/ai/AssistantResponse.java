package dev.sagar.ai;

import java.util.List;

record AssistantResponse(String token, List<String> fileNames) {
}