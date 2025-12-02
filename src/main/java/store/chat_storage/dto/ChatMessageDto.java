package store.chat_storage.dto;

import store.chat_storage.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    // Custom constructor for entity mapping
    public ChatMessageDto(ChatMessage chatMessage) {
        this.id = chatMessage.getId();
        this.chatSessionId = chatMessage.getChatSession().getId();
        this.senderType = chatMessage.getSenderType();
        this.content = chatMessage.getContent();
        this.context = chatMessage.getContext();
        this.createdAt = chatMessage.getCreatedAt();
    }
}
