package bytecode.rag_chat_storage.service;


import bytecode.rag_chat_storage.dto.AddMessageRequest;
import bytecode.rag_chat_storage.dto.ChatMessageDto;
import bytecode.rag_chat_storage.entity.ChatMessage;
import bytecode.rag_chat_storage.entity.ChatSession;
import bytecode.rag_chat_storage.exception.ResourceNotFoundException;
import bytecode.rag_chat_storage.repository.ChatMessageRepository;
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
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    /**
     * Add a new message to a chat session
     */
    public ChatMessageDto addMessage(String userId, Long sessionId, AddMessageRequest request) {
        logger.info("Adding message to session: {} for user: {}", sessionId, userId);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        // Create new message
        ChatMessage message = new ChatMessage();
        message.setChatSession(session);
        message.setSenderType(request.getSenderType());
        message.setContent(request.getContent());
        message.setContext(request.getContext());
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        logger.info("Added message with ID: {} to session: {} for user: {}", 
                   savedMessage.getId(), sessionId, userId);
        
        return new ChatMessageDto(savedMessage);
    }

    /**
     * Get all messages for a chat session
     */
    public List<ChatMessageDto> getMessagesBySessionId(String userId, Long sessionId) {
        logger.info("Retrieving messages for session: {} for user: {}", sessionId, userId);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);
        
        return messages.stream()
                .map(ChatMessageDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Get messages for a chat session with pagination
     */
    public Page<ChatMessageDto> getMessagesBySessionId(String userId, Long sessionId, int page, int size) {
        logger.info("Retrieving messages for session: {} for user: {} with pagination - page: {}, size: {}", 
                   sessionId, userId, page, size);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session, pageable);
        
        return messages.map(ChatMessageDto::new);
    }

    /**
     * Get a specific message by ID
     */
    public ChatMessageDto getMessage(String userId, Long sessionId, Long messageId) {
        logger.info("Retrieving message: {} from session: {} for user: {}", messageId, sessionId, userId);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        // Verify message belongs to the session
        if (!message.getChatSession().getId().equals(sessionId)) {
            throw new ResourceNotFoundException("Message not found in session: " + sessionId);
        }
        
        return new ChatMessageDto(message);
    }

    /**
     * Delete a specific message
     */
    public void deleteMessage(String userId, Long sessionId, Long messageId) {
        logger.info("Deleting message: {} from session: {} for user: {}", messageId, sessionId, userId);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        // Verify message belongs to the session
        if (!message.getChatSession().getId().equals(sessionId)) {
            throw new ResourceNotFoundException("Message not found in session: " + sessionId);
        }
        
        chatMessageRepository.delete(message);
        
        logger.info("Deleted message: {} from session: {} for user: {}", messageId, sessionId, userId);
    }

    /**
     * Delete all messages for a chat session
     */
    public void deleteMessagesBySessionId(String userId, Long sessionId) {
        logger.info("Deleting all messages for session: {} for user: {}", sessionId, userId);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        chatMessageRepository.deleteByChatSession(session);
        
        logger.info("Deleted all messages for session: {} for user: {}", sessionId, userId);
    }

    /**
     * Get message count for a chat session
     */
    public long getMessageCount(String userId, Long sessionId) {
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        return chatMessageRepository.countByChatSession(session);
    }

    /**
     * Get latest messages for a chat session
     */
    public List<ChatMessageDto> getLatestMessages(String userId, Long sessionId, int limit) {
        logger.info("Retrieving latest {} messages for session: {} for user: {}", limit, sessionId, userId);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        Pageable pageable = PageRequest.of(0, limit);
        List<ChatMessage> messages = chatMessageRepository.findLatestByChatSession(session, pageable);
        
        return messages.stream()
                .map(ChatMessageDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Get messages by sender type for a session
     */
    public List<ChatMessageDto> getMessagesBySenderType(String userId, Long sessionId, ChatMessage.SenderType senderType) {
        logger.info("Retrieving {} messages for session: {} for user: {}", senderType, sessionId, userId);
        
        // Verify session exists and belongs to user
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));
        
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionAndSenderTypeOrderByCreatedAtAsc(session, senderType);
        
        return messages.stream()
                .map(ChatMessageDto::new)
                .collect(Collectors.toList());
    }
}
