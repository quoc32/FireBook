package qdt.hcmute.vn.dqtbook_backend.dto.websocket;

/**
 * DTO thông báo thay đổi trạng thái đọc tin nhắn
 */
public class ReadStatusDTO {
    private Integer chatId;
    private Integer userId;
    private Long messageId;

    public ReadStatusDTO() {
    }

    public ReadStatusDTO(Integer chatId, Integer userId, Long messageId) {
        this.chatId = chatId;
        this.userId = userId;
        this.messageId = messageId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}