package bytecode.rag_chat_storage.dto;

import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionListResponse {
    private List<ChatSessionDto> sessions;
    private SessionStatsDto stats;
    private int totalPages;
    private long totalElements;
}
