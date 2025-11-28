package bytecode.rag_chat_storage.service;


import bytecode.rag_chat_storage.dto.*;
import bytecode.rag_chat_storage.entity.ChatSession;
import bytecode.rag_chat_storage.exception.ResourceNotFoundException;
import bytecode.rag_chat_storage.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatSessionService {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionService.class);

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageService chatMessageService;

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
     * Get all chat sessions for a user
     */
    public List<ChatSessionDto> getAllChatSessions(String userId) {
        logger.info("Retrieving all chat sessions for user: {}", userId);
        
        List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        return sessions.stream()
                .map(ChatSessionDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Get chat sessions with pagination
     */
    public Page<ChatSessionDto> getChatSessions(String userId, int page, int size) {
        logger.info("Retrieving chat sessions for user: {} with pagination - page: {}, size: {}", userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        
        return sessions.map(ChatSessionDto::new);
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
     * Get favorite chat sessions for a user
     */
    public List<ChatSessionDto> getFavoriteChatSessions(String userId) {
        logger.info("Retrieving favorite chat sessions for user: {}", userId);
        
        List<ChatSession> sessions = chatSessionRepository.findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(userId);
        return sessions.stream()
                .map(ChatSessionDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Search chat sessions by name
     */
    public List<ChatSessionDto> searchChatSessions(String userId, String searchTerm) {
        logger.info("Searching chat sessions for user: {} with term: {}", userId, searchTerm);
        
        List<ChatSession> sessions = chatSessionRepository.findByUserIdAndNameContainingIgnoreCase(userId, searchTerm);
        return sessions.stream()
                .map(ChatSessionDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Get session statistics for a user
     */
    public SessionStatsDto getSessionStats(String userId) {
        logger.info("Retrieving session statistics for user: {}", userId);
        
        long totalSessions = chatSessionRepository.countByUserId(userId);
        long favoriteSessions = chatSessionRepository.countByUserIdAndIsFavoriteTrue(userId);
        
        return new SessionStatsDto(totalSessions, favoriteSessions);
    }

    /**
     * Check if a chat session exists for a user
     */
    public boolean existsChatSession(String userId, Long sessionId) {
        return chatSessionRepository.existsByIdAndUserId(sessionId, userId);
    }
}
