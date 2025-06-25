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

public class nLikeRepository {

    private static final String TAG = "nLikeRepository";
    private final DatabaseReference database;
    private final String currentUserId;
    private long processedUsers;
    private long totalUsers;

    public nLikeRepository() {
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

        // Đường dẫn đúng là "likedBy/{currentUserId}"
        Query query = database.child("likedBy").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "getUsersWhoLikedMe: No users found in likedBy for current user.");
                    listener.onEmpty();
                    return;
                }

                Log.d(TAG, "getUsersWhoLikedMe: Found " + snapshot.getChildrenCount() + " users who liked current user.");
                final int[] pendingFetches = {0}; // Đếm số lượng fetch đang chờ
                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String likedByUserId = likeSnapshot.getKey(); // Lấy key là userId của người đã thích bạn
                    if (likedByUserId != null && !userIds.contains(likedByUserId)) {
                        pendingFetches[0]++;
                        database.child("users").child(likedByUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (!userSnapshot.exists()) {
                                    Log.e(TAG, "getUsersWhoLikedMe: User data not found for uid: " + likedByUserId);
                                    qUser minimalUser = new qUser();
                                    minimalUser.setUid(likedByUserId);
                                    minimalUser.setName("Người dùng không xác định");
                                    usersWhoLikedMe.add(minimalUser);
                                } else {
                                    qUser user = userSnapshot.getValue(qUser.class);
                                    if (user != null) {
                                        user.setUid(likedByUserId); // Đảm bảo UID được set
                                        usersWhoLikedMe.add(user);
                                        Log.d(TAG, "getUsersWhoLikedMe: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + likedByUserId + ")");
                                    } else {
                                        Log.e(TAG, "getUsersWhoLikedMe: Failed to parse user data for uid: " + likedByUserId);
                                    }
                                }
                                userIds.add(likedByUserId); // Thêm vào set sau khi xử lý
                                pendingFetches[0]--;
                                if (pendingFetches[0] == 0) {
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
                                pendingFetches[0]--;
                                if (pendingFetches[0] == 0) {
                                    if (usersWhoLikedMe.isEmpty()) {
                                        listener.onEmpty();
                                    } else {
                                        listener.onSuccess(usersWhoLikedMe);
                                    }
                                }
                                listener.onError(error.getMessage());
                            }
                        });
                    }
                }
                if (snapshot.getChildrenCount() == 0) { // Trường hợp không có người dùng nào thích bạn
                    listener.onEmpty();
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

        // Đường dẫn đúng là likes/{currentUserId}
        Query query = database.child("likes").child(currentUserId).orderByKey();
        if (lastUserId != null) {
            query = query.startAfter(lastUserId);
        }
        query.limitToFirst(pageSize).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "getUsersILiked: No users found in likes for current user.");
                    listener.onEmpty();
                    return;
                }

                Log.d(TAG, "getUsersILiked: Found " + snapshot.getChildrenCount() + " liked users for current user.");
                final List<String> likedUserIdsToFetch = new ArrayList<>();

                for (DataSnapshot likeSnapshot : snapshot.getChildren()) {
                    String likedUserId = likeSnapshot.getKey(); // Lấy key là userId của người đã thích
                    if (likedUserId != null && !userIds.contains(likedUserId)) {
                        likedUserIdsToFetch.add(likedUserId);
                    }
                }

                if (likedUserIdsToFetch.isEmpty()) {
                    Log.d(TAG, "getUsersILiked: No actual liked users found after parsing.");
                    listener.onEmpty();
                    return;
                }

                final int[] pendingFetches = {likedUserIdsToFetch.size()};
                for (String likedUserId : likedUserIdsToFetch) {
                    database.child("users").child(likedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                            if (!userSnapshot.exists()) {
                                Log.e(TAG, "getUsersILiked: User data not found for uid: " + likedUserId);
                                qUser minimalUser = new qUser();
                                minimalUser.setUid(likedUserId);
                                minimalUser.setName("Người dùng không xác định");
                                usersILiked.add(minimalUser);
                            } else {
                                qUser user = userSnapshot.getValue(qUser.class);
                                if (user != null) {
                                    user.setUid(likedUserId); // Đảm bảo UID được set
                                    usersILiked.add(user);
                                    Log.d(TAG, "getUsersILiked: Added user " + (user.getName() != null ? user.getName() : "Unknown") + " (uid: " + likedUserId + ")");
                                } else {
                                    Log.e(TAG, "getUsersILiked: Failed to parse user data for uid: " + likedUserId);
                                }
                            }
                            userIds.add(likedUserId); // Thêm vào set sau khi xử lý
                            pendingFetches[0]--;
                            if (pendingFetches[0] == 0) {
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
                            pendingFetches[0]--;
                            if (pendingFetches[0] == 0) {
                                if (usersILiked.isEmpty()) {
                                    listener.onEmpty();
                                } else {
                                    listener.onSuccess(usersILiked);
                                }
                            }
                            listener.onError(error.getMessage());
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
}
