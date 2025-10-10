CREATE DATABASE spkt_connect
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE spkt_connect;

-- ================================
-- Bảng Departments 
-- Lưu danh sách khoa, phòng ban
-- ================================
CREATE TABLE Departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE
);

-- ================================
-- Bảng Users 
-- Thêm các thông tin chi tiết hơn về sinh viên/giảng viên, rất hữu ích cho việc tìm kiếm và định danh.
-- ================================
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    -- Định danh & đăng nhập
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    gender ENUM('male','female','other','prefer_not_to_say'),
    date_of_birth DATE,

    avatar_url VARCHAR(255) DEFAULT '/defaults/avatar.png',
    cover_photo_url VARCHAR(255) DEFAULT '/defaults/cover.png',
    bio VARCHAR(255),

    -- Trường/đơn vị
    school_id VARCHAR(20) UNIQUE, -- Mã số sinh viên / giảng viên
    department_id INT, -- Liên kết đến bảng Departments
    academic_year VARCHAR(20), -- Niên khóa (nếu là sinh viên)
    role ENUM('student', 'lecturer', 'special', 'admin') DEFAULT 'student',

    -- Liên hệ & vị trí
    phone VARCHAR(20),
    website VARCHAR(255),
    country VARCHAR(80),
    city VARCHAR(80),

    -- Học vấn, công việc 
    education VARCHAR(255),
    workplace VARCHAR(255),

    -- Liên kết mạng xã hội
    facebook_url VARCHAR(255),
    instagram_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    twitter_url VARCHAR(255),

    -- Hoạt động
    last_seen_at DATETIME,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (department_id) REFERENCES Departments(department_id)
);


-- ================================
-- Bảng Friends 
-- Cấu trúc này đã ổn cho việc quản lý lời mời và trạng thái bạn bè.
-- ================================
CREATE TABLE Friends (
    user_id_1 INT NOT NULL, -- Người gửi yêu cầu
    user_id_2 INT NOT NULL, -- Người nhận yêu cầu
    status ENUM('pending', 'accepted', 'blocked'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id_1, user_id_2),
    FOREIGN KEY (user_id_1) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id_2) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- ================================
-- Bảng Posts thường và quan trọng
-- Không cần kiểm duyệt với Posts thường
-- ================================
CREATE TABLE Posts (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    author_id INT NOT NULL,
    content TEXT,
    visibility ENUM('public', 'friends', 'private') DEFAULT 'friends',
    
    -- Loại bài post: thường hoặc quan trọng
    post_type ENUM('normal', 'important') DEFAULT 'normal',
    
    -- Chỉ dùng cho post important
    status ENUM('pending', 'approved', 'rejected') DEFAULT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (author_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    INDEX(author_id),
    INDEX(post_type)
);


-- Post Media để lưu trữ ảnh, video, file,... đi kèm cùng với bài post
-- ================================
-- Liên kết trực tiếp với bảng Posts
-- ================================
CREATE TABLE Post_Media (
    media_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL, -- Tham chiếu tới bảng Posts
    media_type ENUM('image', 'video') NOT NULL,
    media_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    INDEX (post_id) -- Tạo chỉ mục để truy vấn nhanh media theo post
);



-- ================================
-- Bảng Post_Reactions 
-- Thay `type` thành `reaction_type` để hỗ trợ nhiều loại cảm xúc (like, love, haha...)
-- ================================
CREATE TABLE Post_Reactions (
    reaction_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    user_id INT NOT NULL,
    reaction_type ENUM('like', 'love', 'haha', 'sad', 'angry') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    -- Mỗi user chỉ có 1 reaction cho 1 post
    UNIQUE KEY (post_id, user_id)
);

-- ================================
-- Bảng Comments
-- Thêm `parent_comment_id` để hỗ trợ comment lồng nhau (trả lời bình luận).
-- ================================
CREATE TABLE Comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    author_id INT NOT NULL,
    parent_comment_id INT, -- Tự tham chiếu đến chính nó để tạo comment lồng nhau
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES Comments(comment_id) ON DELETE CASCADE
);

-- ================================
-- Bảng Chats, Chat_Members, Messages 
-- Các bảng này có cấu trúc khá tốt, chỉ cần thêm một vài trường để tăng trải nghiệm người dùng.
-- ================================
CREATE TABLE Chats (
    chat_id INT AUTO_INCREMENT PRIMARY KEY,
    chat_name VARCHAR(100),
    chat_avatar_url VARCHAR(255), -- Ảnh đại diện cho group chat
    is_group BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Chat_Members (
    chat_id INT NOT NULL,
    user_id INT NOT NULL,
    role_in_chat ENUM('member', 'admin') DEFAULT 'member',
    PRIMARY KEY (chat_id, user_id),
    FOREIGN KEY (chat_id) REFERENCES Chats(chat_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE Messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Dùng BIGINT vì bảng này sẽ rất lớn
    chat_id INT NOT NULL,
    sender_id INT NOT NULL,
    content TEXT,
    media_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chat_id) REFERENCES Chats(chat_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- ================================
-- Bảng Message_Read_Status
-- Theo dõi trạng thái "đã xem" của từng người dùng cho mỗi tin nhắn. Rất quan trọng cho chat group.
-- ================================
CREATE TABLE Message_Read_Status (
    message_id BIGINT NOT NULL,
    user_id INT NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id, user_id),
    FOREIGN KEY (message_id) REFERENCES Messages(message_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- ================================
-- Bảng Notifications (Mở rộng)
-- Thêm các trường để liên kết thông báo đến đối tượng cụ thể (bài viết, comment...)
-- ================================
CREATE TABLE Notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_id INT NOT NULL, -- Người nhận thông báo
    sender_id INT, -- Người gây ra hành động
    type ENUM('friend_request', 'friend_accept', 'post_reaction', 'post_comment', 'comment_reply', 'post_approved', 'important_post') NOT NULL,
    source_id INT, -- ID của đối tượng liên quan (post_id, comment_id, user_id...)
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- ================================
-- Bảng Reports 
-- Tính năng cực kỳ quan trọng để người dùng báo cáo nội dung xấu, giúp admin quản lý cộng đồng.
-- ================================
CREATE TABLE Reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    reporter_id INT NOT NULL, -- Người báo cáo
    reported_content_type ENUM('user', 'post', 'comment') NOT NULL,
    reported_content_id INT NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('pending', 'resolved', 'ignored') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES Users(user_id) ON DELETE CASCADE
);
