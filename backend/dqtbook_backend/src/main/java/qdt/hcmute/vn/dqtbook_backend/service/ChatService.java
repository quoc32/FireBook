package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qdt.hcmute.vn.dqtbook_backend.dto.*;
import qdt.hcmute.vn.dqtbook_backend.model.*;
import qdt.hcmute.vn.dqtbook_backend.repository.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;

    public ChatService(ChatRepository chatRepository, ChatMemberRepository chatMemberRepository,
            UserRepository userRepository, MessageRepository messageRepository,
            MessageReadStatusRepository messageReadStatusRepository) {
        this.chatRepository = chatRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageReadStatusRepository = messageReadStatusRepository;
    }

    /**
     * Get all chats for a specific user
     * 
     * @param userId The user's ID
     * @return List of chat response DTOs
     */
    public List<ChatResponseDTO> getChatsByUserId(Integer userId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            return Collections.emptyList();
        }

        List<Chat> chats = chatRepository.findChatsByUserId(userId);
        return chats.stream()
                .map(chat -> convertToChatResponseDTO(chat, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific chat by ID
     * 
     * @param chatId The chat's ID
     * @param userId The requesting user's ID (for unread count)
     * @return Chat response DTO if found
     */
    public Optional<ChatResponseDTO> getChatById(Integer chatId, Integer userId) {
        // Require that the requester is a member of the chat
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return Optional.empty();
        }

        // If userId is provided, ensure membership; otherwise, deny access
        if (userId == null) {
            return Optional.empty();
        }

        ChatMember member = chatMemberRepository.findByChatIdAndUserId(chatId, userId);
        if (member == null) {
            return Optional.empty();
        }

        return Optional.of(convertToChatResponseDTO(chatOpt.get(), userId));
    }

    /**
     * Create a new chat
     * 
     * @param request The chat create request DTO
     * @return The created chat response DTO if successful
     */
    @Transactional
    public Optional<ChatResponseDTO> createChat(ChatCreateRequestDTO request) {
        // Validate creator exists
        if (request.getCreatorId() == null) {
            return Optional.empty();
        }
        Optional<User> creatorOpt = userRepository.findById(request.getCreatorId());
        if (creatorOpt.isEmpty()) {
            return Optional.empty();
        }

        // Validate that we have at least one other member (for direct chats)
        // or appropriate information for group chats
        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            return Optional.empty();
        }

        // For direct chats (one-to-one), check if it already exists
        if (Boolean.FALSE.equals(request.getIsGroup()) && request.getMemberIds().size() == 1) {
            Integer otherUserId = request.getMemberIds().get(0);
            // Check if users are the same
            if (request.getCreatorId().equals(otherUserId)) {
                return Optional.empty(); // Can't create chat with yourself
            }

            // Check if direct chat already exists between these users
            Chat existingChat = chatRepository.findDirectChatBetweenUsers(
                    request.getCreatorId(), otherUserId);

            if (existingChat != null) {
                // Chat already exists, return it
                return Optional.of(convertToChatResponseDTO(existingChat, request.getCreatorId()));
            }
        }

        // Create a new chat
        Chat chat = new Chat();
        chat.setChatName(request.getChatName());
        chat.setChatAvatarUrl(request.getChatAvatarUrl());
        chat.setIsGroup(request.getIsGroup());
        chat.setCreatedAt(Instant.now());
        Chat savedChat = chatRepository.save(chat);

        // Add members to chat
        List<Integer> allMemberIds = new ArrayList<>(request.getMemberIds());
        if (!allMemberIds.contains(request.getCreatorId())) {
            allMemberIds.add(request.getCreatorId());
        }

        for (Integer memberId : allMemberIds) {
            Optional<User> memberOpt = userRepository.findById(memberId);
            if (memberOpt.isEmpty()) {
                continue;
            }

            ChatMember chatMember = new ChatMember();
            ChatMemberId chatMemberId = new ChatMemberId();
            chatMemberId.setChatId(savedChat.getId());
            chatMemberId.setUserId(memberId);
            chatMember.setId(chatMemberId);
            chatMember.setChat(savedChat);
            chatMember.setUser(memberOpt.get());

            // Set the creator as admin, others as members
            if (memberId.equals(request.getCreatorId())) {
                chatMember.setRoleInChat("admin");
            } else {
                chatMember.setRoleInChat("member");
            }

            chatMemberRepository.save(chatMember);
        }

        return Optional.of(convertToChatResponseDTO(savedChat, request.getCreatorId()));
    }

    /**
     * Update an existing chat
     * 
     * @param chatId  The chat's ID
     * @param request The chat update request DTO
     * @param userId  The requesting user's ID
     * @return The updated chat response DTO if successful
     */
    @Transactional
    public Optional<ChatResponseDTO> updateChat(Integer chatId, ChatUpdateRequestDTO request, Integer userId) {
        // Check if chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return Optional.empty();
        }
        Chat chat = chatOpt.get();

        // Check if the user is a member of the chat with admin role
        ChatMember chatMember = chatMemberRepository.findByChatIdAndUserId(chatId, userId);
        if (chatMember == null || !"admin".equalsIgnoreCase(chatMember.getRoleInChat())) {
            return Optional.empty(); // Not authorized to update the chat
        }

        // Update chat properties
        if (request.getChatName() != null) {
            chat.setChatName(request.getChatName());
        }
        if (request.getChatAvatarUrl() != null) {
            chat.setChatAvatarUrl(request.getChatAvatarUrl());
        }

        Chat updatedChat = chatRepository.save(chat);
        return Optional.of(convertToChatResponseDTO(updatedChat, userId));
    }

    /**
     * Delete a chat
     * 
     * @param chatId The chat's ID
     * @param userId The requesting user's ID
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean deleteChat(Integer chatId, Integer userId) {
        // Check if chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return false;
        }

        // Check if the user is a member of the chat with admin role
        ChatMember chatMember = chatMemberRepository.findByChatIdAndUserId(chatId, userId);
        if (chatMember == null || !"admin".equalsIgnoreCase(chatMember.getRoleInChat())) {
            return false; // Not authorized to delete the chat
        }

        // Delete all message read statuses for this chat
        messageReadStatusRepository.deleteByChatId(chatId);

        // Delete all messages in the chat
        List<Message> messages = messageRepository.findByChatId(chatId);
        messageRepository.deleteAll(messages);

        // Delete all members from the chat
        chatMemberRepository.deleteByChatId(chatId);

        // Delete the chat
        chatRepository.deleteById(chatId);

        return true;
    }

    /**
     * Add a member to a chat
     * 
     * @param chatId      The chat's ID
     * @param request     The chat member request DTO
     * @param adminUserId The ID of the user adding the member (must be admin)
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean addChatMember(Integer chatId, ChatMemberRequestDTO request, Integer adminUserId) {
        // Check if chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return false;
        }
        Chat chat = chatOpt.get();

        // Only allow adding members to group chats
        if (!chat.getIsGroup()) {
            return false;
        }

        // Check if the admin user is a member of the chat with admin role
        ChatMember adminMember = chatMemberRepository.findByChatIdAndUserId(chatId, adminUserId);
        if (adminMember == null || !"admin".equals(adminMember.getRoleInChat())) {
            return false; // Not authorized to add members
        }

        // Check if the user to add exists
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();

        // Check if the user is already a member
        if (chatMemberRepository.existsByChatIdAndUserId(chatId, request.getUserId())) {
            return false; // User is already a member
        }

        // Add the user as a member
        ChatMember chatMember = new ChatMember();
        ChatMemberId chatMemberId = new ChatMemberId();
        chatMemberId.setChatId(chatId);
        chatMemberId.setUserId(request.getUserId());
        chatMember.setId(chatMemberId);
        chatMember.setChat(chat);
        chatMember.setUser(user);
        chatMember.setRoleInChat(request.getRoleInChat() != null ? request.getRoleInChat().toLowerCase() : "member");

        chatMemberRepository.save(chatMember);

        return true;
    }

    /**
     * Remove a member from a chat
     * 
     * @param chatId      The chat's ID
     * @param memberId    The ID of the member to remove
     * @param adminUserId The ID of the user removing the member
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean removeChatMember(Integer chatId, Integer memberId, Integer adminUserId) {
        // Check if chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return false;
        }
        Chat chat = chatOpt.get();

        // Only allow removing members from group chats
        if (!chat.getIsGroup()) {
            return false;
        }

        // Check if the admin user is authorized (either admin or the user themselves)
        if (!adminUserId.equals(memberId)) {
            ChatMember adminMember = chatMemberRepository.findByChatIdAndUserId(chatId, adminUserId);
            if (adminMember == null || !"admin".equalsIgnoreCase(adminMember.getRoleInChat())) {
                return false; // Not authorized to remove members
            }
        }

        // Check if the user to remove is a member
        ChatMember memberToRemove = chatMemberRepository.findByChatIdAndUserId(chatId, memberId);
        if (memberToRemove == null) {
            return false; // User is not a member
        }

        // Cannot remove the last admin
        if (memberToRemove.getRoleInChat() != null && "admin".equalsIgnoreCase(memberToRemove.getRoleInChat())) {
            // Count admins
            long adminCount = chatMemberRepository.findByChatId(chatId).stream()
                    .filter(m -> m.getRoleInChat() != null && "admin".equalsIgnoreCase(m.getRoleInChat()))
                    .count();
            if (adminCount <= 1) {
                return false; // Cannot remove the last admin
            }
        }

        // Delete read statuses for this user in the chat
        List<MessageReadStatus> readStatuses = messageReadStatusRepository.findByChatIdAndUserId(chatId, memberId);
        messageReadStatusRepository.deleteAll(readStatuses);

        // Remove the member
        ChatMemberId chatMemberId = new ChatMemberId();
        chatMemberId.setChatId(chatId);
        chatMemberId.setUserId(memberId);
        chatMemberRepository.deleteById(chatMemberId);

        return true;
    }

    /**
     * Update a chat member's role
     * 
     * @param chatId      The chat's ID
     * @param request     The chat member request DTO with updated role
     * @param adminUserId The ID of the user updating the role (must be admin)
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean updateChatMemberRole(Integer chatId, ChatMemberRequestDTO request, Integer adminUserId) {
        // Check if chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return false;
        }

        // Check if the admin user is a member of the chat with admin role
        ChatMember adminMember = chatMemberRepository.findByChatIdAndUserId(chatId, adminUserId);
        if (adminMember == null || !"admin".equals(adminMember.getRoleInChat())) {
            return false; // Not authorized to update roles
        }

        // Check if the user to update is a member
        ChatMember memberToUpdate = chatMemberRepository.findByChatIdAndUserId(chatId, request.getUserId());
        if (memberToUpdate == null) {
            return false; // User is not a member
        }

        // Update the member's role
        memberToUpdate.setRoleInChat(request.getRoleInChat() != null ? request.getRoleInChat().toLowerCase() : memberToUpdate.getRoleInChat());
        chatMemberRepository.save(memberToUpdate);

        return true;
    }

    /**
     * Convert a Chat entity to a ChatResponseDTO
     * 
     * @param chat   The Chat entity
     * @param userId The user ID to calculate unread count for
     * @return The ChatResponseDTO
     */
    private ChatResponseDTO convertToChatResponseDTO(Chat chat, Integer userId) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setId(chat.getId());
        dto.setChatName(chat.getChatName());
        dto.setChatAvatarUrl(chat.getChatAvatarUrl());
        dto.setIsGroup(chat.getIsGroup());
        dto.setCreatedAt(chat.getCreatedAt());

        // Get all members of the chat
        List<ChatMember> members = chatMemberRepository.findByChatId(chat.getId());
        dto.setMembers(members.stream()
                .map(this::convertToChatMemberDTO)
                .collect(Collectors.toList()));

        // Get the last message in the chat
        List<Message> messages = messageRepository.findByChatId(chat.getId());
        if (!messages.isEmpty()) {
            // Sort messages by created time desc
            messages.sort(Comparator.comparing(Message::getCreatedAt).reversed());
            Message lastMessage = messages.get(0);
            dto.setLastMessage(convertToMessageSummaryDTO(lastMessage));
        }

        // Calculate unread message count for this user
        if (userId != null) {
            // TODO: Optimize this with a better query
            long unreadCount = messages.stream()
                    .filter(message -> {
                        MessageReadStatus readStatus = messageReadStatusRepository
                                .findByMessageIdAndUserId(message.getId(), userId);
                        return readStatus == null;
                    })
                    .count();
            dto.setUnreadCount((int) unreadCount);
        }

        return dto;
    }

    /**
     * Convert a ChatMember entity to a ChatMemberDTO
     * 
     * @param chatMember The ChatMember entity
     * @return The ChatMemberDTO
     */
    private ChatMemberDTO convertToChatMemberDTO(ChatMember chatMember) {
        ChatMemberDTO dto = new ChatMemberDTO();
        dto.setUserId(chatMember.getUser().getId());
        dto.setUserFullName(chatMember.getUser().getFullName());
        dto.setUserAvatarUrl(chatMember.getUser().getAvatarUrl());
        dto.setRoleInChat(chatMember.getRoleInChat());
        return dto;
    }

    /**
     * Convert a Message entity to a MessageSummaryDTO
     * 
     * @param message The Message entity
     * @return The MessageSummaryDTO
     */
    private MessageSummaryDTO convertToMessageSummaryDTO(Message message) {
        MessageSummaryDTO dto = new MessageSummaryDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderFullName(message.getSender().getFullName());
        dto.setSenderAvatarUrl(message.getSender().getAvatarUrl());
        dto.setContent(message.getContent());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}