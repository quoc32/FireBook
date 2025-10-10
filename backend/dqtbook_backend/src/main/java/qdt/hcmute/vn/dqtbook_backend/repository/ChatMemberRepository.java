package qdt.hcmute.vn.dqtbook_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import qdt.hcmute.vn.dqtbook_backend.model.ChatMember;
import qdt.hcmute.vn.dqtbook_backend.model.ChatMemberId;

import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMember, ChatMemberId> {

    /**
     * Find all members of a specific chat
     * 
     * @param chatId The chat's ID
     * @return List of chat members
     */
    List<ChatMember> findByChatId(Integer chatId);

    /**
     * Find all chats that a user is a member of
     * 
     * @param userId The user's ID
     * @return List of chat memberships for the user
     */
    List<ChatMember> findByUserId(Integer userId);

    /**
     * Check if a user is a member of a specific chat
     * 
     * @param chatId The chat's ID
     * @param userId The user's ID
     * @return True if the user is a member of the chat
     */
    boolean existsByChatIdAndUserId(Integer chatId, Integer userId);

    /**
     * Delete all members from a chat
     * 
     * @param chatId The chat's ID
     */
    void deleteByChatId(Integer chatId);

    /**
     * Find a specific chat membership
     * 
     * @param chatId The chat's ID
     * @param userId The user's ID
     * @return The chat membership if exists
     */
    @Query("SELECT cm FROM ChatMember cm WHERE cm.chat.id = :chatId AND cm.user.id = :userId")
    ChatMember findByChatIdAndUserId(@Param("chatId") Integer chatId, @Param("userId") Integer userId);

    /**
     * Count number of members in a chat
     * 
     * @param chatId The chat's ID
     * @return Number of members
     */
    long countByChatId(Integer chatId);
}