package vn.edu.tlu.cse.lovematch.model.data;


public class Notification {
    private String userId; // ID của người dùng gửi thông báo (có thể là otherUserId)
    private String userName; // Tên của người dùng
    private String userImage; // URL hình ảnh của người dùng
    private String lastMessage; // Tin nhắn cuối cùng
    private String time; // Thời gian thông báo (định dạng chuỗi)
    private boolean isUnread; // Trạng thái chưa xem
    private long timestamp; // Thời gian tạo thông báo (Unix timestamp)
    private String chatId; // ID của cuộc trò chuyện
    private String type; // Loại thông báo (ví dụ: "new_match", "new_message")

    public Notification() {
    }

    public Notification(String userId, String userName, String userImage, String lastMessage, String time, boolean isUnread, long timestamp, String chatId, String type) {
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
        this.lastMessage = lastMessage;
        this.time = time;
        this.isUnread = isUnread;
        this.timestamp = timestamp;
        this.chatId = chatId;
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
