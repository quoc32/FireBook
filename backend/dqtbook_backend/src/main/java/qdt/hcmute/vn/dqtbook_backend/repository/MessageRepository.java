package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qdt.hcmute.vn.dqtbook_backend.model.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    /**
     * Find all messages in a specific chat
     * 
     * @param chatId The chat's ID
     * @return List of messages
     */
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.createdAt DESC")
    List<Message> findByChatId(@Param("chatId") Integer chatId);

    /**
     * Find messages sent by a specific user
     * 
     * @param userId The user's ID
     * @return List of messages
     */
    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId")
    List<Message> findBySenderId(@Param("userId") Integer userId);
}
