package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    @JsonProperty("notification_id")
    private Integer notificationId;

    @JsonProperty("sender_id")
    private Integer senderId;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("sender_avatar_url")
    private String senderAvatarUrl;

    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private String content;

    @JsonProperty("link")
    private String link;

    @JsonProperty("is_read")
    private boolean isRead;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}