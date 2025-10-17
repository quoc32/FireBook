package qdt.hcmute.vn.dqtbook_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import qdt.hcmute.vn.dqtbook_backend.dto.FriendRequestDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.FriendResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.FriendActionDTO;
import qdt.hcmute.vn.dqtbook_backend.dto.UserResponseDTO;
import qdt.hcmute.vn.dqtbook_backend.model.Friend;
import qdt.hcmute.vn.dqtbook_backend.model.FriendId;
import qdt.hcmute.vn.dqtbook_backend.model.FriendStatus;
import qdt.hcmute.vn.dqtbook_backend.repository.FriendRepository;
import qdt.hcmute.vn.dqtbook_backend.repository.UserRepository;
import qdt.hcmute.vn.dqtbook_backend.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    private HttpSession session;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository, UserService userService, NotificationService notificationService) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public Optional<List<FriendResponseDTO>> getFriendsByUserId(Integer userId) {
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        List<Friend> friends = friendRepository.findByUserId1OrUserId2(userId);
        List<FriendResponseDTO> result = friends.stream()
                .map(friend -> convertToResponseDTO(friend, userId))
                .collect(Collectors.toList());
        return Optional.of(result);
    }

    @Transactional
    public Optional<FriendResponseDTO> sendFriendRequest(FriendRequestDTO dto) {
        Integer senderId = dto.getSenderId();
        Integer receiverId = dto.getReceiverId();

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("sender_id and receiver_id cannot be the same");
        }
        if (!userRepository.existsById(senderId) || !userRepository.existsById(receiverId)) {
            throw new IllegalArgumentException("One or both users do not exist");
        }

        FriendId checkId1 = new FriendId();
        checkId1.setUserId1(senderId);
        checkId1.setUserId2(receiverId);
        FriendId checkId2 = new FriendId();
        checkId2.setUserId1(receiverId);
        checkId2.setUserId2(senderId);
        if (friendRepository.existsById(checkId1) || friendRepository.existsById(checkId2)) {
            throw new IllegalArgumentException("Friend request or relationship already exists");
        }

        Friend friend = new Friend();
        FriendId friendId = new FriendId();
        friendId.setUserId1(senderId);
        friendId.setUserId2(receiverId);
        friend.setId(friendId);
        friend.setUser1(userRepository.findById(senderId).get());
        friend.setUser2(userRepository.findById(receiverId).get());
        friend.setStatus(FriendStatus.pending);
        friend.setCreatedAt(Instant.now());
        friend.setUpdatedAt(Instant.now());

        Friend savedFriend = friendRepository.save(friend);

        try {
            User sender = userRepository.findById(senderId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);
            if (sender != null && receiver != null) {
                notificationService.createAndSendNotification(
                        sender,
                        receiver,
                        "friend_request",
                        sender.getId()
                );
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo kết bạn: " + e.getMessage());
        }
        return Optional.of(convertToResponseDTO(savedFriend, senderId));
    }

    @Transactional
    public Optional<FriendResponseDTO> acceptFriendRequest(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();     // người gửi lời mời ban đầu
        Integer receiverId = dto.getReceiverId(); // người chấp nhận

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }

        // Người chấp nhận phải khớp session
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(receiverId)) {
            throw new IllegalArgumentException("receiver_id does not match the logged-in user");
        }

        FriendId friendId = new FriendId();
        friendId.setUserId1(senderId);
        friendId.setUserId2(receiverId);

        Optional<Friend> friendOpt = friendRepository.findById(friendId);
        if (friendOpt.isEmpty() || friendOpt.get().getStatus() != FriendStatus.pending) {
            throw new IllegalArgumentException("No pending friend request found between the users");
        }

        Friend friend = friendOpt.get();
        friend.setStatus(FriendStatus.accepted);
        friend.setUpdatedAt(Instant.now());

        Friend savedFriend = friendRepository.save(friend);

        // Gửi thông báo cho người gửi yêu cầu ban đầu
        try {
            User originalRequester = userRepository.findById(senderId).orElse(null);
            User acceptor = userRepository.findById(receiverId).orElse(null);
            if (originalRequester != null && acceptor != null) {
                notificationService.createAndSendNotification(
                        acceptor,
                        originalRequester,
                        "friend_request_accepted",
                        null
                );
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo chấp nhận kết bạn: " + e.getMessage());
        }

        return Optional.of(convertToResponseDTO(savedFriend, receiverId));
    }

    @Transactional
    public boolean refuseFriendRequest(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();     // người gửi yêu cầu ban đầu
        Integer receiverId = dto.getReceiverId(); // người từ chối

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }
        // NGƯỜI TỪ CHỐI PHẢI LÀ RECEIVER (đang đăng nhập)
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(receiverId)) {
            throw new IllegalArgumentException("receiver_id does not match the logged-in user");
        }

        // Yêu cầu pending đúng chiều: user1 = sender, user2 = receiver
        FriendId fid = new FriendId();
        fid.setUserId1(senderId);
        fid.setUserId2(receiverId);

        Optional<Friend> opt = friendRepository.findById(fid);
        if (opt.isPresent()) {
            Friend f = opt.get();
            if (f.getStatus() != FriendStatus.pending) {
                throw new IllegalStateException("Cannot refuse a non-pending relationship");
            }
            // đảm bảo đúng vai trò: user2 là người từ chối
            if (!f.getId().getUserId2().equals(sessionUserId)) {
                throw new IllegalArgumentException("Only the receiver can refuse this request");
            }
            friendRepository.deleteById(fid);
            return true;
        }

        // Thử chiều ngược (trường hợp dữ liệu cũ khác chiều)
        FriendId rev = new FriendId();
        rev.setUserId1(receiverId);
        rev.setUserId2(senderId);

        Optional<Friend> opt2 = friendRepository.findById(rev);
        if (opt2.isPresent()) {
            Friend f = opt2.get();
            if (f.getStatus() != FriendStatus.pending) {
                throw new IllegalStateException("Cannot refuse a non-pending relationship");
            }
            if (!f.getId().getUserId2().equals(sessionUserId)) {
                throw new IllegalArgumentException("Only the receiver can refuse this request");
            }
            friendRepository.deleteById(rev);
            return true;
        }

        throw new IllegalArgumentException("No pending friend request found between the users");
    }

    @Transactional
    public Optional<FriendResponseDTO> blockFriend(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();
        Integer receiverId = dto.getReceiverId();

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }

        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        FriendId friendId1 = new FriendId();
        friendId1.setUserId1(senderId);
        friendId1.setUserId2(receiverId);
        FriendId friendId2 = new FriendId();
        friendId2.setUserId1(receiverId);
        friendId2.setUserId2(senderId);

        Optional<Friend> friendOpt = friendRepository.findById(friendId1);
        if (friendOpt.isEmpty()) {
            friendOpt = friendRepository.findById(friendId2);
        }

        if (friendOpt.isEmpty()) {
            throw new IllegalArgumentException("No friend relationship found between the users");
        } else if (friendOpt.get().getStatus() == FriendStatus.blocked) {
            throw new IllegalArgumentException("Users are already blocked");
        }

        Friend friend = friendOpt.get();
        friend.setStatus(FriendStatus.blocked);
        friend.setUpdatedAt(Instant.now());

        Friend savedFriend = friendRepository.save(friend);
        return Optional.of(convertToResponseDTO(savedFriend, senderId));
    }

    @Transactional
    public boolean unfriend(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();
        Integer receiverId = dto.getReceiverId();

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        FriendId friendId1 = new FriendId();
        friendId1.setUserId1(senderId);
        friendId1.setUserId2(receiverId);
        FriendId friendId2 = new FriendId();
        friendId2.setUserId1(receiverId);
        friendId2.setUserId2(senderId);

        if (friendRepository.existsById(friendId1)) {
            friendRepository.deleteById(friendId1);
            return true;
        } else if (friendRepository.existsById(friendId2)) {
            friendRepository.deleteById(friendId2);
            return true;
        } else {
            throw new IllegalArgumentException("No friend relationship found between the users");
        }
    }

    // Người gửi hủy lời mời (chỉ khi pending)
    @Transactional
    public boolean cancel_request(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();     // người gửi yêu cầu ban đầu (đang muốn hủy)
        Integer receiverId = dto.getReceiverId();

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        // Bản ghi đúng chiều: user1 = sender, user2 = receiver
        FriendId fid = new FriendId();
        fid.setUserId1(senderId);
        fid.setUserId2(receiverId);
        Optional<Friend> opt = friendRepository.findById(fid);
        if (opt.isPresent()) {
            Friend f = opt.get();
            if (f.getStatus() != FriendStatus.pending) {
                throw new IllegalStateException("Only pending requests can be cancelled");
            }
            if (!f.getId().getUserId1().equals(sessionUserId)) {
                throw new IllegalArgumentException("Only the original sender can cancel this request");
            }
            friendRepository.deleteById(fid);
            return true;
        }

        // Thử chiều ngược (trường hợp dữ liệu cũ)
        FriendId rev = new FriendId();
        rev.setUserId1(receiverId);
        rev.setUserId2(senderId);
        Optional<Friend> opt2 = friendRepository.findById(rev);
        if (opt2.isPresent()) {
            Friend f = opt2.get();
            if (f.getStatus() != FriendStatus.pending) {
                throw new IllegalStateException("Only pending requests can be cancelled");
            }
            if (!f.getId().getUserId1().equals(sessionUserId)) {
                throw new IllegalArgumentException("Only the original sender can cancel this request");
            }
            friendRepository.deleteById(rev);
            return true;
        }

        throw new IllegalArgumentException("No pending friend request found between the users");
    }

    @Transactional(readOnly = true)
    public Optional<List<UserResponseDTO>> getFriendSuggestions(Integer userId) {
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            throw new IllegalArgumentException("user_id does not match the logged-in user");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        List<Integer> friendIds = friendRepository.findFriendIdsByUserId(userId);
        List<UserResponseDTO> suggestions = userRepository.findSuggestions(userId, friendIds, PageRequest.of(0, 5))
                .stream()
                .map(user -> {
                    UserResponseDTO dto = new UserResponseDTO();
                    dto.setId(user.getId());
                    dto.setFullName(user.getFullName());
                    dto.setEmail(user.getEmail());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        return Optional.of(suggestions);
    }

    private FriendResponseDTO convertToResponseDTO(Friend friend, Integer currentUserId) {
        FriendResponseDTO dto = new FriendResponseDTO();
        dto.setSenderId(friend.getId().getUserId1());
        dto.setReceiverId(friend.getId().getUserId2());
        dto.setStatus(friend.getStatus().name());
        dto.setCreatedAt(friend.getCreatedAt());
        dto.setUpdatedAt(friend.getUpdatedAt());

        Integer friendId = friend.getId().getUserId1().equals(currentUserId)
                ? friend.getId().getUserId2()
                : friend.getId().getUserId1();

        Optional<UserResponseDTO> friendInfo = userService.getUserById(friendId);
        friendInfo.ifPresent(dto::setFriendInfo);

        return dto;
    }
}