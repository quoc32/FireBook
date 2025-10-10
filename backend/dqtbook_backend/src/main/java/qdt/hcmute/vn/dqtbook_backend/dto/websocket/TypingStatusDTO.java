package qdt.hcmute.vn.dqtbook_backend.dto.websocket;

/**
 * DTO thông báo trạng thái đang nhập tin nhắn
 */
public class TypingStatusDTO {
    private Integer userId;
    private boolean typing;
    private Integer chatId;

    public TypingStatusDTO() {
    }

    public TypingStatusDTO(Integer userId, boolean typing, Integer chatId) {
        this.userId = userId;
        this.typing = typing;
        this.chatId = chatId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }
}