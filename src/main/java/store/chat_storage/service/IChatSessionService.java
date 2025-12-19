package store.chat_storage.service;

import org.springframework.data.jpa.domain.Specification;
import store.chat_storage.dto.ChatSessionDto;
import store.chat_storage.dto.CreateChatSessionRequest;
import store.chat_storage.dto.UpdateChatSessionRequest;
import store.chat_storage.entity.ChatSession;

import java.util.Optional;

public interface IChatSessionService {

    ChatSessionDto createChatSession(String userId, CreateChatSessionRequest request);

    ChatSessionDto getChatSession(String userId, Long sessionId);

    ChatSessionDto updateChatSession(String userId, Long sessionId, UpdateChatSessionRequest request);

    ChatSessionDto toggleFavorite(String userId, Long sessionId);

    void deleteChatSession(String userId, Long sessionId);

    Object getSessions(String userId, int limit, int offset, Boolean favorite, String search, boolean includeStats);

    Optional<ChatSession> findOne(Specification<ChatSession> specification);
}
