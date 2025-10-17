package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qdt.hcmute.vn.dqtbook_backend.model.Notification;
import qdt.hcmute.vn.dqtbook_backend.model.User;
import qdt.hcmute.vn.dqtbook_backend.repository.NotificationRepository;
import qdt.hcmute.vn.dqtbook_backend.dto.NotificationDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createAndSendNotification(User sender, User recipient, String type, Integer sourceId) {
        if (sender == null || recipient == null) return;
        if (Objects.equals(sender.getId(), recipient.getId())) return;

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setSourceId(sourceId);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        LocalDateTime createdAt = LocalDateTime.ofInstant(
                saved.getCreatedAt() != null ? saved.getCreatedAt() : java.time.Instant.now(),
                ZoneId.systemDefault()
        );

        NotificationDTO dto = NotificationDTO.builder()
                .notificationId(saved.getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .senderAvatarUrl(sender.getAvatarUrl())
                .type(type)
                .content(buildContent(sender, type))
                .link(buildLink(type, sourceId, sender.getId()))
                .isRead(false)
                .createdAt(createdAt)
                .build();

        // Chỉ gửi 1 kênh user-queue để tránh hiển thị đôi
        messagingTemplate.convertAndSendToUser(String.valueOf(recipient.getId()),
                "/queue/notifications", dto);

        // Nếu muốn giữ fallback topic (có thể gây trùng nếu FE cũng subscribe), bỏ comment dòng dưới:
        // messagingTemplate.convertAndSend("/topic/notifications." + recipient.getId(), dto);
    }

    private String buildContent(User sender, String type) {
        return switch (type) {
            case "friend_request" -> sender.getFullName() + " đã gửi cho bạn một lời mời kết bạn.";
            case "friend_request_accepted" -> sender.getFullName() + " đã chấp nhận lời mời kết bạn của bạn.";
            case "post_comment" -> sender.getFullName() + " đã bình luận vào bài viết của bạn.";
            case "post_reaction" -> sender.getFullName() + " đã thả reaction bài viết của bạn.";
            case "post_share" -> sender.getFullName() + " đã chia sẻ bài viết của bạn.";
            case "new_important_post", "important_post" -> "Có bài viết quan trọng mới!";
            default -> "Bạn có thông báo mới.";
        };
    }

    // SỬA: bài quan trọng dẫn thẳng tới post nếu có sourceId
    private String buildLink(String type, Integer sourceId, Integer senderId) {
        return switch (type) {
            case "friend_request", "friend_request_accepted" -> "/views/friends";
            case "post_comment", "post_reaction", "post_share" ->
                    (sourceId != null ? "/views/post/" + sourceId : "/views/user/" + senderId);
            case "new_important_post", "important_post" ->
                    (sourceId != null ? "/views/post/" + sourceId : "/views/important-posts");
            default -> "#";
        };
    }
}