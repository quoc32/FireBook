USE spkt_connect;

-- ================================
-- Thêm bảng Post_Shares (luôn trỏ về bài gốc)
-- ================================
CREATE TABLE IF NOT EXISTS Post_Shares (
    share_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,        -- Luôn là ID của bài gốc
    user_id INT NOT NULL,        -- Ai share
    shared_text TEXT,
    visibility ENUM('public', 'friends', 'private') DEFAULT 'friends',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (post_id) REFERENCES Posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Cập nhật bảng Notifications để thêm loại thông báo "post_share"
ALTER TABLE Notifications 
  MODIFY COLUMN `type` ENUM(
    'friend_request', 
    'friend_accept', 
    'post_reaction', 
    'post_comment', 
    'comment_reply', 
    'post_approved', 
    'important_post', 
    'post_share'
  ) NOT NULL;
