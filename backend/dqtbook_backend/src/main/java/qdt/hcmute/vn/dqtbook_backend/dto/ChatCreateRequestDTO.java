package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ChatCreateRequestDTO {
    @JsonProperty("chat_name")
    private String chatName;

    @JsonProperty("chat_avatar_url")
    private String chatAvatarUrl;

    @JsonProperty("is_group")
    private Boolean isGroup = false;

    @JsonProperty("creator_id")
    private Integer creatorId;

    @JsonProperty("member_ids")
    private List<Integer> memberIds;
}