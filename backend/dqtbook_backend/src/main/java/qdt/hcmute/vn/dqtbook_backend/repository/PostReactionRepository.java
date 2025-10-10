package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qdt.hcmute.vn.dqtbook_backend.model.PostReaction;
import java.util.List;
import java.util.Optional;

public interface PostReactionRepository extends JpaRepository<PostReaction, Integer> {
    List<PostReaction> findByPostId(Integer postId);
    Optional<PostReaction> findByPostIdAndUserId(Integer postId, Integer userId);
}
