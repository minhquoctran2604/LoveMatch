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
import vn.edu.tlu.cse.lovematch.model.data.Notification;

public class NotificationRepository {
    private static final String TAG = "NotificationRepo"; 
    private DatabaseReference userMatchesRef;
    private ValueEventListener notificationsListener;

    public NotificationRepository() {
        // Constructor để trống, sẽ khởi tạo Ref khi cần để an toàn hơn
    }

    public void getNotifications(final OnResultListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            listener.onError("Người dùng chưa đăng nhập.");
            Log.e(TAG, "Lỗi: Người dùng hiện tại là null.");
            return;
        }

        String currentUserId = currentUser.getUid();
        // Thêm listener cho cả matches và chats
        userMatchesRef = FirebaseDatabase.getInstance().getReference()
            .child("users").child(currentUserId).child("matches");

        Log.d(TAG, "Đang lắng nghe trên đường dẫn: " + userMatchesRef.toString());

        listener.onLoading();

        // Hủy listener cũ để tránh rò rỉ và gọi lại nhiều lần
        if (notificationsListener != null) {
            userMatchesRef.removeEventListener(notificationsListener);
        }

        notificationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Notification> notifications = new ArrayList<>();
                // 3. Kiểm tra xem có dữ liệu không
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "Dữ liệu tồn tại. Tìm thấy " + dataSnapshot.getChildrenCount() + " matches.");
                    // 4. Lặp qua từng match và chuyển nó trực tiếp thành object Notification
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Firebase sẽ tự động map các trường trong JSON vào class Notification
                        Notification notification = snapshot.getValue(Notification.class);
                        if (notification != null) {
                            notifications.add(notification);
                        } else {
                            Log.w(TAG, "Không thể chuyển đổi snapshot thành Notification object: " + snapshot.getKey());
                        }
                    }
                } else {
                    Log.d(TAG, "Không có dữ liệu tại đường dẫn được chỉ định.");
                }

                // 5. GỬI KẾT QUẢ VỀ CHO UI
                if (notifications.isEmpty()) {
                    listener.onEmpty();
                } else {
                    // Chỉ gọi onSuccess MỘT LẦN DUY NHẤT với danh sách đầy đủ
                    listener.onSuccess(notifications);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Lỗi khi đọc dữ liệu: " + databaseError.getMessage());
                listener.onError(databaseError.getMessage());
            }
        };

        // Gắn listener vào
        userMatchesRef.addValueEventListener(notificationsListener);
    }

    // Hàm này để dọn dẹp khi Fragment bị hủy
    public void removeListeners() {
        if (userMatchesRef != null && notificationsListener != null) {
            userMatchesRef.removeEventListener(notificationsListener);
            Log.d(TAG, "Đã gỡ bỏ Firebase listener.");
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