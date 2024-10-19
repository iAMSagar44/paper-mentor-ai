package dev.sagar.conversations;

import jakarta.validation.constraints.NotBlank;

public record ChatMessage(@NotBlank String content, @NotBlank String sender) {
    public ChatMessage {
        if (!sender.equals("llm") && !sender.equals("user")) {
            throw new IllegalArgumentException("Bad request: sender must be 'llm' or 'user'");
        }
    }
}
