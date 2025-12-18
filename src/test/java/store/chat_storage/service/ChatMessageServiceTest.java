package store.chat_storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import store.chat_storage.dto.AddMessageRequest;
import store.chat_storage.dto.ChatMessageDto;
import store.chat_storage.entity.ChatMessage;
import store.chat_storage.entity.ChatSession;
import store.chat_storage.exception.ResourceNotFoundException;
import store.chat_storage.repository.ChatMessageRepository;
import store.chat_storage.repository.ChatSessionRepository;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddMessage() {
        String userId = "user123";
        Long sessionId = 1L;
        AddMessageRequest request = new AddMessageRequest(ChatMessage.SenderType.USER, "Hello", null);
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessageDto result = chatMessageService.addMessage(userId, sessionId, request);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void testGetMessagesBySessionId() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        ChatMessage message = new ChatMessage();
        message.setContent("Hello");
        message.setChatSession(session); // Ensure chatSession is set

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(message));

        List<ChatMessageDto> result = chatMessageService.getMessagesBySessionId(userId, sessionId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getContent());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetMessagesBySessionIdWithPagination() {
        String userId = "user123";
        Long sessionId = 1L;
        int page = 0;
        int size = 10;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        ChatMessage message = new ChatMessage();
        message.setContent("Hello");
        message.setChatSession(session); // Ensure chatSession is set

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(message)));

        Page<ChatMessageDto> result = chatMessageService.getMessagesBySessionId(userId, sessionId, page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Hello", result.getContent().get(0).getContent());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testGetMessage() {
        String userId = "user123";
        Long sessionId = 1L;
        Long messageId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        ChatMessage message = new ChatMessage();
        message.setId(messageId);
        message.setContent("Hello");
        message.setChatSession(session); // Ensure chatSession is set

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findOne(any(Specification.class))).thenReturn(Optional.of(message));

        ChatMessageDto result = chatMessageService.getMessage(userId, sessionId, messageId);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findOne(any(Specification.class));
    }

    @Test
    void testDeleteMessage() {
        String userId = "user123";
        Long sessionId = 1L;
        Long messageId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        ChatMessage message = new ChatMessage();
        message.setId(messageId);

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findOne(any(Specification.class))).thenReturn(Optional.of(message));

        chatMessageService.deleteMessage(userId, sessionId, messageId);

        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).delete(any(ChatMessage.class));
    }

    @Test
    void testDeleteMessagesBySessionId() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        ChatMessage message = new ChatMessage();

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findAll(any(Specification.class))).thenReturn(Collections.singletonList(message));

        chatMessageService.deleteMessagesBySessionId(userId, sessionId);

        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findAll(any(Specification.class));
        verify(chatMessageRepository, times(1)).deleteAll(anyList());
    }

    @Test
    void testGetMessagesWithCountOnly() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.count(any(Specification.class))).thenReturn(5L);

        Object result = chatMessageService.getMessages(userId, sessionId, 10, 0, null, false, true);

        assertNotNull(result);
        assertEquals(5L, result);
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).count(any(Specification.class));
    }

    @Test
    void testGetMessages_InvalidSenderType() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new ChatSession()));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                chatMessageService.getMessages(userId, sessionId, 10, 0, "INVALID_TYPE", false, false));

        assertEquals("Invalid senderType: INVALID_TYPE", exception.getMessage());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
    }

    @Test
    void testGetMessages_CountOnly() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new ChatSession()));
        when(chatMessageRepository.count(any(Specification.class))).thenReturn(5L);

        Object result = chatMessageService.getMessages(userId, sessionId, 10, 0, null, false, true);

        assertNotNull(result);
        assertEquals(5L, result);
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).count(any(Specification.class));
    }

    @Test
    void testDeleteMessagesBySessionId_NonExistentSession() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                chatMessageService.deleteMessagesBySessionId(userId, sessionId));

        assertEquals("Chat session not found with id: 1", exception.getMessage());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
    }

    @Test
    void testAddMessage_NonExistentSession() {
        String userId = "user123";
        Long sessionId = 1L;
        AddMessageRequest request = new AddMessageRequest(ChatMessage.SenderType.USER, "Hello", null);

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                chatMessageService.addMessage(userId, sessionId, request));

        assertEquals("Chat session not found with id: 1", exception.getMessage());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
    }

    @Test
    void testGetMessages_InvalidSession() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                chatMessageService.getMessagesBySessionId(userId, sessionId));

        assertEquals("Chat session not found with id: 1", exception.getMessage());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
    }

    @Test
    void testDeleteMessagesBySessionId_InvalidSession() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                chatMessageService.deleteMessagesBySessionId(userId, sessionId));

        assertEquals("Chat session not found with id: 1", exception.getMessage());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
    }

    @Test
    void testGetMessages_WithSenderType() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        ChatMessage message = new ChatMessage();
        message.setContent("Hello");
        message.setSenderType(ChatMessage.SenderType.USER);
        message.setChatSession(session);

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(message)));

        Object result = chatMessageService.getMessages(userId, sessionId, 10, 0, "USER", false, false);

        assertNotNull(result);
        assertInstanceOf(Page.class, result);
        Page<ChatMessageDto> page = (Page<ChatMessageDto>) result;
        assertEquals(1, page.getTotalElements());
        assertEquals("Hello", page.getContent().get(0).getContent());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetMessages_Latest() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        ChatMessage message = new ChatMessage();
        message.setContent("Latest Message");
        message.setChatSession(session);

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(message)));

        Object result = chatMessageService.getMessages(userId, sessionId, 1, 0, null, true, false);

        assertNotNull(result);
        assertInstanceOf(Page.class, result);
        Page<ChatMessageDto> page = (Page<ChatMessageDto>) result;
        assertEquals(1, page.getTotalElements());
        assertEquals("Latest Message", page.getContent().get(0).getContent());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void testGetMessagesBySessionId_NoMessages() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);

        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));
        when(chatMessageRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        List<ChatMessageDto> result = chatMessageService.getMessagesBySessionId(userId, sessionId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
        verify(chatMessageRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetMessages_InvalidPagination() {
        String userId = "user123";
        Long sessionId = 1L;
        int page = -1;
        int size = 0;

        // Mock the chatSessionRepository to return a valid session to avoid ResourceNotFoundException
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        when(chatSessionRepository.findOne(any(Specification.class))).thenReturn(Optional.of(session));

        // Validate that an IllegalArgumentException is thrown for invalid pagination parameters
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                chatMessageService.getMessages(userId, sessionId, page, size, null, false, false));

        assertEquals("Page size must not be less than one", exception.getMessage());

        // Verify that the repository method was called
        verify(chatSessionRepository, times(1)).findOne(any(Specification.class));
    }
}
