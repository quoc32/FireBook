package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ChatResponseDTO {
    private Integer id;

    @JsonProperty("chat_name")
    private String chatName;

    @JsonProperty("chat_avatar_url")
    private String chatAvatarUrl;

    @JsonProperty("is_group")
    private Boolean isGroup;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("members")
    private List<ChatMemberDTO> members;

    @JsonProperty("last_message")
    private MessageSummaryDTO lastMessage;

    @JsonProperty("unread_count")
    private Integer unreadCount;
}