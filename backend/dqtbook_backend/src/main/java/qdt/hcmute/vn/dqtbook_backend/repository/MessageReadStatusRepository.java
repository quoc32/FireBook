package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import qdt.hcmute.vn.dqtbook_backend.model.MessageReadStatus;
import qdt.hcmute.vn.dqtbook_backend.model.MessageReadStatusId;

import java.util.List;

public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, MessageReadStatusId> {

    /**
     * Find all read statuses for a specific message
     * 
     * @param messageId The message's ID
     * @return List of message read statuses
     */
    List<MessageReadStatus> findByMessageId(Long messageId);

    /**
     * Find all read statuses for a specific user
     * 
     * @param userId The user's ID
     * @return List of message read statuses
     */
    List<MessageReadStatus> findByUserId(Integer userId);

    /**
     * Find read status for a specific message and user
     * 
     * @param messageId The message's ID
     * @param userId    The user's ID
     * @return The message read status if exists
     */
    MessageReadStatus findByMessageIdAndUserId(Long messageId, Integer userId);

    /**
     * Count number of reads for a specific message
     * 
     * @param messageId The message's ID
     * @return Number of users who have read the message
     */
    long countByMessageId(Long messageId);

    /**
     * Find all read statuses for messages in a specific chat for a user
     * 
     * @param chatId The chat's ID
     * @param userId The user's ID
     * @return List of message read statuses
     */
    @Query("SELECT mrs FROM MessageReadStatus mrs JOIN mrs.message m WHERE m.chat.id = :chatId AND mrs.user.id = :userId")
    List<MessageReadStatus> findByChatIdAndUserId(@Param("chatId") Integer chatId, @Param("userId") Integer userId);

    /**
     * Delete all read statuses for messages in a specific chat
     * 
     * @param chatId The chat's ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM MessageReadStatus mrs WHERE mrs.message.chat.id = :chatId")
    void deleteByChatId(@Param("chatId") Integer chatId);
}