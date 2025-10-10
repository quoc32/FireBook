package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import qdt.hcmute.vn.dqtbook_backend.model.Friend;
import qdt.hcmute.vn.dqtbook_backend.model.FriendId;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, FriendId> {
    @Query("SELECT f FROM Friend f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<Friend> findByUserId1OrUserId2(@Param("userId") Integer userId);

    @Query("SELECT f.user2.id FROM Friend f WHERE f.user1.id = :userId " +
       "UNION " +
       "SELECT f.user1.id FROM Friend f WHERE f.user2.id = :userId")
    List<Integer> findFriendIdsByUserId(Integer userId);
}


