package bytecode.rag_chat_storage.service;


import bytecode.rag_chat_storage.dto.AddMessageRequest;
import bytecode.rag_chat_storage.dto.ChatMessageDto;
import bytecode.rag_chat_storage.entity.ChatMessage;
import bytecode.rag_chat_storage.entity.ChatSession;
import bytecode.rag_chat_storage.exception.ResourceNotFoundException;
import bytecode.rag_chat_storage.repository.ChatMessageRepository;
import bytecode.rag_chat_storage.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatSessionServiceTest {

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    private ChatSession session;
    private ChatMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        session = new ChatSession();
        session.setId(1L);
        session.setUserId("user1");

        message = new ChatMessage();
        message.setId(1L);
        message.setChatSession(session);
        message.setSenderType(ChatMessage.SenderType.USER);
        message.setContent("Hello");
    }

    @Test
    void addMessage_success() {
        AddMessageRequest request = new AddMessageRequest();
        request.setSenderType(ChatMessage.SenderType.USER);
        request.setContent("Hello");

        when(chatSessionRepository.findByIdAndUserId(1L, "user1"))
                .thenReturn(Optional.of(session));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);

        ChatMessageDto result = chatMessageService.addMessage("user1", 1L, request);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void addMessage_sessionNotFound() {
        AddMessageRequest request = new AddMessageRequest();
        request.setSenderType(ChatMessage.SenderType.USER);
        request.setContent("Hello");

        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                chatMessageService.addMessage("user1", 1L, request)
        );
    }

    @Test
    void getMessagesBySessionId_success() {
        when(chatSessionRepository.findByIdAndUserId(1L, "user1"))
                .thenReturn(Optional.of(session));
        when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session))
                .thenReturn(List.of(message));

        List<ChatMessageDto> messages = chatMessageService.getMessagesBySessionId("user1", 1L);

        assertEquals(1, messages.size());
        assertEquals("Hello", messages.get(0).getContent());
    }

    @Test
    void getMessagesBySessionId_paginated() {
        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.of(session));
        Page<ChatMessage> page = new PageImpl<>(List.of(message));
        when(chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(eq(session), any(Pageable.class)))
                .thenReturn(page);

        Page<ChatMessageDto> result = chatMessageService.getMessagesBySessionId("user1", 1L, 0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals("Hello", result.getContent().get(0).getContent());
    }

    @Test
    void getMessage_success() {
        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.of(session));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(message));

        ChatMessageDto result = chatMessageService.getMessage("user1", 1L, 1L);

        assertEquals("Hello", result.getContent());
    }

    @Test
    void getMessage_messageNotInSession() {
        ChatSession otherSession = new ChatSession();
        otherSession.setId(2L);
        message.setChatSession(otherSession);

        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.of(session));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThrows(ResourceNotFoundException.class, () ->
                chatMessageService.getMessage("user1", 1L, 1L)
        );
    }

    @Test
    void deleteMessage_success() {
        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.of(session));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(message));

        chatMessageService.deleteMessage("user1", 1L, 1L);

        verify(chatMessageRepository, times(1)).delete(message);
    }

    @Test
    void getMessageCount_success() {
        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.of(session));
        when(chatMessageRepository.countByChatSession(session)).thenReturn(5L);

        long count = chatMessageService.getMessageCount("user1", 1L);

        assertEquals(5L, count);
    }

    @Test
    void getLatestMessages_success() {
        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.of(session));
        when(chatMessageRepository.findLatestByChatSession(eq(session), any(Pageable.class)))
                .thenReturn(List.of(message));

        List<ChatMessageDto> messages = chatMessageService.getLatestMessages("user1", 1L, 10);

        assertEquals(1, messages.size());
    }

    @Test
    void getMessagesBySenderType_success() {
        when(chatSessionRepository.findByIdAndUserId(1L, "user1")).thenReturn(Optional.of(session));
        when(chatMessageRepository.findByChatSessionAndSenderTypeOrderByCreatedAtAsc(session, ChatMessage.SenderType.USER))
                .thenReturn(List.of(message));

        List<ChatMessageDto> messages = chatMessageService.getMessagesBySenderType("user1", 1L, ChatMessage.SenderType.USER);

        assertEquals(1, messages.size());
    }


}

