package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class MessageSummaryDTO {
    @JsonProperty("message_id")
    private Long id;

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
}