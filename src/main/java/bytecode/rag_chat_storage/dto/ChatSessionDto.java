package bytecode.rag_chat_storage.dto;

import bytecode.rag_chat_storage.entity.ChatSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class ChatSessionDto {

    private Long id;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Session name is required")
    private String name;

    @NotNull(message = "Is favorite flag is required")
    private Boolean isFavorite;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatMessageDto> messages;

    // Constructors
    public ChatSessionDto() {}

    public ChatSessionDto(ChatSession chatSession) {
        this.id = chatSession.getId();
        this.userId = chatSession.getUserId();
        this.name = chatSession.getName();
        this.isFavorite = chatSession.getIsFavorite();
        this.createdAt = chatSession.getCreatedAt();
        this.updatedAt = chatSession.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
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

    public List<ChatMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDto> messages) {
        this.messages = messages;
    }
}
