package bytecode.rag_chat_storage.dto;

import bytecode.rag_chat_storage.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddMessageRequest {

    @NotNull(message = "Sender type is required")
    private ChatMessage.SenderType senderType;

    @NotBlank(message = "Content is required")
    private String content;

    private String context;

    // Constructors
    public AddMessageRequest() {}

    public AddMessageRequest(ChatMessage.SenderType senderType, String content) {
        this.senderType = senderType;
        this.content = content;
    }

    public AddMessageRequest(ChatMessage.SenderType senderType, String content, String context) {
        this.senderType = senderType;
        this.content = content;
        this.context = context;
    }

    // Getters and Setters
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
}
