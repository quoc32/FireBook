package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qdt.hcmute.vn.dqtbook_backend.model.Chat;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {

    /**
     * Find all chats that a user is a member of
     * 
     * @param userId The user's ID
     * @return List of chats
     */
    @Query("SELECT c FROM Chat c JOIN ChatMember cm ON c.id = cm.chat.id WHERE cm.user.id = :userId")
    List<Chat> findChatsByUserId(@Param("userId") Integer userId);

    /**
     * Find direct chat between two users
     * 
     * @param userId1 First user's ID
     * @param userId2 Second user's ID
     * @return Direct chat between the two users, if exists
     */
    @Query("SELECT c FROM Chat c " +
            "JOIN ChatMember cm1 ON c.id = cm1.chat.id " +
            "JOIN ChatMember cm2 ON c.id = cm2.chat.id " +
            "WHERE c.isGroup = false " +
            "AND cm1.user.id = :userId1 AND cm2.user.id = :userId2")
    Chat findDirectChatBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
}