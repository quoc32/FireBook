package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qdt.hcmute.vn.dqtbook_backend.dto.MessageRequestDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.MessageResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.websocket.ChatMessageDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.websocket.ChatMessageResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.websocket.ReadStatusDTO;
import qdt.hcmute.vn.dqtbook_backend.model.*;
import qdt.hcmute.vn.dqtbook_backend.model.MessageReadStatusId;
import qdt.hcmute.vn.dqtbook_backend.repository.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;

    public MessageService(MessageRepository messageRepository, ChatRepository chatRepository,
            UserRepository userRepository, ChatMemberRepository chatMemberRepository,
            MessageReadStatusRepository messageReadStatusRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatMemberRepository = chatMemberRepository;
        this.messageReadStatusRepository = messageReadStatusRepository;
    }

    /**
     * Get messages for a specific chat
     * 
     * @param chatId The chat's ID
     * @param userId The requesting user's ID
     * @return List of message response DTOs
     */
    public List<MessageResponseDTO> getMessagesForChat(Integer chatId, Integer userId) {
        // Check if chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return Collections.emptyList();
        }

        // Check if the user is a member of the chat
        if (!chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {
            return Collections.emptyList(); // User is not a member of the chat
        }

        // Get messages for the chat
        List<Message> messages = messageRepository.findByChatId(chatId);

        return messages.stream()
                .map(this::convertToMessageResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific message by ID
     * 
     * @param messageId The message's ID
     * @param userId    The requesting user's ID
     * @return Message response DTO if found
     */
    public Optional<MessageResponseDTO> getMessageById(Long messageId, Integer userId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return Optional.empty();
        }
        Message message = messageOpt.get();

        // Check if the user is a member of the chat
        if (!chatMemberRepository.existsByChatIdAndUserId(message.getChat().getId(), userId)) {
            return Optional.empty(); // User is not a member of the chat
        }

        return Optional.of(convertToMessageResponseDTO(message));
    }

    /**
     * Send a new message
     * 
     * @param request The message request DTO
     * @return The created message response DTO if successful
     */
    @Transactional
    public Optional<MessageResponseDTO> sendMessage(MessageRequestDTO request) {
        // Validate chat exists
        Optional<Chat> chatOpt = chatRepository.findById(request.getChatId());
        if (chatOpt.isEmpty()) {
            return Optional.empty();
        }
        Chat chat = chatOpt.get();

        // Validate sender exists
        Optional<User> senderOpt = userRepository.findById(request.getSenderId());
        if (senderOpt.isEmpty()) {
            return Optional.empty();
        }
        User sender = senderOpt.get();

        // Check if the sender is a member of the chat
        if (!chatMemberRepository.existsByChatIdAndUserId(chat.getId(), sender.getId())) {
            return Optional.empty(); // Sender is not a member of the chat
        }

        // Create and save the message
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMediaUrl(request.getMediaUrl());
        message.setCreatedAt(Instant.now());

        Message savedMessage = messageRepository.save(message);

        // Mark the message as read by the sender
        MessageReadStatus readStatus = new MessageReadStatus();
        MessageReadStatusId readStatusId = new MessageReadStatusId();
        readStatusId.setMessageId(savedMessage.getId());
        readStatusId.setUserId(sender.getId());
        readStatus.setId(readStatusId);
        readStatus.setMessage(savedMessage);
        readStatus.setUser(sender);
        readStatus.setReadAt(Instant.now());

        messageReadStatusRepository.save(readStatus);

        return Optional.of(convertToMessageResponseDTO(savedMessage));
    }

    /**
     * Delete a message
     * 
     * @param messageId The message's ID
     * @param userId    The ID of the user deleting the message
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean deleteMessage(Long messageId, Integer userId) {
        // Check if message exists
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return false;
        }
        Message message = messageOpt.get();

        // Check if the user is authorized to delete the message
        // (either the sender or an admin of the chat)
        if (!userId.equals(message.getSender().getId())) {
            ChatMember chatMember = chatMemberRepository.findByChatIdAndUserId(
                    message.getChat().getId(), userId);
            if (chatMember == null || !"admin".equals(chatMember.getRoleInChat())) {
                return false; // Not authorized to delete the message
            }
        }

        // Delete all read statuses for the message
        List<MessageReadStatus> readStatuses = messageReadStatusRepository.findByMessageId(messageId);
        messageReadStatusRepository.deleteAll(readStatuses);

        // Delete the message
        messageRepository.delete(message);

        return true;
    }

    /**
     * Mark a message as read by a user
     * 
     * @param messageId The message's ID
     * @param userId    The user's ID
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean markMessageAsRead(Long messageId, Integer userId) {
        // Check if message exists
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        if (messageOpt.isEmpty()) {
            return false;
        }
        Message message = messageOpt.get();

        // Check if the user is a member of the chat
        if (!chatMemberRepository.existsByChatIdAndUserId(message.getChat().getId(), userId)) {
            return false; // User is not a member of the chat
        }

        // Check if the message is already read by the user
        MessageReadStatus existingReadStatus = messageReadStatusRepository.findByMessageIdAndUserId(messageId, userId);
        if (existingReadStatus != null) {
            return true; // Message is already marked as read
        }

        // Create a new read status
        MessageReadStatus readStatus = new MessageReadStatus();
        MessageReadStatusId readStatusId = new MessageReadStatusId();
        readStatusId.setMessageId(messageId);
        readStatusId.setUserId(userId);
        readStatus.setId(readStatusId);
        readStatus.setMessage(message);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        readStatus.setUser(userOpt.get());

        readStatus.setReadAt(Instant.now());

        messageReadStatusRepository.save(readStatus);

        return true;
    }

    /**
     * Mark all messages in a chat as read by a user
     * 
     * @param chatId The chat's ID
     * @param userId The user's ID
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean markAllMessagesAsRead(Integer chatId, Integer userId) {
        // Check if chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            return false;
        }

        // Check if the user is a member of the chat
        if (!chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {
            return false; // User is not a member of the chat
        }

        // Get all unread messages in the chat
        List<Message> messages = messageRepository.findByChatId(chatId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();

        Instant now = Instant.now();

        for (Message message : messages) {
            // Check if the message is already read by the user
            MessageReadStatus existingReadStatus = messageReadStatusRepository.findByMessageIdAndUserId(message.getId(),
                    userId);
            if (existingReadStatus != null) {
                continue; // Message is already marked as read
            }

            // Create a new read status
            MessageReadStatus readStatus = new MessageReadStatus();
            MessageReadStatusId readStatusId = new MessageReadStatusId();
            readStatusId.setMessageId(message.getId());
            readStatusId.setUserId(userId);
            readStatus.setId(readStatusId);
            readStatus.setMessage(message);
            readStatus.setUser(user);
            readStatus.setReadAt(now);

            messageReadStatusRepository.save(readStatus);
        }

        return true;
    }

    /**
     * Convert a Message entity to a MessageResponseDTO
     * 
     * @param message The Message entity
     * @return The MessageResponseDTO
     */
    private MessageResponseDTO convertToMessageResponseDTO(Message message) {
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(message.getId());
        dto.setChatId(message.getChat().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderFullName(message.getSender().getFullName());
        dto.setSenderAvatarUrl(message.getSender().getAvatarUrl());
        dto.setContent(message.getContent());
        dto.setMediaUrl(message.getMediaUrl());
        dto.setCreatedAt(message.getCreatedAt());

        // Get read statuses for this message
        List<MessageReadStatus> readStatuses = messageReadStatusRepository.findByMessageId(message.getId());
        dto.setReadBy(readStatuses.stream()
                .map(this::convertToMessageReadDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Convert a MessageReadStatus entity to a MessageReadDTO
     * 
     * @param readStatus The MessageReadStatus entity
     * @return The MessageReadDTO
     */
    private MessageResponseDTO.MessageReadDTO convertToMessageReadDTO(MessageReadStatus readStatus) {
        MessageResponseDTO.MessageReadDTO dto = new MessageResponseDTO.MessageReadDTO();
        dto.setUserId(readStatus.getUser().getId());
        dto.setUserFullName(readStatus.getUser().getFullName());
        dto.setReadAt(readStatus.getReadAt());
        return dto;
    }

    /**
     * Save a message from WebSocket and prepare response
     * 
     * @param chatMessageDTO The WebSocket chat message DTO
     * @return ChatMessageResponseDTO if successful, null otherwise
     */
    @Transactional
    public ChatMessageResponseDTO saveAndBroadcastMessage(ChatMessageDTO chatMessageDTO) {
        // Validate chat exists
        Optional<Chat> chatOpt = chatRepository.findById(chatMessageDTO.getChatId().intValue());
        if (chatOpt.isEmpty()) {
            return null;
        }
        Chat chat = chatOpt.get();

        // Validate sender exists
        Optional<User> senderOpt = userRepository.findById(chatMessageDTO.getSenderId().intValue());
        if (senderOpt.isEmpty()) {
            return null;
        }
        User sender = senderOpt.get();

        // Check if the sender is a member of the chat
        if (!chatMemberRepository.existsByChatIdAndUserId(chat.getId(), sender.getId())) {
            return null; // Sender is not a member of the chat
        }

        // Create and save the message
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(chatMessageDTO.getContent());
        message.setMediaUrl(null); // WebSocket messages currently don't support media
        message.setCreatedAt(Instant.now());

        Message savedMessage = messageRepository.save(message);

        // Mark the message as read by the sender
        MessageReadStatus readStatus = new MessageReadStatus();
        MessageReadStatusId readStatusId = new MessageReadStatusId();
        readStatusId.setMessageId(savedMessage.getId());
        readStatusId.setUserId(sender.getId());
        readStatus.setId(readStatusId);
        readStatus.setMessage(savedMessage);
        readStatus.setUser(sender);
        readStatus.setReadAt(Instant.now());

        messageReadStatusRepository.save(readStatus);

        // Convert to WebSocket response DTO
        return convertToWebSocketMessageDTO(savedMessage);
    }

    /**
     * Mark message as read through WebSocket
     * 
     * @param readStatusDTO The read status update DTO
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean markMessageAsRead(ReadStatusDTO readStatusDTO) {
        return markMessageAsRead(readStatusDTO.getMessageId(), readStatusDTO.getUserId().intValue());
    }

    /**
     * Convert a Message entity to a WebSocket ChatMessageResponseDTO
     * 
     * @param message The Message entity
     * @return The ChatMessageResponseDTO
     */
    private ChatMessageResponseDTO convertToWebSocketMessageDTO(Message message) {
        LocalDateTime timestamp = LocalDateTime.ofInstant(message.getCreatedAt(), ZoneId.systemDefault());

        return new ChatMessageResponseDTO(
                message.getId(),
                message.getContent(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getSender().getFullName(),
                message.getSender().getAvatarUrl(),
                message.getMediaUrl() != null ? "MEDIA" : "TEXT",
                timestamp);
    }
}