package qdt.hcmute.vn.dqtbook_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostCreateRequestDTO {
    @JsonProperty("author_id")
    private Integer authorId;

    private String content;

    private String visibility;

    @JsonProperty("post_type")
    private String postType;

    private String status;

    private List<MediaItem> media;

    @Getter
    @Setter
    public static class MediaItem {
        @JsonProperty("media_type")
        private String mediaType;

        @JsonProperty("media_url")
        private String mediaUrl;
    }
}
