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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    @Autowired
    private HttpSession session;

    public FriendService(FriendRepository friendRepository, UserRepository userRepository, UserService userService) {
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Lấy danh sách bạn bè của người dùng (theo ID) sau khi xác thực người dùng trong session.
     *
     * Logic:
     * - Kiểm tra userId trong session phải trùng với tham số truyền vào (bảo vệ không truy xuất chéo).
     * - Kiểm tra sự tồn tại của user.
     * - Trả về danh sách bạn bè dưới dạng DTO (có thể rỗng) bọc trong Optional (luôn present).
     *
     * @param userId ID của người dùng cần lấy danh sách bạn bè (phải trùng với user đang đăng nhập).
     * @return Optional luôn chứa List<FriendResponseDTO>; danh sách có thể rỗng nếu chưa có bạn bè.
     * @throws IllegalArgumentException nếu:
     *         - userId không trùng với user trong session
     *         - hoặc user không tồn tại
     */
    public Optional<List<FriendResponseDTO>> getFriendsByUserId(Integer userId) {
        // Session user check
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        
        List<Friend> friends = friendRepository.findByUserId1OrUserId2(userId);
        List<FriendResponseDTO> result = friends.stream()
                .map(friend -> convertToResponseDTO(friend, userId))
                .collect(Collectors.toList());
        return Optional.of(result);
    }

    /**
     * Gửi yêu cầu kết bạn (sender -> receiver). Kiểm tra hợp lệ, xác thực user phiên,
     * tồn tại user, chưa có quan hệ hai chiều; nếu OK tạo yêu cầu trạng thái pending.
     * @param dto senderId, receiverId
     * @return Optional FriendResponseDTO
     * @throws IllegalArgumentException nếu dữ liệu sai hoặc quan hệ đã tồn tại
     */
    @Transactional
    public Optional<FriendResponseDTO> sendFriendRequest(FriendRequestDTO dto) {
        Integer senderId = dto.getSenderId();
        Integer receiverId = dto.getReceiverId();

        // Basic validation
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("sender_id and receiver_id cannot be the same");
        }

        // Check if users exist
        if (!userRepository.existsById(senderId) || !userRepository.existsById(receiverId)) {
            throw new IllegalArgumentException("One or both users do not exist");
        }

        // Check if relationship already exists (both directions)
        FriendId checkId1 = new FriendId();
        checkId1.setUserId1(senderId);
        checkId1.setUserId2(receiverId);
        
        FriendId checkId2 = new FriendId();
        checkId2.setUserId1(receiverId);
        checkId2.setUserId2(senderId);
        
        if (friendRepository.existsById(checkId1) || friendRepository.existsById(checkId2)) {
            throw new IllegalArgumentException("Friend request or relationship already exists");
        }

        // Create friend request: user_id_1 = sender, user_id_2 = receiver
        Friend friend = new Friend();
        FriendId friendId = new FriendId();
        friendId.setUserId1(senderId);  // sender
        friendId.setUserId2(receiverId); // receiver
        friend.setId(friendId);
        friend.setUser1(userRepository.findById(senderId).get());
        friend.setUser2(userRepository.findById(receiverId).get());
        friend.setStatus(FriendStatus.pending);
        friend.setCreatedAt(Instant.now());
        friend.setUpdatedAt(Instant.now());

        Friend savedFriend = friendRepository.save(friend);
        return Optional.of(convertToResponseDTO(savedFriend, senderId));
    }

    /**
     * Chấp nhận yêu cầu kết bạn. receiver (user đang đăng nhập) chấp nhận request của sender.
     * Kiểm tra: sender/receiver hợp lệ, phiên đăng nhập đúng receiver, tồn tại bản ghi pending
     * ở một trong hai chiều. Nếu hợp lệ -> cập nhật trạng thái accepted.
     * @param dto senderId (người gửi yêu cầu ban đầu), receiverId (người chấp nhận)
     * @return Optional FriendResponseDTO
     * @throws IllegalArgumentException nếu không tìm thấy pending request hoặc sai user phiên
     */
    @Transactional
    public Optional<FriendResponseDTO> acceptFriendRequest(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();    
        Integer receiverId = dto.getReceiverId(); 

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }

        // Session user check (optional)
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        // Find the friend relationship // ! (NO check both directions)
        FriendId friendId1 = new FriendId();
        friendId1.setUserId1(receiverId);    // user_id_1 = Người gửi lời mời ban đầu
        friendId1.setUserId2(senderId);  // user_id_2 = Người nhận lời mời (đang chấp nhận)

        Optional<Friend> friendOpt = friendRepository.findById(friendId1);
        
        if (friendOpt.isEmpty() || friendOpt.get().getStatus() != FriendStatus.pending) {
            throw new IllegalArgumentException("No pending friend request found between the users");
        }

        Friend friend = friendOpt.get();
        friend.setStatus(FriendStatus.accepted);
        friend.setUpdatedAt(Instant.now());

        Friend savedFriend = friendRepository.save(friend);
        return Optional.of(convertToResponseDTO(savedFriend, receiverId));
    }

    /**
     * Từ chối (cập nhật trạng thái) một lời mời kết bạn đang chờ giữa hai người dùng.
     * Logic giống hủy kết bạn (unfriend) vì về bản chất là xóa quan hệ pending.
     */
    @Transactional
    public boolean refuseFriendRequest(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();    // Người gửi lời mời ban đầu
        Integer receiverId = dto.getReceiverId(); // Người nhận lời mời (đang unfriend)

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }
        // Session user check
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        // Find the friend relationship (check both directions)
        FriendId friendId1 = new FriendId();
        friendId1.setUserId1(senderId);    // user_id_1 = sender
        friendId1.setUserId2(receiverId);  // user_id_2 = receiver
        
        FriendId friendId2 = new FriendId();
        friendId2.setUserId1(receiverId);  // user_id_1 = receiver  
        friendId2.setUserId2(senderId);    // user_id_2 = sender

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


    /**
     * Chặn bạn bè trong quan hệ hiện có giữa hai người dùng.
     *
     * Quy trình:
     * - Kiểm tra senderId, receiverId hợp lệ và sender trùng với người dùng đang đăng nhập.
     * - Tìm quan hệ bạn bè theo cả hai chiều.
     * - Nếu không tồn tại quan hệ hoặc đã ở trạng thái blocked -> ném lỗi.
     * - Cập nhật trạng thái quan hệ thành blocked và trả về thông tin sau cập nhật.
     *
     * @param dto DTO chứa senderId (người thực hiện chặn) và receiverId (người bị chặn)
     * @return Optional chứa FriendResponseDTO phản ánh trạng thái blocked sau khi cập nhật
     * @throws IllegalArgumentException nếu thiếu tham số, sai người dùng phiên, không tìm thấy quan hệ, hoặc đã bị chặn trước đó
     */
    @Transactional
    public Optional<FriendResponseDTO> blockFriend(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();    // Người gửi lời mời ban đầu
        Integer receiverId = dto.getReceiverId(); // Người nhận lời mời (đang block)

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }

        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        // Find the friend relationship (check both directions)
        FriendId friendId1 = new FriendId();
        friendId1.setUserId1(senderId);    // user_id_1 = sender
        friendId1.setUserId2(receiverId);  // user_id_2 = receiver
        
        FriendId friendId2 = new FriendId();
        friendId2.setUserId1(receiverId);  // user_id_1 = receiver  
        friendId2.setUserId2(senderId);    // user_id_2 = sender

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

    /**
     * Hủy kết bạn giữa hai người dùng.
     *
     * Điều kiện:
     * - senderId và receiverId phải có trong DTO.
     * - senderId phải trùng với user đang đăng nhập (lấy từ session).
     *
     * Xử lý:
     * - Tìm quan hệ bạn bè theo cả hai chiều.
     * - Nếu tồn tại thì xóa và trả về true.
     * - Nếu không tìm thấy quan hệ thì trả về false.
     *
     * Giao dịch: @Transactional đảm bảo xóa an toàn trong một phiên làm việc.
     *
     * Ném:
     * - IllegalArgumentException nếu thiếu dữ liệu hoặc user không hợp lệ.
     *
     * @param dto FriendActionDTO chứa senderId (người gửi ban đầu) và receiverId.
     * @return true nếu hủy kết bạn thành công, false nếu không tìm thấy quan hệ.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không đúng user phiên.
     */
    @Transactional
    public boolean unfriend(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();    // Người gửi lời mời ban đầu
        Integer receiverId = dto.getReceiverId(); // Người nhận lời mời (đang unfriend)

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }
        // Session user check
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        // Find the friend relationship (check both directions)
        FriendId friendId1 = new FriendId();
        friendId1.setUserId1(senderId);    // user_id_1 = sender
        friendId1.setUserId2(receiverId);  // user_id_2 = receiver
        
        FriendId friendId2 = new FriendId();
        friendId2.setUserId1(receiverId);  // user_id_1 = receiver  
        friendId2.setUserId2(senderId);    // user_id_2 = sender

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

    
    @Transactional
    public boolean cancel_request(FriendActionDTO dto) {
        Integer senderId = dto.getSenderId();    // Người gửi lời mời ban đầu
        Integer receiverId = dto.getReceiverId(); // Người nhận lời mời (đang unfriend)

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("sender_id and receiver_id is required");
        }
        // Session user check
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(senderId)) {
            throw new IllegalArgumentException("sender_id does not match the logged-in user");
        }

        // Find the friend relationship (check both directions)
        FriendId friendId1 = new FriendId();
        friendId1.setUserId1(senderId);    // user_id_1 = sender
        friendId1.setUserId2(receiverId);  // user_id_2 = receiver
        
        FriendId friendId2 = new FriendId();
        friendId2.setUserId1(receiverId);  // user_id_1 = receiver  
        friendId2.setUserId2(senderId);    // user_id_2 = sender

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

    @Transactional(readOnly = true)
    public Optional<List<UserResponseDTO>> getFriendSuggestions(Integer userId) {
        // Session user check
        Integer sessionUserId = (Integer) session.getAttribute("userId");
        if (sessionUserId == null || !sessionUserId.equals(userId)) {
            throw new IllegalArgumentException("user_id does not match the logged-in user");
        }

        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        List<Integer> friendIds = friendRepository.findFriendIdsByUserId(userId);
        List<UserResponseDTO> suggestions = userRepository.findSuggestions(userId, friendIds, PageRequest.of(0, 5)) // Giới hạn 5 gợi ý
                .stream()
                .map(user -> {
                    UserResponseDTO dto = new UserResponseDTO();
                    dto.setId(user.getId());
                    dto.setFullName(user.getFullName());
                    dto.setEmail(user.getEmail());
                    dto.setFullName(user.getFullName());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        return Optional.of(suggestions);
    }

    private FriendResponseDTO convertToResponseDTO(Friend friend, Integer currentUserId) {
        FriendResponseDTO dto = new FriendResponseDTO();
        dto.setSenderId(friend.getId().getUserId1());    // user_id_1 = sender
        dto.setReceiverId(friend.getId().getUserId2());  // user_id_2 = receiver
        dto.setStatus(friend.getStatus().name());
        dto.setCreatedAt(friend.getCreatedAt());
        dto.setUpdatedAt(friend.getUpdatedAt());
        
        // Determine which user is the "friend" (not the current user)
        Integer friendId = friend.getId().getUserId1().equals(currentUserId)
            ? friend.getId().getUserId2()
            : friend.getId().getUserId1();
            
        Optional<UserResponseDTO> friendInfo = userService.getUserById(friendId);
        friendInfo.ifPresent(dto::setFriendInfo);
        
        return dto;
    }
}