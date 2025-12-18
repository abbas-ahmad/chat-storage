package store.chat_storage.service;


import store.chat_storage.dto.*;
import store.chat_storage.entity.ChatSession;
import store.chat_storage.exception.ResourceNotFoundException;
import store.chat_storage.repository.ChatSessionRepository;
import store.chat_storage.specification.ChatSessionSpecifications;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class ChatSessionService {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionService.class);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageService chatMessageService;

    /**
     * Create a new chat session
     */
    public ChatSessionDto createChatSession(String userId, CreateChatSessionRequest request) {
        logger.info("Creating new chat session for user: {}", userId);

        ChatSession savedSession = chatSessionRepository.save(ChatSession.builder()
                                                                .userId(userId)
                                                                .name(request.getName())
                                                                .isFavorite(false) // Explicitly set default value
                                                                .build());

        logger.info("Created chat session with ID: {} for user: {}", savedSession.getId(), userId);
        return new ChatSessionDto(savedSession);
    }


    /**
     * Get a specific chat session by ID
     */
    public ChatSessionDto getChatSession(String userId, Long sessionId) {
        logger.info("Retrieving chat session: {} for user: {}", sessionId, userId);

        ChatSession session = findSessionByIdAndUserId(sessionId, userId);

        ChatSessionDto sessionDto = new ChatSessionDto(session);

        // Load messages for the session
        List<ChatMessageDto> messages = chatMessageService.getMessagesBySessionId(userId, sessionId);
        sessionDto.setMessages(messages);

        return sessionDto;
    }

    /**
     * Update a chat session name
     */
    public ChatSessionDto updateChatSession(String userId, Long sessionId, UpdateChatSessionRequest request) {
        logger.info("Updating chat session: {} for user: {}", sessionId, userId);

        ChatSession session = findSessionByIdAndUserId(sessionId, userId);

        session.setName(request.getName());
        ChatSession updatedSession = chatSessionRepository.save(session);

        logger.info("Updated chat session: {} for user: {}", sessionId, userId);
        return new ChatSessionDto(updatedSession);
    }

    /**
     * Toggle favorite status of a chat session
     */
    public ChatSessionDto toggleFavorite(String userId, Long sessionId) {
        logger.info("Toggling favorite status for chat session: {} for user: {}", sessionId, userId);

        ChatSession session = findSessionByIdAndUserId(sessionId, userId);

        session.setIsFavorite(!session.getIsFavorite());
        ChatSession updatedSession = chatSessionRepository.save(session);

        logger.info("Toggled favorite status for chat session: {} to {} for user: {}",
                sessionId, updatedSession.getIsFavorite(), userId);
        return new ChatSessionDto(updatedSession);
    }

    /**
     * Delete a chat session and all its messages
     */
    public void deleteChatSession(String userId, Long sessionId) {
        logger.info("Deleting chat session: {} for user: {}", sessionId, userId);

        ChatSession session = findSessionByIdAndUserId(sessionId, userId);

        // Delete all messages first
        chatMessageService.deleteMessagesBySessionId(userId, sessionId);

        // Delete the session
        chatSessionRepository.delete(session);

        logger.info("Deleted chat session: {} for user: {}", sessionId, userId);
    }

    /**
     * Get chat sessions with filtering, pagination, and optional stats
     */

    public Object getSessions(String userId, int limit, int offset, Boolean favorite, String search, boolean includeStats) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Specification<ChatSession> spec = ChatSessionSpecifications.hasUserId(userId);

        if (favorite != null && favorite) {
            spec = spec.and(ChatSessionSpecifications.isFavorite());
        }

        if (search != null && !search.isEmpty()) {
            spec = spec.and(ChatSessionSpecifications.nameContains(search));
        }

        Page<ChatSession> sessionsPage = chatSessionRepository.findAll(spec, pageable);
        List<ChatSessionDto> sessionDtos = sessionsPage.getContent().stream()
                .map(ChatSessionDto::new)
                .collect(Collectors.toList());

        if (includeStats) {
            long totalSessions = chatSessionRepository.count(ChatSessionSpecifications.hasUserId(userId));
            long favoriteSessions = chatSessionRepository.count(spec.and(ChatSessionSpecifications.isFavorite()));
            SessionStatsDto stats = new SessionStatsDto(totalSessions, favoriteSessions);
            return new ChatSessionListResponse(sessionDtos, stats, sessionsPage.getTotalPages(), sessionsPage.getTotalElements());
        } else {
            return sessionDtos;
        }
    }

    private ChatSession findSessionByIdAndUserId(Long sessionId, String userId) {
        Specification<ChatSession> spec = ChatSessionSpecifications.hasUserId(userId)
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), sessionId));

        return chatSessionRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
    }
}
