package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import qdt.hcmute.vn.dqtbook_backend.model.PostMedia;

public interface PostMediaRepository extends JpaRepository<PostMedia, Integer> {
	@Modifying
	@Transactional
	@Query("delete from PostMedia pm where pm.post.id = :postId")
	void deleteByPostId(@Param("postId") Integer postId);
}


