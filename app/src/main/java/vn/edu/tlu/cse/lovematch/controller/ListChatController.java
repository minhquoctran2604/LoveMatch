package vn.edu.tlu.cse.lovematch.controller;


import android.os.Bundle;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.Notification;
import vn.edu.tlu.cse.lovematch.model.repository.NotificationRepository;
import vn.edu.tlu.cse.lovematch.view.fragment.ListChatFragment;

public class ListChatController {

    private static final String TAG = "ListChatController";
    private final ListChatFragment fragment;
    private final NotificationRepository notificationRepository;
    private final String currentUserId;

    public ListChatController(ListChatFragment fragment) {
        this.fragment = fragment;
        this.notificationRepository = new NotificationRepository();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void loadNotifications() {
        notificationRepository.getNotifications(new NotificationRepository.OnResultListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                fragment.updateNotifications(notifications);
            }

            @Override
            public void onEmpty() {
                fragment.showError("Không có match nào để hiển thị");
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }

            @Override
            public void onChatsUpdated() {
                loadNotifications(); // Tải lại danh sách khi có cập nhật chat
            }
        });
    }

    public void onNotificationClicked(Notification notification) {
        String chatId = currentUserId.compareTo(notification.getUserId()) < 0
                ? currentUserId + "_" + notification.getUserId()
                : notification.getUserId() + "_" + currentUserId;

        DatabaseReference lastMessageRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("lastMessage");
        Map<String, Object> updates = new HashMap<>();
        updates.put("isUnread", false);
        lastMessageRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    notification.setUnread(false);
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", notification.getUserId());
                    bundle.putString("userName", notification.getUserName());
                    fragment.getNavController().navigate(R.id.action_listChatFragment_to_chatUserFragment, bundle);
                })
                .addOnFailureListener(e -> {
                    fragment.showError("Lỗi khi cập nhật trạng thái đọc: " + e.getMessage());
                });
    }

    public void onDestroy() {
        notificationRepository.removeListeners();
        Log.d(TAG, "onDestroy: Removed Firebase listeners");
    }
}