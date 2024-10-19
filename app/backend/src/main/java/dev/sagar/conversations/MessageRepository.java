package dev.sagar.conversations;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Message findByConversation(Conversation conversation);
}