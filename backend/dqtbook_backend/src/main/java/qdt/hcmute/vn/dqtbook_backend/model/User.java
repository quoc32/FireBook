package qdt.hcmute.vn.dqtbook_backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "gender")
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "avatar_url", length = 255)
    @ColumnDefault("'/defaults/avatar.png'")
    private String avatarUrl;

    @Column(name = "cover_photo_url", length = 255)
    @ColumnDefault("'/defaults/cover.png'")
    private String coverPhotoUrl;

    @Column(name = "bio", length = 255)
    private String bio;

    @Column(name = "school_id", length = 20, unique = true)
    private String schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "academic_year", length = 20)
    private String academicYear;

    @Column(name = "role")
    private String role;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "country", length = 80)
    private String country;

    @Column(name = "city", length = 80)
    private String city;

    @Column(name = "education", length = 255)
    private String education;

    @Column(name = "workplace", length = 255)
    private String workplace;

    @Column(name = "facebook_url", length = 255)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 255)
    private String instagramUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "twitter_url", length = 255)
    private String twitterUrl;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    private Instant updatedAt;
}


