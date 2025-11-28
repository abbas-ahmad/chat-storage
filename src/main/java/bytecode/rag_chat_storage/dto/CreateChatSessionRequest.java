package bytecode.rag_chat_storage.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateChatSessionRequest {

    @NotBlank(message = "Session name is required")
    private String name;

    // Constructors
    public CreateChatSessionRequest() {}

    public CreateChatSessionRequest(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
