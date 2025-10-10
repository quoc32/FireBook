package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import qdt.hcmute.vn.dqtbook_backend.model.Post;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
  List<Post> findByAuthorId(Integer authorId);
}
