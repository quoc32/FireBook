### Chat WebSocket API Test
### Lưu ý: Các test STOMP WebSocket dưới đây là các hướng dẫn, không thể chạy trực tiếp trong REST Client
### Bạn cần sử dụng một client WebSocket như Postman hoặc viết mã JavaScript để test

### Kết nối WebSocket
# URL kết nối: ws://localhost:8080/ws
# Sử dụng SockJS và STOMP để kết nối

### Gửi tin nhắn mới
# Destination: /app/chat.sendMessage
# Body:
{
  "content": "Xin chào, đây là tin nhắn test",
  "chatId": 1,
  "senderId": 1,
  "messageType": "TEXT"
}

### Cập nhật trạng thái đọc tin nhắn
# Destination: /app/chat.readStatus
# Body:
{
  "chatId": 1,
  "userId": 2,
  "messageId": 5
}

### Gửi trạng thái đang nhập
# Destination: /app/chat.typing
# Body:
{
  "chatId": 1,
  "senderId": 2
}

### Đăng ký nhận tin nhắn từ một chat cụ thể
# Subscribe to: /topic/chat.1
# (Trong đó 1 là ID của chat)

### Đăng ký nhận thông báo trạng thái đọc từ một chat cụ thể
# Subscribe to: /topic/chat.1.readStatus
# (Trong đó 1 là ID của chat)

### Đăng ký nhận thông báo đang nhập từ một chat cụ thể
# Subscribe to: /topic/chat.1.typing
# (Trong đó 1 là ID của chat)

### Mã JavaScript client để test WebSocket
```javascript
// Kết nối WebSocket
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// Connect
stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe để nhận tin nhắn từ chat có ID = 1
  stompClient.subscribe('/topic/chat.1', function(message) {
    const messageBody = JSON.parse(message.body);
    console.log('Received message:', messageBody);
  });
  
  // Subscribe để nhận thông báo trạng thái đọc từ chat có ID = 1
  stompClient.subscribe('/topic/chat.1.readStatus', function(readStatus) {
    const readStatusBody = JSON.parse(readStatus.body);
    console.log('Message read status:', readStatusBody);
  });
  
  // Subscribe để nhận thông báo đang nhập từ chat có ID = 1
  stompClient.subscribe('/topic/chat.1.typing', function(typing) {
    const typingBody = JSON.parse(typing.body);
    console.log('Typing status:', typingBody);
  });
});

// Gửi tin nhắn mới
function sendMessage() {
  const message = {
    content: "Hello, this is a test message",
    chatId: 1,
    senderId: 1,
    messageType: "TEXT"
  };
  
  stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
}

// Đánh dấu tin nhắn đã đọc
function markMessageAsRead() {
  const readStatus = {
    chatId: 1,
    userId: 2,
    messageId: 5
  };
  
  stompClient.send("/app/chat.readStatus", {}, JSON.stringify(readStatus));
}

// Gửi thông báo đang nhập
function sendTypingStatus() {
  const typing = {
    chatId: 1,
    senderId: 2
  };
  
  stompClient.send("/app/chat.typing", {}, JSON.stringify(typing));
}
```

### Tích hợp với HTML để test
```html
<!DOCTYPE html>
<html>
<head>
  <title>WebSocket Chat Test</title>
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stomp-websocket@2.3.4-next/lib/stomp.min.js"></script>
</head>
<body>
  <h2>WebSocket Chat Test</h2>
  
  <div>
    <button onclick="connect()">Connect</button>
    <button onclick="disconnect()">Disconnect</button>
  </div>
  
  <div>
    <h3>Send Message</h3>
    <input type="text" id="messageContent" placeholder="Message content" />
    <button onclick="sendMessage()">Send</button>
  </div>
  
  <div>
    <h3>Mark Message as Read</h3>
    <input type="text" id="messageId" placeholder="Message ID" />
    <button onclick="markMessageAsRead()">Mark as Read</button>
  </div>
  
  <div>
    <h3>Send Typing Status</h3>
    <button onclick="sendTypingStatus()">I'm typing...</button>
  </div>
  
  <div>
    <h3>Messages</h3>
    <ul id="messagesList"></ul>
  </div>
  
  <script>
    let stompClient = null;
    const userId = 1; // Đổi thành ID người dùng đang đăng nhập
    const chatId = 1; // Đổi thành chat ID mà bạn muốn test
    
    function connect() {
      const socket = new SockJS('http://localhost:8080/ws');
      stompClient = Stomp.over(socket);
      
      stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        
        // Đăng ký nhận tin nhắn
        stompClient.subscribe('/topic/chat.' + chatId, function(message) {
          showMessage(JSON.parse(message.body));
        });
        
        // Đăng ký nhận trạng thái đọc
        stompClient.subscribe('/topic/chat.' + chatId + '.readStatus', function(readStatus) {
          console.log('Message read status:', JSON.parse(readStatus.body));
        });
        
        // Đăng ký nhận trạng thái đang nhập
        stompClient.subscribe('/topic/chat.' + chatId + '.typing', function(typing) {
          console.log('Typing status:', JSON.parse(typing.body));
        });
      });
    }
    
    function disconnect() {
      if (stompClient !== null) {
        stompClient.disconnect();
      }
      console.log("Disconnected");
    }
    
    function sendMessage() {
      const content = document.getElementById('messageContent').value;
      const message = {
        content: content,
        chatId: chatId,
        senderId: userId,
        messageType: "TEXT"
      };
      
      stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
    }
    
    function markMessageAsRead() {
      const messageId = document.getElementById('messageId').value;
      const readStatus = {
        chatId: chatId,
        userId: userId,
        messageId: messageId
      };
      
      stompClient.send("/app/chat.readStatus", {}, JSON.stringify(readStatus));
    }
    
    function sendTypingStatus() {
      const typing = {
        chatId: chatId,
        senderId: userId
      };
      
      stompClient.send("/app/chat.typing", {}, JSON.stringify(typing));
    }
    
    function showMessage(message) {
      const messagesList = document.getElementById('messagesList');
      const li = document.createElement('li');
      li.innerHTML = `<strong>${message.senderName}:</strong> ${message.content}`;
      messagesList.appendChild(li);
    }
  </script>
</body>
</html>
```