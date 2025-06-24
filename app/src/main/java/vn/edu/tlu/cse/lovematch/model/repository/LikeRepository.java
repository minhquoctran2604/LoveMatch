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

    public void getUsersWhoLikedMe(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<qUser> usersWhoLikedMe = new ArrayList<>();
        Set<String> userIds = new HashSet<>();

        Query query = database.child("likedBy").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "getUsersWhoLikedMe: No users found in likedBy");
                    listener.onEmpty();
                    return;
                }

                Log.d(TAG, "getUsersWhoLikedMe: Found " + snapshot.getChildrenCount() + " users in likedBy");
                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String userId = likeSnapshot.getKey();
                    if (userId != null && !userIds.contains(userId)) {
                        database.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (!userSnapshot.exists()) {
                                    Log.e(TAG, "getUsersWhoLikedMe: User data not found for uid: " + userId);
                                    userIds.add(userId);
                                    if (userIds.size() == snapshot.getChildrenCount()) {
                                        if (usersWhoLikedMe.isEmpty()) {
                                            Log.d(TAG, "getUsersWhoLikedMe: No users found after processing");
                                            listener.onEmpty();
                                        } else {
                                            Log.d(TAG, "getUsersWhoLikedMe: Found " + usersWhoLikedMe.size() + " users");
                                            listener.onSuccess(usersWhoLikedMe);
                                        }
                                    }
                                    return;
                                }

                                qUser user = userSnapshot.getValue(qUser.class);
                                if (user != null) {
                                    user.setUid(userId);
                                    usersWhoLikedMe.add(user);
                                    userIds.add(userId);
                                    Log.d(TAG, "getUsersWhoLikedMe: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + userId + ")");
                                } else {
                                    Log.e(TAG, "getUsersWhoLikedMe: Failed to parse user data for uid: " + userId);
                                }

                                if (userIds.size() == snapshot.getChildrenCount()) {
                                    if (usersWhoLikedMe.isEmpty()) {
                                        Log.d(TAG, "getUsersWhoLikedMe: No users found after processing");
                                        listener.onEmpty();
                                    } else {
                                        Log.d(TAG, "getUsersWhoLikedMe: Found " + usersWhoLikedMe.size() + " users");
                                        listener.onSuccess(usersWhoLikedMe);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "getUsersWhoLikedMe: Error fetching user data: " + error.getMessage());
                                listener.onError(error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "getUsersWhoLikedMe: Error: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        });
    }

    public void getUsersILiked(OnResultListener listener, String lastUserId, int pageSize) {
        listener.onLoading();
        List<qUser> usersILiked = new ArrayList<>();
        Set<String> userIds = new HashSet<>();

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

                Log.d(TAG, "getUsersILiked: Found " + snapshot.getChildrenCount() + " users in likes");
                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String likedUserId = likeSnapshot.getKey();
                    if (likedUserId != null && !userIds.contains(likedUserId)) {
                        database.child("users").child(likedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (!userSnapshot.exists()) {
                                    Log.e(TAG, "getUsersILiked: User data not found for uid: " + likedUserId);
                                    userIds.add(likedUserId);
                                    if (userIds.size() == snapshot.getChildrenCount()) {
                                        if (usersILiked.isEmpty()) {
                                            Log.d(TAG, "getUsersILiked: No users found after processing");
                                            listener.onEmpty();
                                        } else {
                                            Log.d(TAG, "getUsersILiked: Found " + usersILiked.size() + " users");
                                            listener.onSuccess(usersILiked);
                                        }
                                    }
                                    return;
                                }

                                qUser user = userSnapshot.getValue(qUser.class);
                                if (user != null) {
                                    user.setUid(likedUserId);
                                    usersILiked.add(user);
                                    userIds.add(likedUserId);
                                    Log.d(TAG, "getUsersILiked: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + likedUserId + ")");
                                } else {
                                    Log.e(TAG, "getUsersILiked: Failed to parse user data for uid: " + likedUserId);
                                }

                                if (userIds.size() == snapshot.getChildrenCount()) {
                                    if (usersILiked.isEmpty()) {
                                        Log.d(TAG, "getUsersILiked: No users found after processing");
                                        listener.onEmpty();
                                    } else {
                                        Log.d(TAG, "getUsersILiked: Found " + usersILiked.size() + " users");
                                        listener.onSuccess(usersILiked);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "getUsersILiked: Error fetching user data: " + error.getMessage());
                                listener.onError(error.getMessage());
                            }
                        });
                    }
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
}