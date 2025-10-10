package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostShareRequest {
    @JsonProperty("post_id")
    private Integer postId;

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("shared_text")
    private String sharedText;

    @JsonProperty("visibility")
    private String visibility;
}
