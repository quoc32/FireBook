package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import qdt.hcmute.vn.dqtbook_backend.dto.websocket.ChatMessageDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.websocket.ChatMessageResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.websocket.ReadStatusDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.websocket.TypingStatusDTO;
import qdt.hcmute.vn.dqtbook_backend.service.MessageService;

@Controller
public class WebSocketChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    /**
     * Xử lý tin nhắn mới từ client
     * Client sẽ gửi tin nhắn đến /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void processMessage(@Payload ChatMessageDTO chatMessageDTO) {
        // Lưu tin nhắn vào database thông qua MessageService
        ChatMessageResponseDTO responseDTO = messageService.saveAndBroadcastMessage(chatMessageDTO);

        // Gửi tin nhắn đến tất cả thành viên trong chat
        String destination = "/topic/chat." + chatMessageDTO.getChatId();
        messagingTemplate.convertAndSend(destination, responseDTO);
    }

    /**
     * Xử lý cập nhật trạng thái đọc tin nhắn
     * Client sẽ gửi thông báo đến /app/chat.readStatus
     */
    @MessageMapping("/chat.readStatus")
    public void processReadStatus(@Payload ReadStatusDTO readStatusDTO) {
        // Cập nhật trạng thái đã đọc
        messageService.markMessageAsRead(readStatusDTO);

        // Gửi cập nhật trạng thái đến tất cả thành viên trong chat
        String destination = "/topic/chat." + readStatusDTO.getChatId() + ".readStatus";
        messagingTemplate.convertAndSend(destination, readStatusDTO);
    }

    /**
     * Xử lý thông báo khi người dùng đang nhập tin nhắn
     * Client sẽ gửi thông báo đến /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void processTypingStatus(@Payload TypingStatusDTO typingStatus) {
        // Chuyển tiếp thông báo đang nhập tin nhắn
        String destination = "/topic/chat." + typingStatus.getChatId() + ".typing";
        messagingTemplate.convertAndSend(destination, typingStatus);
    }
}