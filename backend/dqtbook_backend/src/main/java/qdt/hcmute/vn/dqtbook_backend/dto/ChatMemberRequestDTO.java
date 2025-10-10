package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMemberRequestDTO {
    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("role_in_chat")
    private String roleInChat;
}