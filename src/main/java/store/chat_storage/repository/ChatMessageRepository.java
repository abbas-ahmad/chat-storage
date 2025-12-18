package store.chat_storage.repository;

import store.chat_storage.entity.ChatMessage;
import store.chat_storage.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, JpaSpecificationExecutor<ChatMessage> {

    /**
     * Delete all messages for a specific chat session
     */
    void deleteByChatSession(ChatSession chatSession);
}
