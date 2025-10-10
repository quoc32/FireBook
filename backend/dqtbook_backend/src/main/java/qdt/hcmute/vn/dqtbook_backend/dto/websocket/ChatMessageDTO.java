package qdt.hcmute.vn.dqtbook_backend.dto.websocket;

/**
 * DTO cho tin nhắn WebSocket được gửi từ client tới server
 */
public class ChatMessageDTO {
    private String content;
    private Integer chatId;
    private Integer senderId;
    private String messageType; // TEXT, IMAGE, FILE, etc.

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(String content, Integer chatId, Integer senderId, String messageType) {
        this.content = content;
        this.chatId = chatId;
        this.senderId = senderId;
        this.messageType = messageType;
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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}