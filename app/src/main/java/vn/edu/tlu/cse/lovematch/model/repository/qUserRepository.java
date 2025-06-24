package vn.edu.tlu.cse.lovematch.model.repository;

import android.util.Log;

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
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId == null) {
            listener.onFailure("Người dùng chưa đăng nhập");
            return;
        }

        usersReference.child(currentUserId).child(field).setValue(value, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.d(TAG, "User field updated successfully: " + field);
                    listener.onSuccess();
                } else {
                    Log.e(TAG, "Failed to update user field: " + field, databaseError.toException());
                    listener.onFailure(databaseError.getMessage());
                }
            }
        });
    }
}
