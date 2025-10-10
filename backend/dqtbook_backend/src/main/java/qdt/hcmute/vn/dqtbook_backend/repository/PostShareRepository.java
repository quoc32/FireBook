package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qdt.hcmute.vn.dqtbook_backend.model.PostShare;
import java.util.List;

public interface PostShareRepository extends JpaRepository<PostShare, Integer> {
    List<PostShare> findByPostId(Integer postId);
}
