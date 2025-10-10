package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDTO {
    @JsonProperty("chat_id")
    private Integer chatId;

    @JsonProperty("sender_id")
    private Integer senderId;

    private String content;

    @JsonProperty("media_url")
    private String mediaUrl;
}