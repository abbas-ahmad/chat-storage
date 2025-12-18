package store.chat_storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import store.chat_storage.dto.*;
import store.chat_storage.entity.ChatMessage;
import store.chat_storage.entity.ChatSession;
import store.chat_storage.exception.ResourceNotFoundException;
import store.chat_storage.repository.ChatSessionRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatSessionService chatSessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateChatSession() {
        String userId = "user123";
        CreateChatSessionRequest request = new CreateChatSessionRequest("Test Session");
        ChatSession savedSession = ChatSession.builder()
                .id(1L)
                .userId(userId)
                .name("Test Session")
                .build();

        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(savedSession);

        ChatSessionDto result = chatSessionService.createChatSession(userId, request);

        assertNotNull(result);
        assertEquals("Test Session", result.getName());
        verify(chatSessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void testGetChatSession() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .name("Test Session")
                .build();
        List<ChatMessageDto> messages = Collections.singletonList(
                ChatMessageDto.builder()
                        .id(1L)
                        .chatSessionId(sessionId)
                        .senderType(ChatMessage.SenderType.USER)
                        .content("Hello")
                        .build()
        );

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(chatMessageService.getMessagesBySessionId(userId, sessionId)).thenReturn(messages);

        ChatSessionDto result = chatSessionService.getChatSession(userId, sessionId);

        assertNotNull(result);
        assertEquals("Test Session", result.getName());
        assertEquals(1, result.getMessages().size());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatMessageService, times(1)).getMessagesBySessionId(userId, sessionId);
    }

    @Test
    void testUpdateChatSession() {
        String userId = "user123";
        Long sessionId = 1L;
        UpdateChatSessionRequest request = new UpdateChatSessionRequest("Updated Session");
        ChatSession session = ChatSession.builder().id(sessionId).userId(userId).name("Old Session").build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);

        ChatSessionDto result = chatSessionService.updateChatSession(userId, sessionId, request);

        assertNotNull(result);
        assertEquals("Updated Session", result.getName());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatSessionRepository, times(1)).save(session);
    }

    @Test
    void testToggleFavorite() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = ChatSession.builder().id(sessionId).userId(userId).isFavorite(false).build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(session);

        ChatSessionDto result = chatSessionService.toggleFavorite(userId, sessionId);

        assertNotNull(result);
        assertTrue(result.getIsFavorite());
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
        verify(chatSessionRepository, times(1)).save(session);
    }

    @Test
    void testDeleteChatSession() {
        String userId = "user123";
        Long sessionId = 1L;
        ChatSession session = ChatSession.builder().id(sessionId).userId(userId).build();

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        chatSessionService.deleteChatSession(userId, sessionId);

        verify(chatMessageService, times(1)).deleteMessagesBySessionId(userId, sessionId);
        verify(chatSessionRepository, times(1)).delete(session);
    }

    @Test
    void testGetSessions() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder().id(1L).userId(userId).name("Test Session").build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)).thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, null, null, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1)).findByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }

    @Test
    void testGetSessions_NoFilters_NoStats() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder().id(1L).userId(userId).name("Test Session").build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)).thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, null, null, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1)).findByUserIdOrderByUpdatedAtDesc(userId, pageable);
    }

    @Test
    void testGetSessions_FavoriteFilter() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder().id(1L).userId(userId).name("Favorite Session").isFavorite(true).build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(userId, pageable)).thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, true, null, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1)).findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(userId, pageable);
    }

    @Test
    void testGetSessions_SearchFilter() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        String search = "Test";
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder().id(1L).userId(userId).name("Test Session").build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdAndNameContainingIgnoreCase(userId, search, pageable)).thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, null, search, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1)).findByUserIdAndNameContainingIgnoreCase(userId, search, pageable);
    }

    @Test
    void testGetSessions_FavoriteAndSearchFilter() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        String search = "Favorite";
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder().id(1L).userId(userId).name("Favorite Session").isFavorite(true).build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdAndIsFavoriteTrueAndNameContainingIgnoreCase(userId, search, pageable)).thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, true, search, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1)).findByUserIdAndIsFavoriteTrueAndNameContainingIgnoreCase(userId, search, pageable);
    }

    @Test
    void testGetSessions_WithStats_NoFilters() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder().id(1L).userId(userId).name("Test Session").build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)).thenReturn(sessionsPage);
        when(chatSessionRepository.countByUserId(userId)).thenReturn(5L);
        when(chatSessionRepository.countByUserIdAndIsFavoriteTrue(userId)).thenReturn(2L);

        Object result = chatSessionService.getSessions(userId, limit, offset, null, null, true);

        assertNotNull(result);
        assertTrue(result instanceof ChatSessionListResponse);
        ChatSessionListResponse response = (ChatSessionListResponse) result;
        assertEquals(1, response.getSessions().size());
        assertEquals(5L, response.getStats().getTotalSessions());
        assertEquals(2L, response.getStats().getFavoriteSessions());
        verify(chatSessionRepository, times(1)).findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        verify(chatSessionRepository, times(1)).countByUserId(userId);
        verify(chatSessionRepository, times(1)).countByUserIdAndIsFavoriteTrue(userId);
    }

    @Test
    void testGetSessions_WithStats_Filters() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        String search = "Test";
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder().id(1L).userId(userId).name("Test Session").isFavorite(true).build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdAndIsFavoriteTrueAndNameContainingIgnoreCase(userId, search, pageable)).thenReturn(sessionsPage);
        when(chatSessionRepository.countByUserId(userId)).thenReturn(5L);
        when(chatSessionRepository.countByUserIdAndIsFavoriteTrue(userId)).thenReturn(2L);

        Object result = chatSessionService.getSessions(userId, limit, offset, true, search, true);

        assertNotNull(result);
        assertTrue(result instanceof ChatSessionListResponse);
        ChatSessionListResponse response = (ChatSessionListResponse) result;
        assertEquals(1, response.getSessions().size());
        assertEquals(5L, response.getStats().getTotalSessions());
        assertEquals(2L, response.getStats().getFavoriteSessions());
        verify(chatSessionRepository, times(1)).findByUserIdAndIsFavoriteTrueAndNameContainingIgnoreCase(userId, search, pageable);
        verify(chatSessionRepository, times(1)).countByUserId(userId);
        verify(chatSessionRepository, times(1)).countByUserIdAndIsFavoriteTrue(userId);
    }

    @Test
    void testGetChatSession_NotFound() {
        String userId = "user123";
        Long sessionId = 1L;

        when(chatSessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatSessionService.getChatSession(userId, sessionId));
        verify(chatSessionRepository, times(1)).findByIdAndUserId(sessionId, userId);
    }

    @Test
    void testGetSessions_FavoriteTrue_SearchNotEmpty() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        String search = "Test";
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder()
                .id(1L)
                .userId(userId)
                .name("Test Session")
                .isFavorite(true)
                .build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdAndIsFavoriteTrueAndNameContainingIgnoreCase(userId, search, pageable))
                .thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, true, search, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1))
                .findByUserIdAndIsFavoriteTrueAndNameContainingIgnoreCase(userId, search, pageable);
    }

    @Test
    void testGetSessions_FavoriteTrue_SearchEmpty() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder()
                .id(1L)
                .userId(userId)
                .name("Favorite Session")
                .isFavorite(true)
                .build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(userId, pageable))
                .thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, true, null, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1))
                .findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(userId, pageable);
    }

    @Test
    void testGetSessions_FavoriteFalse_SearchNotEmpty() {
        String userId = "user123";
        int limit = 10;
        int offset = 0;
        String search = "Test";
        PageRequest pageable = PageRequest.of(offset / limit, limit);
        ChatSession session = ChatSession.builder()
                .id(1L)
                .userId(userId)
                .name("Test Session")
                .build();
        Page<ChatSession> sessionsPage = new PageImpl<>(Collections.singletonList(session), pageable, 1);

        when(chatSessionRepository.findByUserIdAndNameContainingIgnoreCase(userId, search, pageable))
                .thenReturn(sessionsPage);

        Object result = chatSessionService.getSessions(userId, limit, offset, false, search, false);

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<?> sessionDtos = (List<?>) result;
        assertEquals(1, sessionDtos.size());
        verify(chatSessionRepository, times(1))
                .findByUserIdAndNameContainingIgnoreCase(userId, search, pageable);
    }
}
