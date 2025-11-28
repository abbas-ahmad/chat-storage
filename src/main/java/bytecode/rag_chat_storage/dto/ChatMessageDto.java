package bytecode.rag_chat_storage.dto;

import bytecode.rag_chat_storage.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ChatMessageDto {

    private Long id;

    @NotNull(message = "Chat session ID is required")
    private Long chatSessionId;

    @NotNull(message = "Sender type is required")
    private ChatMessage.SenderType senderType;

    @NotBlank(message = "Content is required")
    private String content;

    private String context;
    private LocalDateTime createdAt;

    // Constructors
    public ChatMessageDto() {}

    public ChatMessageDto(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.chatSessionId = chatMessage.getChatSession().getId();
        this.senderType = chatMessage.getSenderType();
        this.content = chatMessage.getContent();
        this.context = chatMessage.getContext();
        this.createdAt = chatMessage.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(Long chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public ChatMessage.SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(ChatMessage.SenderType senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
