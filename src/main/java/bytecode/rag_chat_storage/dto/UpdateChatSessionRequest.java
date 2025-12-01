package bytecode.rag_chat_storage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateChatSessionRequest {
    @NotBlank(message = "Session name is required")
    private String name;
}
