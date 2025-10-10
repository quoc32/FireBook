package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMemberDTO {
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("user_full_name")
    private String userFullName;

    @JsonProperty("user_avatar_url")
    private String userAvatarUrl;

    @JsonProperty("role_in_chat")
    private String roleInChat;
}