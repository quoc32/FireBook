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

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createAndSendNotification(User sender, User recipient, String type, Integer sourceId) {
        if (sender.getId().equals(recipient.getId())) {
            return; // Không tự gửi thông báo cho chính mình
        }

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setSourceId(sourceId);
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);

        NotificationDTO dto = NotificationDTO.builder()
                .notificationId(savedNotification.getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .senderAvatarUrl(sender.getAvatarUrl())
                .type(type)
                .content(buildContent(sender, type))
                .link(buildLink(sender, type))
                .isRead(false)
                .createdAt(LocalDateTime.ofInstant(savedNotification.getCreatedAt(), ZoneId.systemDefault()))
                .build();

        // Gửi thông báo real-time đến kênh riêng của người nhận
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(), // Định danh người nhận bằng email (hoặc username)
                "/queue/notifications",
                dto);
    }

    private String buildContent(User sender, String type) {
        if ("friend_request_accepted".equals(type)) {
            return sender.getFullName() + " đã chấp nhận lời mời kết bạn của bạn.";
        }
        // Thêm các case khác sau này
        // case "new_comment": return sender.getFullName() + " đã bình luận...";
        return "Bạn có thông báo mới.";
    }

    private String buildLink(User sender, String type) {
        if ("friend_request_accepted".equals(type)) {
            return "/views/user/" + sender.getId();
        }
        return "#";
    }
}