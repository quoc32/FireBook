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
                .link(buildLink(sender, type, sourceId))
                .isRead(false)
                .createdAt(createdAt)
                .build();

        // 1) Theo user-destination (yêu cầu Principal.name = userId)
        messagingTemplate.convertAndSendToUser(String.valueOf(recipient.getId()),
                "/queue/notifications", dto);

        // 2) Fallback theo topic riêng userId (client có thể subscribe thêm)
        messagingTemplate.convertAndSend("/topic/notifications." + recipient.getId(), dto);
    }

    private String buildContent(User sender, String type) {
        return switch (type) {
            case "friend_request" -> sender.getFullName() + " đã gửi cho bạn một lời mời kết bạn.";
            case "friend_request_accepted" -> sender.getFullName() + " đã chấp nhận lời mời kết bạn của bạn.";
            case "post_comment" -> sender.getFullName() + " đã bình luận vào bài viết của bạn.";
            case "post_reaction" -> sender.getFullName() + " đã thả reaction bài viết của bạn.";
            case "new_important_post", "important_post" -> "Có bài viết quan trọng mới!";
            default -> "Bạn có thông báo mới.";
        };
    }

    private String buildLink(User sender, String type, Integer sourceId) {
        return switch (type) {
            case "friend_request", "friend_request_accepted" -> "/views/friends";
            case "post_comment", "post_reaction" -> (sourceId != null ? "/views/post/" + sourceId : "/views/user/" + sender.getId());
            case "new_important_post", "important_post" -> "/views/important-posts";
            default -> "#";
        };
    }
}