package qdt.hcmute.vn.dqtbook_backend.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateResponseDTO {
    private Integer id;
    private String content;
    private String visibility;
    private String postType;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private AuthorDTO author; // lồng DTO con
    private List<MediaDTO> medias; // lồng DTO con

    @Getter
    @Setter
    public static class AuthorDTO {
        private Integer id;
        private String email;
        private String fullName;
        private String firstName;
        private String lastName;
        private String gender;
        private String bio;
    }

    @Getter
    @Setter
    public static class MediaDTO {
        private String mediaType;
        private String mediaUrl;
    }
}
