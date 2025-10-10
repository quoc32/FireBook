package qdt.hcmute.vn.dqtbook_backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "posts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "visibility")
    private String visibility;

    @Column(name = "post_type")
    @com.fasterxml.jackson.annotation.JsonAlias({"post_type", "postType"})
    private String postType;

    // Support older/alternate JSON payloads that send a boolean flag 'isSpecial'
    // This field is transient (not persisted) and when set will update postType accordingly.
    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("isSpecial")
    private Boolean isSpecial;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // >> relationship with PostMedia
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PostMedia> medias;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) this.createdAt = now;
        if (this.updatedAt == null) this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Boolean getIsSpecial() {
        return this.isSpecial;
    }

    public void setIsSpecial(Boolean isSpecial) {
        this.isSpecial = isSpecial;
        if (isSpecial != null) {
            // map boolean to postType values defined in DB ('normal' or 'important')
            this.postType = isSpecial ? "important" : "normal";
        }
    }
}


