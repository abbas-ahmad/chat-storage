package bytecode.rag_chat_storage.repository;

import bytecode.rag_chat_storage.entity.ChatMessage;
import bytecode.rag_chat_storage.entity.ChatSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Find all messages for a specific chat session ordered by creation time
     */
    List<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession);

    /**
     * Find all messages for a specific chat session with pagination
     */
    Page<ChatMessage> findByChatSessionOrderByCreatedAtAsc(ChatSession chatSession, Pageable pageable);

    /**
     * Find messages for a chat session by session ID and user ID
     */
    @Query("SELECT cm FROM ChatMessage cm JOIN cm.chatSession cs WHERE cs.id = :sessionId AND cs.userId = :userId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatSessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") String userId);

    /**
     * Find messages for a chat session with pagination by session ID and user ID
     */
    @Query("SELECT cm FROM ChatMessage cm JOIN cm.chatSession cs WHERE cs.id = :sessionId AND cs.userId = :userId ORDER BY cm.createdAt ASC")
    Page<ChatMessage> findByChatSessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") String userId, Pageable pageable);

    /**
     * Count total messages for a chat session
     */
    long countByChatSession(ChatSession chatSession);

    /**
     * Find the latest message for a chat session
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLatestByChatSession(@Param("chatSession") ChatSession chatSession, Pageable pageable);

    /**
     * Delete all messages for a specific chat session
     */
    void deleteByChatSession(ChatSession chatSession);

    /**
     * Find messages by sender type for a specific session
     */
    List<ChatMessage> findByChatSessionAndSenderTypeOrderByCreatedAtAsc(ChatSession chatSession, ChatMessage.SenderType senderType);
}
