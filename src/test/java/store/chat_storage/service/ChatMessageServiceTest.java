package store.chat_storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import store.chat_storage.dto.AddMessageRequest;
import store.chat_storage.dto.ChatMessageDto;
import store.chat_storage.entity.ChatMessage;
import store.chat_storage.entity.ChatSession;
import store.chat_storage.exception.ResourceNotFoundException;
import store.chat_storage.repository.ChatMessageRepository;
import store.chat_storage.repository.ChatSessionRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        Long sessionId = 1L;
        ChatSession chatSession = ChatSession.builder().id(sessionId).userId("user123").build();
        ChatMessage chatMessage = ChatMessage.builder()
                .id(1L)
                .chatSession(chatSession)
                .senderType(ChatMessage.SenderType.USER)
                .content("Hello")
                .createdAt(LocalDateTime.now())
                .build();

        AddMessageRequest request = new AddMessageRequest();
        request.setSenderType(ChatMessage.SenderType.USER);
        request.setContent("Hello");
        request.setContext(null);

        when(chatSessionRepository.findByIdAndUserId(sessionId, "user123")).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        ChatMessageDto result = chatMessageService.addMessage("user123", sessionId, request);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, "user123");
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void testGetMessage() {
        String userId = "user123";
        Long sessionId = 1L;
        Long messageId = 1L;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id(messageId)
                .chatSession(chatSession)
                .senderType(ChatMessage.SenderType.USER)
                .content("Hello")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(chatMessage));

        ChatMessageDto result = chatMessageService.getMessage(userId, sessionId, messageId);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findById(messageId);
    }

    @Test
    void testGetMessage_NotFound() {
        String userId = "user123";
        Long sessionId = 1L;
        Long messageId = 1L;

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(ChatSession.builder().id(sessionId).userId(userId).build()));
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatMessageService.getMessage(userId, sessionId, messageId));
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findById(messageId);
    }

    @Test
    void testGetMessagesBySessionId_ChatSessionNotFound() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatMessageService.getMessagesBySessionId(userId, sessionId));
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, never()).findByChatSessionOrderByCreatedAtAsc(any());
    }

    @Test
    void testGetMessagesBySessionId_Success() {
        String userId = "user123";
        Long sessionId = 1L;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id(1L)
                .chatSession(chatSession)
                .senderType(ChatMessage.SenderType.USER)
                .content("Hello")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(chatSession))
                .thenReturn(Collections.singletonList(chatMessage));

        List<ChatMessageDto> result = chatMessageService.getMessagesBySessionId(userId, sessionId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hello", result.get(0).getContent());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findByChatSessionOrderByCreatedAtAsc(chatSession);
    }

    @Test
    void testGetMessagesBySessionId_EmptyMessages() {
        String userId = "user123";
        Long sessionId = 1L;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(chatSession))
                .thenReturn(Collections.emptyList());

        List<ChatMessageDto> result = chatMessageService.getMessagesBySessionId(userId, sessionId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findByChatSessionOrderByCreatedAtAsc(chatSession);
    }

    @Test
    void testGetMessagesBySessionId_InvalidSession() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatMessageService.getMessagesBySessionId(userId, sessionId));
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, never()).findByChatSessionOrderByCreatedAtAsc(any());
    }

    @Test
    void testGetMessagesBySessionIdWithPagination() {
        String userId = "user123";
        Long sessionId = 1L;
        int page = 0;
        int size = 10;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id(1L)
                .chatSession(chatSession)
                .senderType(ChatMessage.SenderType.USER)
                .content("Hello")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(eq(chatSession), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(chatMessage)));

        Page<ChatMessageDto> result = chatMessageService.getMessagesBySessionId(userId, sessionId, page, size);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Hello", result.getContent().get(0).getContent());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findByChatSessionOrderByCreatedAtAsc(eq(chatSession), any(Pageable.class));
    }

    @Test
    void testDeleteMessage() {
        String userId = "user123";
        Long sessionId = 1L;
        Long messageId = 1L;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id(messageId)
                .chatSession(chatSession)
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findById(messageId)).thenReturn(Optional.of(chatMessage));

        chatMessageService.deleteMessage(userId, sessionId, messageId);

        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findById(messageId);
        verify(chatMessageRepository, times(1)).delete(chatMessage);
    }

    @Test
    void testDeleteMessagesBySessionId() {
        String userId = "user123";
        Long sessionId = 1L;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        doNothing().when(chatMessageRepository).deleteByChatSession(chatSession);

        chatMessageService.deleteMessagesBySessionId(userId, sessionId);

        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).deleteByChatSession(chatSession);
    }

    @Test
    void testGetMessagesWithCountOnly() {
        String userId = "user123";
        Long sessionId = 1L;
        boolean countOnly = true;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.countByChatSession(chatSession)).thenReturn(5L);

        Object result = chatMessageService.getMessages(userId, sessionId, 0, 0, null, false, countOnly);

        assertNotNull(result);
        assertEquals(5L, result);
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).countByChatSession(chatSession);
    }

    @Test
    void testGetMessagesWithLatest() {
        String userId = "user123";
        Long sessionId = 1L;
        int limit = 1;
        boolean latest = true;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id(1L)
                .chatSession(chatSession)
                .senderType(ChatMessage.SenderType.USER)
                .content("Latest message")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findLatestByChatSession(eq(chatSession), any(Pageable.class)))
                .thenReturn(Collections.singletonList(chatMessage));

        Object result = chatMessageService.getMessages(userId, sessionId, limit, 0, null, latest, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> messages = (List<?>) result;
        assertEquals(1, messages.size());
        assertEquals("Latest message", ((ChatMessageDto) messages.get(0)).getContent());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findLatestByChatSession(eq(chatSession), any(Pageable.class));
    }

    @Test
    void testGetMessagesWithSenderType() {
        String userId = "user123";
        Long sessionId = 1L;
        String senderType = "USER";

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id(1L)
                .chatSession(chatSession)
                .senderType(ChatMessage.SenderType.USER)
                .content("Filtered message")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findByChatSessionAndSenderTypeOrderByCreatedAtAsc(chatSession, ChatMessage.SenderType.USER))
                .thenReturn(Collections.singletonList(chatMessage));

        Object result = chatMessageService.getMessages(userId, sessionId, 0, 0, senderType, false, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> messages = (List<?>) result;
        assertEquals(1, messages.size());
        assertEquals("Filtered message", ((ChatMessageDto) messages.get(0)).getContent());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findByChatSessionAndSenderTypeOrderByCreatedAtAsc(chatSession, ChatMessage.SenderType.USER);
    }

    @Test
    void testGetMessagesWithPagination() {
        String userId = "user123";
        Long sessionId = 1L;
        int limit = 10;
        int offset = 0;

        ChatSession chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .build();

        ChatMessage chatMessage = ChatMessage.builder()
                .id(1L)
                .chatSession(chatSession)
                .senderType(ChatMessage.SenderType.USER)
                .content("Paginated message")
                .createdAt(LocalDateTime.now())
                .build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(chatSession));
        when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(eq(chatSession), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(chatMessage)));

        Object result = chatMessageService.getMessages(userId, sessionId, limit, offset, null, false, false);

        assertNotNull(result);
        assertTrue(result instanceof Page);
        Page<?> messages = (Page<?>) result;
        assertEquals(1, messages.getContent().size());
        assertEquals("Paginated message", ((ChatMessageDto) messages.getContent().get(0)).getContent());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageRepository, times(1)).findByChatSessionOrderByCreatedAtAsc(eq(chatSession), any(Pageable.class));
    }
}
