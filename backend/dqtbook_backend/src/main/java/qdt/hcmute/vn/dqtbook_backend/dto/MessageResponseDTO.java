package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class MessageResponseDTO {
    @JsonProperty("message_id")
    private Long id;

    @JsonProperty("chat_id")
    private Integer chatId;

    @JsonProperty("sender_id")
    private Integer senderId;

    @JsonProperty("sender_full_name")
    private String senderFullName;

    @JsonProperty("sender_avatar_url")
    private String senderAvatarUrl;

    private String content;

    @JsonProperty("media_url")
    private String mediaUrl;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("read_by")
    private List<MessageReadDTO> readBy;

    @Getter
    @Setter
    public static class MessageReadDTO {
        @JsonProperty("user_id")
        private Integer userId;

        @JsonProperty("user_full_name")
        private String userFullName;

        @JsonProperty("read_at")
        private Instant readAt;
    }
}