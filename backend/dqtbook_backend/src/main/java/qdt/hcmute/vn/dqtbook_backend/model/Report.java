package qdt.hcmute.vn.dqtbook_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "reported_content_type", length = 20, nullable = false)
    private String reportedContentType;

    @Column(name = "reported_content_id", nullable = false)
    private Integer reportedContentId;

    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;
}


