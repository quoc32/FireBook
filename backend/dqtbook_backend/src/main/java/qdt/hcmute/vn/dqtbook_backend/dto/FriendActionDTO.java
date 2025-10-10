package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendActionDTO {
    @JsonProperty("sender_id")
    private Integer senderId;    // user_id_1: người gửi yêu cầu
    
    @JsonProperty("receiver_id")
    private Integer receiverId;  // user_id_2: người nhận yêu cầu
}