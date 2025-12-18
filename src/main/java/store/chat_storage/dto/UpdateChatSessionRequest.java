package store.chat_storage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdateChatSessionRequest {
    @NotBlank(message = "Session name is required")
    private String name;
}
