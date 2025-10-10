package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatUpdateRequestDTO {
    @JsonProperty("chat_name")
    private String chatName;

    @JsonProperty("chat_avatar_url")
    private String chatAvatarUrl;
}