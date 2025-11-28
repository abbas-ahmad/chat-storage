package bytecode.rag_chat_storage.controller;

import bytecode.rag_chat_storage.dto.AddMessageRequest;
import bytecode.rag_chat_storage.dto.ChatMessageDto;
import bytecode.rag_chat_storage.entity.ChatMessage;
import bytecode.rag_chat_storage.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@Tag(name = "Chat Message Management", description = "APIs for managing chat messages within sessions")
public class ChatMessageController {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping
    @Operation(summary = "Add a message to a chat session", description = "Adds a new message to the specified chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<ChatMessageDto> addMessage(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId,
            @Valid @RequestBody AddMessageRequest request) {
        
        logger.info("Adding message to session: {} for user: {}", sessionId, userId);
        ChatMessageDto message = chatMessageService.addMessage(userId, sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping
    @Operation(summary = "Get all messages in a chat session", description = "Retrieves all messages for the specified chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId) {
        
        logger.info("Retrieving messages for session: {} for user: {}", sessionId, userId);
        List<ChatMessageDto> messages = chatMessageService.getMessagesBySessionId(userId, sessionId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Get messages with pagination", description = "Retrieves messages for a chat session with pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<Page<ChatMessageDto>> getMessages(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Retrieving messages for session: {} for user: {} with pagination - page: {}, size: {}", 
                   sessionId, userId, page, size);
        Page<ChatMessageDto> messages = chatMessageService.getMessagesBySessionId(userId, sessionId, page, size);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{messageId}")
    @Operation(summary = "Get a specific message", description = "Retrieves a specific message by ID from a chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Message or chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<ChatMessageDto> getMessage(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId,
            @Parameter(description = "Message ID") @PathVariable Long messageId) {
        
        logger.info("Retrieving message: {} from session: {} for user: {}", messageId, sessionId, userId);
        ChatMessageDto message = chatMessageService.getMessage(userId, sessionId, messageId);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete a specific message", description = "Deletes a specific message from a chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Message deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Message or chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId,
            @Parameter(description = "Message ID") @PathVariable Long messageId) {
        
        logger.info("Deleting message: {} from session: {} for user: {}", messageId, sessionId, userId);
        chatMessageService.deleteMessage(userId, sessionId, messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest messages", description = "Retrieves the latest N messages from a chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<List<ChatMessageDto>> getLatestMessages(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId,
            @Parameter(description = "Number of latest messages to retrieve") @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Retrieving latest {} messages for session: {} for user: {}", limit, sessionId, userId);
        List<ChatMessageDto> messages = chatMessageService.getLatestMessages(userId, sessionId, limit);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/by-sender/{senderType}")
    @Operation(summary = "Get messages by sender type", description = "Retrieves messages filtered by sender type (USER or ASSISTANT)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "400", description = "Invalid sender type"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<List<ChatMessageDto>> getMessagesBySenderType(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId,
            @Parameter(description = "Sender type (USER or ASSISTANT)") @PathVariable ChatMessage.SenderType senderType) {
        
        logger.info("Retrieving {} messages for session: {} for user: {}", senderType, sessionId, userId);
        List<ChatMessageDto> messages = chatMessageService.getMessagesBySenderType(userId, sessionId, senderType);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/count")
    @Operation(summary = "Get message count", description = "Retrieves the total number of messages in a chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<Long> getMessageCount(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId) {
        
        logger.info("Retrieving message count for session: {} for user: {}", sessionId, userId);
        long count = chatMessageService.getMessageCount(userId, sessionId);
        return ResponseEntity.ok(count);
    }
}
