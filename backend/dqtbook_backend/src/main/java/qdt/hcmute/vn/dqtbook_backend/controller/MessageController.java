package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import qdt.hcmute.vn.dqtbook_backend.dto.MessageRequestDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.MessageResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Get messages for a specific chat
     * 
     * @param chatId The chat's ID
     * @param userId The requesting user's ID
     * @return List of message response DTOs
     */
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageResponseDTO>> getMessagesForChat(
            @PathVariable Integer chatId,
            @RequestParam Integer userId) {
        List<MessageResponseDTO> messages = messageService.getMessagesForChat(chatId, userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get a specific message by ID
     * 
     * @param id     The message's ID
     * @param userId The requesting user's ID
     * @return Message response DTO if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMessageById(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        return messageService.getMessageById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Send a new message
     * 
     * @param request The message request DTO
     * @return The created message response DTO if successful
     */
    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody MessageRequestDTO request) {
        return messageService.sendMessage(request)
                .map(message -> ResponseEntity.status(HttpStatus.CREATED).body(message))
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * Delete a message
     * 
     * @param id     The message's ID
     * @param userId The ID of the user deleting the message
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        boolean deleted = messageService.deleteMessage(id, userId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark a message as read
     * 
     * @param id     The message's ID
     * @param userId The user's ID
     * @return No content if successful
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable Long id,
            @RequestParam Integer userId) {
        boolean marked = messageService.markMessageAsRead(id, userId);
        if (marked) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark all messages in a chat as read
     * 
     * @param chatId The chat's ID
     * @param userId The user's ID
     * @return No content if successful
     */
    @PostMapping("/chat/{chatId}/read-all")
    public ResponseEntity<?> markAllMessagesAsRead(
            @PathVariable Integer chatId,
            @RequestParam Integer userId) {
        boolean marked = messageService.markAllMessagesAsRead(chatId, userId);
        if (marked) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}