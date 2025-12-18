package store.chat_storage.service;


import store.chat_storage.dto.AddMessageRequest;
import store.chat_storage.dto.ChatMessageDto;
import store.chat_storage.entity.ChatMessage;
import store.chat_storage.entity.ChatSession;
import store.chat_storage.exception.ResourceNotFoundException;
import store.chat_storage.repository.ChatMessageRepository;
import store.chat_storage.repository.ChatSessionRepository;
import store.chat_storage.specification.ChatMessageSpecifications;
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
public class ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageService.class);
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;

    /**
     * Add a new message to a chat session
     */
    public ChatMessageDto addMessage(String userId, Long sessionId, AddMessageRequest request) {
        logger.info("Adding message to session: {} for user: {}", sessionId, userId);

        // Verify session exists and belongs to user
        Specification<ChatSession> sessionSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), sessionId),
                criteriaBuilder.equal(root.get("userId"), userId));
        logger.debug("Executing Specification to find ChatSession with id: {} and userId: {}", sessionId, userId);
        ChatSession session = chatSessionRepository.findOne(sessionSpec)
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

        Specification<ChatSession> sessionSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), sessionId),
                criteriaBuilder.equal(root.get("userId"), userId));
        chatSessionRepository.findOne(sessionSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));

        Specification<ChatMessage> spec = ChatMessageSpecifications.hasSessionId(sessionId);
        List<ChatMessage> messages = chatMessageRepository.findAll(spec);

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

        Specification<ChatSession> sessionSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), sessionId),
                criteriaBuilder.equal(root.get("userId"), userId));
        chatSessionRepository.findOne(sessionSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));

        Specification<ChatMessage> spec = ChatMessageSpecifications.hasSessionId(sessionId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messages = chatMessageRepository.findAll(spec, pageable);

        return messages.map(ChatMessageDto::new);
    }

    /**
     * Get a specific message by ID
     */
    public ChatMessageDto getMessage(String userId, Long sessionId, Long messageId) {
        logger.info("Retrieving message: {} from session: {} for user: {}", messageId, sessionId, userId);

        // Verify session exists and belongs to user
        Specification<ChatSession> sessionSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), sessionId),
                criteriaBuilder.equal(root.get("userId"), userId));
        chatSessionRepository.findOne(sessionSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));

        Specification<ChatMessage> messageSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), messageId),
                criteriaBuilder.equal(root.get("chatSession"), sessionId));
        ChatMessage message = chatMessageRepository.findOne(messageSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        return new ChatMessageDto(message);
    }

    /**
     * Delete a specific message
     */
    public void deleteMessage(String userId, Long sessionId, Long messageId) {
        logger.info("Deleting message: {} from session: {} for user: {}", messageId, sessionId, userId);

        // Verify session exists and belongs to user
        Specification<ChatSession> sessionSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), sessionId),
                criteriaBuilder.equal(root.get("userId"), userId));
        chatSessionRepository.findOne(sessionSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));

        Specification<ChatMessage> messageSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), messageId),
                criteriaBuilder.equal(root.get("chatSession"), sessionId));
        ChatMessage message = chatMessageRepository.findOne(messageSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        chatMessageRepository.delete(message);

        logger.info("Deleted message: {} from session: {} for user: {}", messageId, sessionId, userId);
    }

    /**
     * Delete all messages for a chat session
     */
    public void deleteMessagesBySessionId(String userId, Long sessionId) {
        logger.info("Deleting all messages for session: {} for user: {}", sessionId, userId);

        Specification<ChatSession> sessionSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), sessionId),
                criteriaBuilder.equal(root.get("userId"), userId));
        ChatSession session = chatSessionRepository.findOne(sessionSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));

        Specification<ChatMessage> deleteSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("chatSession"), session);
        chatMessageRepository.deleteAll(chatMessageRepository.findAll(deleteSpec));

        logger.info("Deleted all messages for session: {} for user: {}", sessionId, userId);
    }

    /**
     * Consolidated getMessages method supporting pagination, senderType, latest, and countOnly
     */
    public Object getMessages(String userId, Long sessionId, int limit, int offset, String senderType, boolean latest, boolean countOnly) {
        logger.info("Creating Specification for ChatSession with id: {} and userId: {}", sessionId, userId);
        Specification<ChatSession> sessionSpec = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), sessionId),
                criteriaBuilder.equal(root.get("userId"), userId));
        logger.info("Executing Specification to find ChatSession with id: {} and userId: {}", sessionId, userId);
        chatSessionRepository.findOne(sessionSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));

        Specification<ChatMessage> spec = ChatMessageSpecifications.hasSessionId(sessionId);

        if (senderType != null) {
            ChatMessage.SenderType type;
            try {
                type = ChatMessage.SenderType.valueOf(senderType);
            } catch (IllegalArgumentException e) {
                throw new ResourceNotFoundException("Invalid senderType: " + senderType);
            }
            spec = spec.and(ChatMessageSpecifications.hasSenderType(type));
        }

        if (countOnly) {
            return chatMessageRepository.count(spec);
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);
        if (latest) {
            pageable = PageRequest.of(0, limit, org.springframework.data.domain.Sort.by("createdAt").descending());
        }

        Page<ChatMessage> messages = chatMessageRepository.findAll(spec, pageable);
        return messages.map(ChatMessageDto::new);
    }
}
