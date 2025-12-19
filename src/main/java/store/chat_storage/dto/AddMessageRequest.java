package store.chat_storage.dto;

import store.chat_storage.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMessageRequest {
    @NotNull(message = "Sender type is required")
    private ChatMessage.SenderType senderType;

    @NotBlank(message = "Content is required")
    private String content;

    private String context;
}

