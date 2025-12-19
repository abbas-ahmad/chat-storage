package store.chat_storage.service;

import store.chat_storage.dto.ChatMessageDto;
import store.chat_storage.dto.AddMessageRequest;

import org.springframework.data.domain.Page;

import java.util.List;

public interface IChatMessageService {

    ChatMessageDto addMessage(String userId, Long sessionId, AddMessageRequest request);

    List<ChatMessageDto> getMessagesBySessionId(String userId, Long sessionId);

    Page<ChatMessageDto> getMessagesBySessionId(String userId, Long sessionId, int page, int size);

    ChatMessageDto getMessage(String userId, Long sessionId, Long messageId);

    void deleteMessage(String userId, Long sessionId, Long messageId);

    void deleteMessagesBySessionId(String userId, Long sessionId);

    Object getMessages(String userId, Long sessionId, int limit, int offset, String senderType, boolean latest, boolean countOnly);
}
