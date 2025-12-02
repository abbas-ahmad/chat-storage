package store.chat_storage.controller;

import store.chat_storage.dto.AddMessageRequest;
import store.chat_storage.dto.ChatMessageDto;
import store.chat_storage.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@Tag(name = "Chat Message Management", description = "APIs for managing chat messages within sessions")
@SecurityRequirement(name = "ApiKeyAuth")
@RequiredArgsConstructor
public class ChatMessageController {
    private static final Logger logger = LoggerFactory.getLogger(ChatMessageController.class);
    private final ChatMessageService chatMessageService;

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
    @Operation(summary = "Get messages in a chat session", description = "Retrieves messages for a chat session with optional pagination, sender filter, latest, and count-only.")
    public ResponseEntity<?> getMessages(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable Long sessionId,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(value = "senderType", required = false) String senderType,
            @RequestParam(value = "latest", required = false, defaultValue = "false") boolean latest,
            @RequestParam(value = "countOnly", required = false, defaultValue = "false") boolean countOnly) {

        logger.info("Retrieving messages for session: {} for user: {} with params - limit: {}, offset: {}, senderType: {}, latest: {}, countOnly: {}",
                sessionId, userId, limit, offset, senderType, latest, countOnly);
        var result = chatMessageService.getMessages(userId, sessionId, limit, offset, senderType, latest, countOnly);
        return ResponseEntity.ok(result);
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
}
