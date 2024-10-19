package dev.sagar.conversations;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "messages", uniqueConstraints = @UniqueConstraint(columnNames = "conversation_id"))
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @OneToOne
    @JoinColumn(name = "conversation_id", nullable = false, unique = true)
    private Conversation conversation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message_text", columnDefinition = "jsonb", nullable = false)
    private List<ChatMessage> messageText;

    @Column(name = "created_at", columnDefinition = "timestamp without time zone")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamp without time zone")
    private LocalDateTime updatedAt;

    // Getters and Setters

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public List<ChatMessage> getMessageText() {
        return messageText;
    }

    public void setMessageText(List<ChatMessage> messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
