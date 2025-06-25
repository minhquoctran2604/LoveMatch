package vn.edu.tlu.cse.lovematch.model.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import vn.edu.tlu.cse.lovematch.model.data.Notification;

public class NotificationRepository {
    private static final String TAG = "NotificationRepo"; 
    private DatabaseReference usersRef;
    private DatabaseReference chatsRef;
    private ValueEventListener matchesListener;
    private ValueEventListener chatListener; // Listener cho tin nhắn cuối cùng

    public NotificationRepository() {
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        chatsRef = FirebaseDatabase.getInstance().getReference().child("chats");
    }

    public void getNotifications(final OnResultListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            listener.onError("Người dùng chưa đăng nhập.");
            Log.e(TAG, "Lỗi: Người dùng hiện tại là null.");
            return;
        }

        String currentUserId = currentUser.getUid();
        DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference().child("matches").child(currentUserId);

        Log.d(TAG, "Đang lắng nghe matches trên đường dẫn: " + matchesRef.toString());

        listener.onLoading();

        // Hủy listener cũ để tránh rò rỉ và gọi lại nhiều lần
        if (matchesListener != null) {
            matchesRef.removeEventListener(matchesListener);
        }

        // Sử dụng Map để lưu trữ các chat listener, key là chatId
        final Map<String, ValueEventListener> activeChatListeners = new HashMap<>();

        matchesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Gỡ bỏ tất cả các chat listener cũ trước khi thêm mới
                for (ValueEventListener listener : activeChatListeners.values()) {
                    chatsRef.removeEventListener(listener);
                }
                activeChatListeners.clear();

                List<Notification> notifications = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Dữ liệu matches tồn tại. Tìm thấy " + dataSnapshot.getChildrenCount() + " matches.");
                    final int[] pendingFetches = {0}; // Đếm số lượng fetch đang chờ
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String matchedUserId = snapshot.getKey();
                        if (matchedUserId != null) {
                            pendingFetches[0]++;
                            // Lấy thông tin chi tiết của người dùng đã match
                            usersRef.child(matchedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                    if (userSnapshot.exists()) {
                                        String userName = userSnapshot.child("name").getValue(String.class);
                                        List<String> photos = (List<String>) userSnapshot.child("photos").getValue();
                                        String userImage = (photos != null && !photos.isEmpty()) ? photos.get(0) : null;

                                        // Lấy chatId từ node matches
                                        String chatId = snapshot.child("chatId").getValue(String.class);
                                        if (chatId == null) {
                                            Log.w(TAG, "Không tìm thấy chatId cho match với user: " + matchedUserId);
                                            pendingFetches[0]--;
                                            return;
                                        }

                                        // Lắng nghe tin nhắn cuối cùng và trạng thái đọc từ node chats
                                        ValueEventListener currentChatListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot messagesSnapshot) {
                                                String lastMessage = "Bạn đã match với nhau!"; 
                                                long timestamp = snapshot.child("timestamp").getValue(Long.class) != null ? snapshot.child("timestamp").getValue(Long.class) : System.currentTimeMillis(); // Lấy timestamp từ node matches
                                                boolean isUnread = true; // Mặc định là chưa đọc cho match mới

                                                if (messagesSnapshot.exists()) {
                                                    for (DataSnapshot messageSnapshot : messagesSnapshot.getChildren()) {
                                                        lastMessage = messageSnapshot.child("text").getValue(String.class);
                                                        Long messageTimestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                                                        if (messageTimestamp != null) {
                                                            timestamp = messageTimestamp;
                                                        }
                                                        // isUnread sẽ được quản lý bởi logic khác hoặc mặc định là true cho tin nhắn mới
                                                    }
                                                }

                                                Notification notification = new Notification(
                                                        matchedUserId,
                                                        userName,
                                                        userImage,
                                                        lastMessage,
                                                        "", // Thời gian sẽ được định dạng ở Adapter
                                                        isUnread,
                                                        timestamp,
                                                        chatId,
                                                        "new_match" // Loại thông báo
                                                );
                                                // Cập nhật hoặc thêm thông báo vào danh sách
                                                // Để tránh trùng lặp và cập nhật hiệu quả, cần tìm và thay thế notification cũ
                                                // hoặc xóa tất cả và thêm lại. Hiện tại, tôi sẽ thêm vào và để ListChatFragment xử lý.
                                                // Tuy nhiên, để đảm bảo cập nhật đúng, cần một cơ chế tìm kiếm và cập nhật notification cụ thể.
                                                // Tạm thời, tôi sẽ gọi lại loadNotifications() để làm mới toàn bộ danh sách.
                                                listener.onChatsUpdated(); // Thông báo cho fragment rằng có cập nhật chat
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e(TAG, "Lỗi khi đọc lastMessage: " + error.getMessage());
                                            }
                                        };
                                        
                                        // Thêm listener mới và lưu vào map
                                        chatsRef.child(chatId).child("messages").orderByChild("timestamp").limitToLast(1).addValueEventListener(currentChatListener);
                                        activeChatListeners.put(chatId, currentChatListener);

                                        // Giảm pendingFetches ở đây vì việc thêm listener đã hoàn tất
                                        pendingFetches[0]--;
                                        if (pendingFetches[0] == 0) {
                                            // Tất cả dữ liệu đã được fetch
                                            if (notifications.isEmpty()) {
                                                listener.onEmpty();
                                            } else {
                                                listener.onSuccess(notifications);
                                            }
                                        }

                                    } else {
                                        Log.w(TAG, "Không tìm thấy thông tin user cho ID: " + matchedUserId);
                                        pendingFetches[0]--;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Lỗi khi đọc thông tin user: " + error.getMessage());
                                    pendingFetches[0]--;
                                }
                            });
                        }
                    }
                    if (dataSnapshot.getChildrenCount() == 0) { // Trường hợp không có match nào
                        listener.onEmpty();
                    }
                } else {
                    Log.d(TAG, "Không có dữ liệu matches tại đường dẫn được chỉ định.");
                    listener.onEmpty();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Lỗi khi đọc dữ liệu matches: " + databaseError.getMessage());
                listener.onError(databaseError.getMessage());
            }
        };

        matchesRef.addValueEventListener(matchesListener);
    }

    // Hàm này để dọn dẹp khi Fragment bị hủy
    public void removeListeners() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference matchesRef = FirebaseDatabase.getInstance().getReference().child("matches").child(currentUser.getUid());
            if (matchesRef != null && matchesListener != null) {
                matchesRef.removeEventListener(matchesListener);
                Log.d(TAG, "Đã gỡ bỏ Firebase listener cho matches.");
            }
            if (chatListener != null) {
                // Xử lý gỡ bỏ chat listener nếu cần
            }
        }
    }

    // Interface OnResultListener không thay đổi
    public interface OnResultListener {
        void onSuccess(List<Notification> notifications);
        void onEmpty();
        void onChatsUpdated(); // Thêm callback mới khi có cập nhật chats
        void onError(String error);
        void onLoading();
    }
}
