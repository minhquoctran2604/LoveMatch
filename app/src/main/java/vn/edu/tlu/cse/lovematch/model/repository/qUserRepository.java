package vn.edu.tlu.cse.lovematch.model.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.edu.tlu.cse.lovematch.model.data.qUser;

public class qUserRepository {
    private static final String TAG = "UserRepository";
    private final DatabaseReference usersReference;

    public qUserRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
    }

    public interface OnUserActionListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void saveUser(qUser user, OnUserActionListener listener) {
        if (user == null || user.getId() == null) {
            listener.onFailure("Thông tin người dùng không hợp lệ");
            return;
        }

        usersReference.child(user.getId()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "User saved successfully");
                    listener.onSuccess();
                } else {
                    Log.e(TAG, "Failed to save user", databaseError.toException());
                    listener.onFailure(databaseError.getMessage());
                }
            }
        });
    }

    public void updateUserField(String field, Object value, OnUserActionListener listener) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (currentUserId == null) {
            Log.w(TAG, "updateUserField: No current user found");
            listener.onFailure("Người dùng chưa đăng nhập hoặc không hợp lệ");
            return;
        }

        if (field == null || field.trim().isEmpty()) {
            Log.w(TAG, "updateUserField: Field name is invalid");
            listener.onFailure("Tên trường không hợp lệ");
            return;
        }

        if (value == null) {
            Log.w(TAG, "updateUserField: Value is null for field: " + field);
            listener.onFailure("Giá trị không được để trống");
            return;
        }

        DatabaseReference userRef = usersReference.child(currentUserId).child(field);
        userRef.setValue(value)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User field updated successfully: " + field + " = " + value);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user field: " + field, e);
                    listener.onFailure("Lỗi khi cập nhật trường " + field + ": " + e.getMessage());
                });
    }
}
