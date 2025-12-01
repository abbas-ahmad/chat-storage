package bytecode.rag_chat_storage.service;


import bytecode.rag_chat_storage.dto.*;
import bytecode.rag_chat_storage.entity.ChatSession;
import bytecode.rag_chat_storage.exception.ResourceNotFoundException;
import bytecode.rag_chat_storage.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        
        ChatSession chatSession = new ChatSession(userId, request.getName());
        ChatSession savedSession = chatSessionRepository.save(chatSession);
        
        logger.info("Created chat session with ID: {} for user: {}", savedSession.getId(), userId);
        return new ChatSessionDto(savedSession);
    }


    /**
     * Get a specific chat session by ID
     */
    public ChatSessionDto getChatSession(String userId, Long sessionId) {
        logger.info("Retrieving chat session: {} for user: {}", sessionId, userId);
        
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
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
        
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
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
        
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
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
        
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
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
        Page<ChatSession> sessionsPage;
        // Filtering logic
        if (favorite != null && favorite) {
            if (search != null && !search.isEmpty()) {
                sessionsPage = chatSessionRepository.findByUserIdAndIsFavoriteTrueAndNameContainingIgnoreCase(userId, search, pageable);
            } else {
                sessionsPage = chatSessionRepository.findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(userId, pageable);
            }
        } else if (search != null && !search.isEmpty()) {
            sessionsPage = chatSessionRepository.findByUserIdAndNameContainingIgnoreCase(userId, search, pageable);
        } else {
            sessionsPage = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        }
        List<ChatSessionDto> sessionDtos = sessionsPage.getContent().stream()
                .map(ChatSessionDto::new)
                .collect(Collectors.toList());
        if (includeStats) {
            long totalSessions = chatSessionRepository.countByUserId(userId);
            long favoriteSessions = chatSessionRepository.countByUserIdAndIsFavoriteTrue(userId);
            SessionStatsDto stats = new SessionStatsDto(totalSessions, favoriteSessions);
            return new ChatSessionListResponse(sessionDtos, stats, sessionsPage.getTotalPages(), sessionsPage.getTotalElements());
        } else {
            return sessionDtos;
        }
    }
}
