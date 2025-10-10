package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qdt.hcmute.vn.dqtbook_backend.dto.*;
import qdt.hcmute.vn.dqtbook_backend.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Get all chats for a specific user
     * 
     * @param userId The user's ID
     * @return List of chat response DTOs
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatResponseDTO>> getChatsByUserId(@PathVariable Integer userId) {
        List<ChatResponseDTO> chats = chatService.getChatsByUserId(userId);
        return ResponseEntity.ok(chats);
    }

    /**
     * Get a specific chat by ID
     * 
     * @param id     The chat's ID
     * @param userId The requesting user's ID
     * @return Chat response DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatById(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        return chatService.getChatById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new chat
     * 
     * @param request The chat create request DTO
     * @return The created chat response DTO if successful
     */
    @PostMapping
    public ResponseEntity<?> createChat(@RequestBody ChatCreateRequestDTO request) {
        return chatService.createChat(request)
                .map(chat -> ResponseEntity.status(HttpStatus.CREATED).body(chat))
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * Update an existing chat
     * 
     * @param id      The chat's ID
     * @param request The chat update request DTO
     * @param userId  The requesting user's ID
     * @return The updated chat response DTO if successful
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateChat(
            @PathVariable Integer id,
            @RequestBody ChatUpdateRequestDTO request,
            @RequestParam Integer userId) {
        return chatService.updateChat(id, request, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * Delete a chat
     * 
     * @param id     The chat's ID
     * @param userId The requesting user's ID
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        boolean deleted = chatService.deleteChat(id, userId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Add a member to a chat
     * 
     * @param chatId      The chat's ID
     * @param request     The chat member request DTO
     * @param adminUserId The ID of the user adding the member
     * @return No content if successful
     */
    @PostMapping("/{chatId}/members")
    public ResponseEntity<?> addChatMember(
            @PathVariable Integer chatId,
            @RequestBody ChatMemberRequestDTO request,
            @RequestParam Integer adminUserId) {
        boolean added = chatService.addChatMember(chatId, request, adminUserId);
        if (added) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove a member from a chat
     * 
     * @param chatId      The chat's ID
     * @param memberId    The ID of the member to remove
     * @param adminUserId The ID of the user removing the member
     * @return No content if successful
     */
    @DeleteMapping("/{chatId}/members/{memberId}")
    public ResponseEntity<?> removeChatMember(
            @PathVariable Integer chatId,
            @PathVariable Integer memberId,
            @RequestParam Integer adminUserId) {
        boolean removed = chatService.removeChatMember(chatId, memberId, adminUserId);
        if (removed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update a chat member's role
     * 
     * @param chatId      The chat's ID
     * @param request     The chat member request DTO with updated role
     * @param adminUserId The ID of the user updating the role
     * @return No content if successful
     */
    @PutMapping("/{chatId}/members")
    public ResponseEntity<?> updateChatMemberRole(
            @PathVariable Integer chatId,
            @RequestBody ChatMemberRequestDTO request,
            @RequestParam Integer adminUserId) {
        boolean updated = chatService.updateChatMemberRole(chatId, request, adminUserId);
        if (updated) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}