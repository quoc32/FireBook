package qdt.hcmute.vn.dqtbook_backend.dto.websocket;

import java.time.LocalDateTime;

/**
 * DTO cho tin nhắn WebSocket được gửi từ server tới client
 */
public class ChatMessageResponseDTO {
    private Long id;
    private String content;
    private Integer chatId;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private String messageType;
    private LocalDateTime timestamp;

    public ChatMessageResponseDTO() {
    }

    public ChatMessageResponseDTO(Long id, String content, Integer chatId, Integer senderId,
            String senderName, String senderAvatar, String messageType,
            LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
        this.messageType = messageType;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}