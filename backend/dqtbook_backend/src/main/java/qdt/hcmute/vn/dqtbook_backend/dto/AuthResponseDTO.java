package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDTO {

    private String message;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private Integer userId;

    private String email;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String gender;

    private String bio;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("cover_photo_url")
    private String coverPhotoUrl;
}
