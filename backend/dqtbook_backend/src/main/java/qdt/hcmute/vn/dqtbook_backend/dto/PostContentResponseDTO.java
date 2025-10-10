package qdt.hcmute.vn.dqtbook_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.time.Instant;

@Getter
@Setter
public class PostContentResponseDTO {
    private Integer id;
    private String content;
    private String visibility;
    private String postType;
    private String status;
    private AuthorDTO author;
    private List<MediaDTO> media;
    private Instant createdAt;

    @Getter
    @Setter
    public static class MediaDTO {
        private String mediaType;
        private String mediaUrl;

        public MediaDTO(String mediaType, String mediaUrl) {
            this.mediaType = mediaType;
            this.mediaUrl = mediaUrl;
        }
    }

    @Getter
    @Setter
    public static class AuthorDTO {
        private Integer id;
        private String fullName;
        private String avatarUrl;
        private String email;
    }
}
