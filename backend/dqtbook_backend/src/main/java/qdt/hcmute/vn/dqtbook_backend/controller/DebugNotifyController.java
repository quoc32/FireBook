package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.web.bind.annotation.*;
import qdt.hcmute.vn.dqtbook_backend.service.NotificationService;
import qdt.hcmute.vn.dqtbook_backend.repository.UserRepository;
import qdt.hcmute.vn.dqtbook_backend.model.User;

@RestController
@RequestMapping("/api/debug")
public class DebugNotifyController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public DebugNotifyController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    /**
     * Test nhanh pipeline thông báo:
     * GET /api/debug/notify?from=1&to=5&type=post_comment&sourceId=123
     */
    @GetMapping("/notify")
    public String notifyOnce(@RequestParam Integer from,
                             @RequestParam Integer to,
                             @RequestParam(defaultValue = "post_comment") String type,
                             @RequestParam(required = false) Integer sourceId) {
        User sender = userRepository.findById(from).orElseThrow();
        User recipient = userRepository.findById(to).orElseThrow();
        notificationService.createAndSendNotification(sender, recipient, type, sourceId);
        return "sent";
    }
}