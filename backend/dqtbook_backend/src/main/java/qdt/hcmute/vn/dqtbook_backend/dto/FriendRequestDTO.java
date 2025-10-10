package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestDTO {
    @JsonProperty("sender_id")
    private Integer senderId;
    
    @JsonProperty("receiver_id")
    private Integer receiverId;
}
