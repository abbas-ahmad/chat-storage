package bytecode.rag_chat_storage.dto;

import bytecode.rag_chat_storage.entity.ChatSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    // Custom constructor for entity mapping
    public ChatSessionDto(ChatSession chatSession) {
        this.id = chatSession.getId();
        this.userId = chatSession.getUserId();
        this.name = chatSession.getName();
        this.isFavorite = chatSession.getIsFavorite();
        this.createdAt = chatSession.getCreatedAt();
        this.updatedAt = chatSession.getUpdatedAt();
    }
}
