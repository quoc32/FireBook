package qdt.hcmute.vn.dqtbook_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id", nullable = false)
    private Integer id;

    @Column(name = "chat_name", length = 100)
    private String chatName;

    @Column(name = "chat_avatar_url", length = 255)
    private String chatAvatarUrl;

    @Column(name = "is_group")
    private Boolean isGroup;

    @Column(name = "created_at")
    private Instant createdAt;
}


