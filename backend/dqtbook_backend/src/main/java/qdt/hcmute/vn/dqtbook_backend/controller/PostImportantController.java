package qdt.hcmute.vn.dqtbook_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import qdt.hcmute.vn.dqtbook_backend.model.Post;
import qdt.hcmute.vn.dqtbook_backend.model.User;
import qdt.hcmute.vn.dqtbook_backend.repository.PostRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.UserRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.FriendRepository;
import qdt.hcmute.vn.dqtbook_backend.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostImportantController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final NotificationService notificationService;

    public PostImportantController(PostRepository postRepository,
                                   UserRepository userRepository,
                                   FriendRepository friendRepository,
                                   NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.notificationService = notificationService;
    }

    // Gửi thông báo bài quan trọng đến toàn bộ bạn bè của tác giả
    @PostMapping("/{id}/broadcast-important")
    @Transactional
    public ResponseEntity<?> broadcastImportant(@PathVariable Integer id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        String type = (post.getPostType() != null) ? post.getPostType().toLowerCase() : "";
        if (!"important".equals(type)) {
            return ResponseEntity.badRequest().body("Post is not important");
        }

        User author = post.getAuthor();
        if (author == null || author.getId() == null) {
            return ResponseEntity.badRequest().body("Post author not found");
        }

        List<Integer> friendIds = friendRepository.findFriendIdsByUserId(author.getId());
        for (Integer fid : friendIds) {
            if (fid == null || fid.equals(author.getId())) continue;
            userRepository.findById(fid).ifPresent(recipient ->
                    notificationService.createAndSendNotification(
                        author, recipient, "new_important_post", post.getId()
                    )
            );
        }
        return ResponseEntity.ok().body("Broadcasted");
    }
}