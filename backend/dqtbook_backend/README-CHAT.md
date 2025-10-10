# Tính năng Chat trong DQTBook

## Giới thiệu

Tính năng chat trong DQTBook cung cấp khả năng nhắn tin real-time giữa các người dùng thông qua REST API và WebSocket. Các tính năng chính bao gồm:

- Tạo và quản lý các cuộc trò chuyện (1-1 hoặc nhóm)
- Gửi và nhận tin nhắn theo thời gian thực
- Xem trạng thái đọc tin nhắn
- Hiển thị trạng thái đang nhập tin nhắn

## Kiến trúc hệ thống

Hệ thống chat được xây dựng trên nền tảng Spring Boot với WebSocket và STOMP:

1. **REST API** (non-realtime):
   - Quản lý các cuộc trò chuyện (tạo, cập nhật, xóa)
   - Quản lý thành viên của cuộc trò chuyện
   - Tìm kiếm và truy xuất lịch sử tin nhắn

2. **WebSocket API** (realtime):
   - Gửi và nhận tin nhắn ngay lập tức
   - Cập nhật trạng thái đọc tin nhắn
   - Hiển thị khi người dùng đang nhập tin nhắn

## Mô hình dữ liệu

1. **Chat**: Đại diện cho một cuộc trò chuyện
   - id: ID của cuộc trò chuyện
   - chatName: Tên cuộc trò chuyện
   - chatAvatarUrl: URL ảnh đại diện của cuộc trò chuyện
   - isGroup: true nếu là trò chuyện nhóm, false nếu là trò chuyện 1-1
   - createdAt: Thời điểm tạo cuộc trò chuyện

2. **ChatMember**: Đại diện cho thành viên trong cuộc trò chuyện
   - id: Khóa kết hợp (chatId + userId)
   - chat: Liên kết đến cuộc trò chuyện
   - user: Liên kết đến người dùng
   - roleInChat: Vai trò trong cuộc trò chuyện (admin, member)

3. **Message**: Đại diện cho một tin nhắn
   - id: ID của tin nhắn
   - chat: Liên kết đến cuộc trò chuyện
   - sender: Liên kết đến người gửi
   - content: Nội dung tin nhắn
   - mediaUrl: URL media (hình ảnh, video) nếu có
   - createdAt: Thời điểm gửi tin nhắn

4. **MessageReadStatus**: Trạng thái đọc của tin nhắn
   - id: Khóa kết hợp (messageId + userId)
   - message: Liên kết đến tin nhắn
   - user: Liên kết đến người dùng đã đọc
   - readAt: Thời điểm đọc tin nhắn

## REST API Endpoints

### Quản lý cuộc trò chuyện (Chat)

- `GET /api/chats`: Lấy danh sách cuộc trò chuyện của người dùng
- `GET /api/chats/{id}`: Lấy thông tin chi tiết về một cuộc trò chuyện
- `POST /api/chats`: Tạo cuộc trò chuyện mới
- `PUT /api/chats/{id}`: Cập nhật thông tin cuộc trò chuyện
- `DELETE /api/chats/{id}`: Xóa cuộc trò chuyện

### Quản lý thành viên cuộc trò chuyện (ChatMember)

- `GET /api/chats/{id}/members`: Lấy danh sách thành viên trong cuộc trò chuyện
- `POST /api/chats/{id}/members`: Thêm thành viên vào cuộc trò chuyện
- `DELETE /api/chats/{id}/members/{userId}`: Xóa thành viên khỏi cuộc trò chuyện

### Quản lý tin nhắn (Message)

- `GET /api/chats/{id}/messages`: Lấy tin nhắn của một cuộc trò chuyện
- `POST /api/messages`: Gửi tin nhắn mới (REST)
- `GET /api/messages/{id}`: Lấy thông tin chi tiết về một tin nhắn
- `DELETE /api/messages/{id}`: Xóa tin nhắn
- `PUT /api/messages/{id}/read`: Đánh dấu tin nhắn đã đọc
- `PUT /api/chats/{id}/read-all`: Đánh dấu tất cả tin nhắn trong cuộc trò chuyện đã đọc

## WebSocket API

Endpoints WebSocket (sử dụng STOMP):

- `/ws`: Endpoint để kết nối WebSocket
- `/app/chat.sendMessage`: Gửi tin nhắn mới
- `/app/chat.readStatus`: Cập nhật trạng thái đọc tin nhắn
- `/app/chat.typing`: Gửi trạng thái đang nhập tin nhắn

Các kênh đăng ký (STOMP Subscribe):

- `/topic/chat.{chatId}`: Nhận tin nhắn mới cho cuộc trò chuyện có ID là chatId
- `/topic/chat.{chatId}.readStatus`: Nhận cập nhật trạng thái đọc cho cuộc trò chuyện
- `/topic/chat.{chatId}.typing`: Nhận thông báo khi có người đang nhập tin nhắn

## Định dạng dữ liệu WebSocket

### Gửi tin nhắn
```json
{
  "content": "Nội dung tin nhắn",
  "chatId": 1,
  "senderId": 1,
  "messageType": "TEXT"
}
```

### Nhận tin nhắn
```json
{
  "id": 1,
  "content": "Nội dung tin nhắn",
  "chatId": 1,
  "senderId": 1,
  "senderName": "Nguyen Van A",
  "senderAvatar": "https://example.com/avatar.jpg",
  "messageType": "TEXT",
  "timestamp": "2023-06-15T14:30:00"
}
```

### Trạng thái đọc tin nhắn
```json
{
  "chatId": 1,
  "userId": 2,
  "messageId": 5
}
```

### Trạng thái đang nhập
```json
{
  "userId": 1,
  "typing": true,
  "chatId": 1
}
```

## Cách test

1. Chạy ứng dụng Spring Boot
2. Mở URL `http://localhost:8080/chat-test.html` để truy cập giao diện test chat
3. Kết nối WebSocket và thử các tính năng nhắn tin, đánh dấu đã đọc, hiển thị đang nhập

## Tích hợp vào Frontend

### JavaScript

Tích hợp vào ứng dụng frontend bằng SockJS và STOMP:

```javascript
// Kết nối WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Kết nối và đăng ký các kênh
stompClient.connect({}, frame => {
  console.log('Connected: ' + frame);
  
  // Đăng ký nhận tin nhắn cho cuộc trò chuyện có ID = 1
  stompClient.subscribe('/topic/chat.1', message => {
    const receivedMessage = JSON.parse(message.body);
    console.log('Received message:', receivedMessage);
    // Hiển thị tin nhắn trong UI
  });
  
  // Đăng ký nhận cập nhật trạng thái đọc
  stompClient.subscribe('/topic/chat.1.readStatus', readStatus => {
    const readStatusData = JSON.parse(readStatus.body);
    console.log('Message read status:', readStatusData);
    // Cập nhật UI để hiển thị trạng thái đọc
  });
  
  // Đăng ký nhận thông báo đang nhập
  stompClient.subscribe('/topic/chat.1.typing', typing => {
    const typingData = JSON.parse(typing.body);
    console.log('Typing status:', typingData);
    // Hiển thị thông báo đang nhập
  });
});

// Gửi tin nhắn mới
function sendMessage(content, chatId, senderId) {
  const message = {
    content: content,
    chatId: chatId,
    senderId: senderId,
    messageType: "TEXT"
  };
  
  stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
}

// Đánh dấu tin nhắn đã đọc
function markAsRead(chatId, userId, messageId) {
  const readStatus = {
    chatId: chatId,
    userId: userId,
    messageId: messageId
  };
  
  stompClient.send("/app/chat.readStatus", {}, JSON.stringify(readStatus));
}

// Gửi trạng thái đang nhập
function sendTypingStatus(chatId, senderId) {
  const typing = {
    chatId: chatId,
    senderId: senderId
  };
  
  stompClient.send("/app/chat.typing", {}, JSON.stringify(typing));
}
```