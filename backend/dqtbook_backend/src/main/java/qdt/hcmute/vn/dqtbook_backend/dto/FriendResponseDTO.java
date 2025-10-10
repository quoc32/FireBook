package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class FriendResponseDTO {
    @JsonProperty("sender_id")
    private Integer senderId;    // user_id_1: người gửi yêu cầu
    
    @JsonProperty("receiver_id")
    private Integer receiverId;  // user_id_2: người nhận yêu cầu
    
    private String status;
    
    @JsonProperty("created_at")
    private Instant createdAt;
    
    @JsonProperty("updated_at")
    private Instant updatedAt;
    
    // User info for display
    @JsonProperty("friend_info")
    private UserResponseDTO friendInfo;
}