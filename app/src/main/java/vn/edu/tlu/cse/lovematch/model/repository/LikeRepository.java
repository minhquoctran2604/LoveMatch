package vn.edu.tlu.cse.lovematch.model.repository;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.util.Log;
import vn.edu.tlu.cse.lovematch.model.data.qUser;

public class LikeRepository {

    private static final String TAG = "LikeRepository";
    private final DatabaseReference database;
    private final String currentUserId;

    public LikeRepository() {
        database = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void getCurrentUserLocation(OnLocationListener locationListener) {
        database.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);
                    if (latitude != null && longitude != null) {
                        Log.d(TAG, "getCurrentUserLocation: Found location - latitude: " + latitude + ", longitude: " + longitude);
                        locationListener.onSuccess(latitude, longitude);
                    } else {
                        Log.w(TAG, "getCurrentUserLocation: Latitude or longitude is null");
                        locationListener.onError("Không tìm thấy tọa độ của bạn");
                    }
                } else {
                    Log.w(TAG, "getCurrentUserLocation: User data not found");
                    locationListener.onError("Không tìm thấy thông tin người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getCurrentUserLocation: Error: " + error.getMessage());
                locationListener.onError(error.getMessage());
            }
        });
    }

    public void getUsersWhoLikedMe(final OnResultListener listener, String lastUserId, int pageSize) {
        if (currentUserId == null) {
            listener.onError("User not authenticated");
            return;
        }

        listener.onLoading();
        Query query = database.child("likedBy").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<qUser> users = new ArrayList<>();
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        userIds.add(userId);
                    }
                }
                if (userIds.isEmpty()) {
                    listener.onEmpty();
                    return;
                }
                final int total = userIds.size();
                final int[] count = {0};
                for (String userId : userIds) {
                    database.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userDataSnapshot) {
                            qUser user = userDataSnapshot.getValue(qUser.class);
                            if (user != null) {
                                user.setUid(userId);
                                users.add(user);
                            }
                            count[0]++;
                            if (count[0] == total) {
                                if (users.isEmpty()) {
                                    listener.onEmpty();
                                } else {
                                    listener.onSuccess(users);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            count[0]++;
                            if (count[0] == total) {
                                if (users.isEmpty()) {
                                    listener.onEmpty();
                                } else {
                                    listener.onSuccess(users);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError("Failed to load likedBy data: " + error.getMessage());
            }
        });
    }

    public void getUsersILiked(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<qUser> usersILiked = new ArrayList<>();
        List<String> userIds = new ArrayList<>();

        Query query = database.child("likes").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "getUsersILiked: No users found in likes");
                    listener.onEmpty();
                    return;
                }

                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String likedUserId = likeSnapshot.getKey();
                    if (likedUserId != null) {
                        userIds.add(likedUserId);
                    }
                }
                if (userIds.isEmpty()) {
                    listener.onEmpty();
                    return;
                }
                final int total = userIds.size();
                final int[] count = {0};
                for (String likedUserId : userIds) {
                    database.child("users").child(likedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            qUser user = userSnapshot.getValue(qUser.class);
                            if (user != null) {
                                user.setUid(likedUserId);
                                usersILiked.add(user);
                            }
                            count[0]++;
                            if (count[0] == total) {
                                if (usersILiked.isEmpty()) {
                                    listener.onEmpty();
                                } else {
                                    listener.onSuccess(usersILiked);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            count[0]++;
                            if (count[0] == total) {
                                if (usersILiked.isEmpty()) {
                                    listener.onEmpty();
                                } else {
                                    listener.onSuccess(usersILiked);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getUsersILiked: Error: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        });
    }

    public interface OnResultListener {
        void onSuccess(List<qUser> users);
        void onEmpty();
        void onError(String error);
        void onLoading();
    }

    public interface OnLocationListener {
        void onSuccess(double latitude, double longitude);
        void onError(String error);
    }

    // Add this interface for Like/Dislike actions
    public interface OnActionListener {
        void onSuccess();
        void onError(@NonNull String error);
    }

    // Dummy implementations for likeUser/dislikeUser to avoid compilation error
    public void likeUser(String userId, OnActionListener listener) {
        // ...implement actual like logic here...
        listener.onSuccess();
    }

    public void dislikeUser(String userId, OnActionListener listener) {
        // ...implement actual dislike logic here...
        listener.onSuccess();
    }
}
