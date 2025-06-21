package vn.edu.tlu.cse.lovematch.model.repository;

import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.edu.tlu.cse.lovematch.model.data.xUser;

public class xUserRepository {
    private static final String TAG = "UserRepository";
    private final DatabaseReference usersReference;

    public xUserRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
    }

    public interface OnUserActionListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void saveUser(xUser user, OnUserActionListener listener) {
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
}
