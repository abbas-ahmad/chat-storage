package bytecode.rag_chat_storage.controller;

import bytecode.rag_chat_storage.dto.ChatSessionDto;
import bytecode.rag_chat_storage.dto.CreateChatSessionRequest;
import bytecode.rag_chat_storage.dto.SessionStatsDto;
import bytecode.rag_chat_storage.dto.UpdateChatSessionRequest;
import bytecode.rag_chat_storage.service.ChatSessionService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Chat Session Management", description = "APIs for managing chat sessions")
@SecurityRequirement(name = "ApiKeyAuth")
@RequiredArgsConstructor
public class ChatSessionController {
    private static final Logger logger = LoggerFactory.getLogger(ChatSessionController.class);
    private final ChatSessionService chatSessionService;

    @PostMapping
    @Operation(summary = "Create a new chat session", description = "Creates a new chat session for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chat session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<ChatSessionDto> createChatSession(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody CreateChatSessionRequest request) {

        logger.info("Creating chat session for user: {}", userId);
        ChatSessionDto session = chatSessionService.createChatSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping
    @Operation(summary = "Get chat sessions", description = "Retrieves chat sessions for the authenticated user with optional pagination, favorite filter, search, and stats.")
    public ResponseEntity<?> getChatSessions(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(value = "favorite", required = false) Boolean favorite,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "includeStats", required = false, defaultValue = "false") boolean includeStats) {

        logger.info("Retrieving chat sessions for user: {} with params - limit: {}, offset: {}, favorite: {}, search: {}, includeStats: {}",
                userId, limit, offset, favorite, search, includeStats);

        var result = chatSessionService.getSessions(userId, limit, offset, favorite, search, includeStats);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "Get a specific chat session", description = "Retrieves a specific chat session with its messages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat session retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<ChatSessionDto> getChatSession(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId) {

        logger.info("Retrieving chat session: {} for user: {}", sessionId, userId);
        ChatSessionDto session = chatSessionService.getChatSession(userId, sessionId);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = "Update a chat session", description = "Updates the name of a chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat session updated successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<ChatSessionDto> updateChatSession(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId,
            @Valid @RequestBody UpdateChatSessionRequest request) {

        logger.info("Updating chat session: {} for user: {}", sessionId, userId);
        ChatSessionDto session = chatSessionService.updateChatSession(userId, sessionId, request);
        return ResponseEntity.ok(session);
    }

    @PatchMapping("/{sessionId}/favorite")
    @Operation(summary = "Toggle favorite status", description = "Toggles the favorite status of a chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<ChatSessionDto> toggleFavorite(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId) {

        logger.info("Toggling favorite status for session: {} for user: {}", sessionId, userId);
        ChatSessionDto session = chatSessionService.toggleFavorite(userId, sessionId);
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "Delete a chat session", description = "Deletes a chat session and all its messages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chat session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Chat session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key")
    })
    public ResponseEntity<Void> deleteChatSession(
            @Parameter(description = "User ID from the authenticated request") @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Chat session ID") @PathVariable Long sessionId) {

        logger.info("Deleting chat session: {} for user: {}", sessionId, userId);
        chatSessionService.deleteChatSession(userId, sessionId);
        return ResponseEntity.noContent().build();
    }
}
