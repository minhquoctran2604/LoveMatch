package vn.edu.tlu.cse.lovematch.controller;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.qUser;
import vn.edu.tlu.cse.lovematch.model.repository.nLikeRepository;
import vn.edu.tlu.cse.lovematch.view.fragment.nLikeFragment;

public class nLikeController {

    private static final String TAG = "nLikeController";
    private static final int PAGE_SIZE = 10;
    private final nLikeFragment fragment;
    private final nLikeRepository repository;
    private List<qUser> usersWhoLikedMe;
    private List<qUser> usersILiked;
    private String lastUserIdWhoLikedMe;
    private String lastUserIdILiked;
    private boolean isLikesTabSelected;
    private double maxDistance = Double.MAX_VALUE;
    private int minAge = 0;
    private int maxAge = Integer.MAX_VALUE;
    private String residenceFilter;
    private final DatabaseReference matchNotificationsRef; // Thêm để đẩy thông báo match

    public nLikeController(nLikeFragment fragment) {
        this.fragment = fragment;
        this.repository = new nLikeRepository();
        this.usersWhoLikedMe = new ArrayList<>();
        this.usersILiked = new ArrayList<>();
        this.lastUserIdWhoLikedMe = null;
        this.lastUserIdILiked = null;
        this.isLikesTabSelected = true;
        this.matchNotificationsRef = FirebaseDatabase.getInstance().getReference("match_notifications");

        // Load current user location
        repository.getCurrentUserLocation(new nLikeRepository.OnLocationListener() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                fragment.setCurrentLocation(latitude, longitude);
                onLikesTabClicked();
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }
        });
    }

    public void onLikesTabClicked() {
        isLikesTabSelected = true;
        fragment.updateTabSelection(true);
        lastUserIdWhoLikedMe = null;
        usersWhoLikedMe.clear();
        loadUsersWhoLikedMe();
    }

    public void onLikedTabClicked() {
        isLikesTabSelected = false;
        fragment.updateTabSelection(false);
        lastUserIdILiked = null;
        usersILiked.clear();
        loadUsersILiked();
    }

    public void loadMoreUsers() {
        if (isLikesTabSelected) {
            loadUsersWhoLikedMe();
        } else {
            loadUsersILiked();
        }
    }

    private void loadUsersWhoLikedMe() {
        repository.getUsersWhoLikedMe(new nLikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<qUser> users) {
                // Loại bỏ trùng lặp trước khi thêm
                Set<String> existingUserIds = new HashSet<>();
                for (qUser existingUser : usersWhoLikedMe) {
                    existingUserIds.add(existingUser.getUid());
                }
                for (qUser newUser : users) {
                    if (!existingUserIds.contains(newUser.getUid())) {
                        usersWhoLikedMe.add(newUser);
                        existingUserIds.add(newUser.getUid());
                    }
                }
                applyFilterToUsers(usersWhoLikedMe);
                fragment.updateUserList(usersWhoLikedMe);
                if (!usersWhoLikedMe.isEmpty()) {
                    lastUserIdWhoLikedMe = usersWhoLikedMe.get(usersWhoLikedMe.size() - 1).getUid();
                }
            }

            @Override
            public void onEmpty() {
                fragment.updateUserList(usersWhoLikedMe);
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }
        }, lastUserIdWhoLikedMe, PAGE_SIZE);
    }

    private void loadUsersILiked() {
        repository.getUsersILiked(new nLikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<qUser> users) {
                // Loại bỏ trùng lặp trước khi thêm
                Set<String> existingUserIds = new HashSet<>();
                for (qUser existingUser : usersILiked) {
                    existingUserIds.add(existingUser.getUid());
                }
                for (qUser newUser : users) {
                    if (!existingUserIds.contains(newUser.getUid())) {
                        usersILiked.add(newUser);
                        existingUserIds.add(newUser.getUid());
                    }
                }
                applyFilterToUsers(usersILiked);
                fragment.updateUserList(usersILiked);
                if (!usersILiked.isEmpty()) {
                    lastUserIdILiked = usersILiked.get(usersILiked.size() - 1).getUid();
                }
            }

            @Override
            public void onEmpty() {
                fragment.updateUserList(usersILiked);
            }

            @Override
            public void onError(String error) {
                fragment.showError(error);
            }

            @Override
            public void onLoading() {
                // Hiển thị loading indicator nếu cần
            }
        }, lastUserIdILiked, PAGE_SIZE);
    }

    public void onLikeUser(qUser otherUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currentUserId = repository.getCurrentUserId();

        // Lưu hành động "thích" vào node likes của người dùng hiện tại
        database.child("likes").child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onLikeUser: Successfully liked user: " + otherUser.getName());
                        // Xóa người dùng khỏi danh sách những người đã thích bạn (nếu có)
                        usersWhoLikedMe.removeIf(user -> user.getUid().equals(otherUser.getUid()));

                        // Thêm người dùng vào danh sách những người bạn đã thích (nếu chưa có)
                        boolean alreadyLiked = false;
                        for (qUser user : usersILiked) {
                            if (user.getUid().equals(otherUser.getUid())) {
                                alreadyLiked = true;
                                break;
                            }
                        }
                        if (!alreadyLiked) {
                            usersILiked.add(otherUser);
                        }

                        // Cập nhật giao diện của tab hiện tại
                        // Nếu tab "Lượt thích" đang được chọn, cập nhật usersWhoLikedMe. Ngược lại, cập nhật usersILiked.
                        // Đảm bảo rằng khi người dùng thích một ai đó, danh sách "usersILiked" được cập nhật và hiển thị nếu tab "Đã thích" đang active.
                        if (isLikesTabSelected) {
                            fragment.updateUserList(usersWhoLikedMe);
                        } else {
                            fragment.updateUserList(usersILiked);
                        }

                        // Lưu vào node likedBy của người được thích
                        database.child("likedBy").child(otherUser.getUid()).child(currentUserId).setValue(true)
                                .addOnCompleteListener(likedByTask -> {
                                    if (likedByTask.isSuccessful()) {
                                        Log.d(TAG, "onLikeUser: Successfully updated likedBy for user: " + otherUser.getUid());
                                    } else {
                                        Log.e(TAG, "onLikeUser: Error updating likedBy: " + likedByTask.getException().getMessage());
                                        fragment.showError("Lỗi khi cập nhật likedBy: " + likedByTask.getException().getMessage());
                                    }
                                });

                        // Kiểm tra ghép đôi
                        checkForMatch(otherUser);
                    } else {
                        Log.e(TAG, "onLikeUser: Error liking user: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi thích: " + task.getException().getMessage());
                    }
                });
    }

    public void onDislikeUser(qUser otherUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currentUserId = repository.getCurrentUserId();

        database.child("likes").child(otherUser.getUid()).child("likedUsers").child(currentUserId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onDislikeUser: Successfully removed current user from other user's likedUsers: " + otherUser.getName());
                        database.child("likes").child(currentUserId).child("likedByUsers").child(otherUser.getUid()).removeValue()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Log.d(TAG, "onDislikeUser: Successfully removed other user from current user's likedByUsers: " + otherUser.getName());
                                        // Xóa người dùng khỏi cả hai danh sách cục bộ
                                        usersWhoLikedMe.removeIf(user -> user.getUid().equals(otherUser.getUid()));
                                        usersILiked.removeIf(user -> user.getUid().equals(otherUser.getUid()));
                                        fragment.updateUserList(usersWhoLikedMe); // Cập nhật lại danh sách hiển thị
                                    } else {
                                        Log.e(TAG, "onDislikeUser: Error removing other user from current user's likedByUsers: " + task2.getException().getMessage());
                                        fragment.showError("Lỗi khi bỏ thích: " + task2.getException().getMessage());
                                    }
                                });
                    } else {
                        Log.e(TAG, "onDislikeUser: Error removing current user from other user's likedUsers: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi bỏ thích: " + task.getException().getMessage());
                    }
                });
    }

    private void checkForMatch(qUser otherUser) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String currentUserId = repository.getCurrentUserId();

        database.child("likes").child(otherUser.getUid()).child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d(TAG, "checkForMatch: Mutual like detected, match successful!");
                            String chatId = currentUserId.compareTo(otherUser.getUid()) < 0
                                    ? currentUserId + "_" + otherUser.getUid()
                                    : otherUser.getUid() + "_" + currentUserId;

                            // Lưu thông tin match cho cả hai bên
                            database.child("matches").child(currentUserId).child(otherUser.getUid()).setValue(true);
                            database.child("matches").child(otherUser.getUid()).child(currentUserId).setValue(true);

                            database.child("chats").child(chatId).child("participants").child(currentUserId).setValue(true);
                            database.child("chats").child(chatId).child("participants").child(otherUser.getUid()).setValue(true);

                            // Xóa thông tin lượt thích của cả hai bên trên Firebase
                            database.child("likes").child(currentUserId).child(otherUser.getUid()).removeValue();
                            database.child("likedBy").child(currentUserId).child(otherUser.getUid()).removeValue();
                            database.child("likes").child(otherUser.getUid()).child(currentUserId).removeValue();
                            database.child("likedBy").child(otherUser.getUid()).child(currentUserId).removeValue();

                            // Xóa người dùng khỏi cả hai danh sách của người dùng hiện tại
                            usersWhoLikedMe.removeIf(user -> user.getUid().equals(otherUser.getUid()));
                            usersILiked.removeIf(user -> user.getUid().equals(otherUser.getUid()));
                            fragment.updateUserList(isLikesTabSelected ? usersWhoLikedMe : usersILiked);

                            String matchedUserName = otherUser.getName() != null ? otherUser.getName() : "người dùng này";
                            Log.d(TAG, "Match successful with user: " + matchedUserName);

                            // Hiển thị dialog match cho người dùng hiện tại
                            fragment.showMatchDialog(matchedUserName, chatId, otherUser);

                            // Đẩy thông báo match cho đối phương
                            pushMatchNotification(currentUserId, otherUser.getUid(), chatId);
                        } else {
                            Log.d(TAG, "checkForMatch: No mutual like found with user: " + otherUser.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "checkForMatch: Error checking match: " + error.getMessage());
                        fragment.showError("Lỗi kiểm tra match: " + error.getMessage());
                    }
                });
    }

    private void pushMatchNotification(String currentUserId, String otherUserId, String chatId) {
        // Tạo thông báo match cho đối phương
        String matchId = String.valueOf(System.currentTimeMillis());
        Map<String, Object> notification = new HashMap<>();
        notification.put("otherUserId", currentUserId);
        notification.put("chatId", chatId);
        notification.put("timestamp", System.currentTimeMillis()); // Đảm bảo timestamp chính xác

        // Đẩy thông báo vào node match_notifications của đối phương
        matchNotificationsRef.child(otherUserId).child(matchId).setValue(notification)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "pushMatchNotification: Successfully pushed match notification to user: " + otherUserId);
                    } else {
                        Log.e(TAG, "pushMatchNotification: Error pushing match notification: " + task.getException().getMessage());
                    }
                });
    }

    public void applyFilter(double maxDistance, int minAge, int maxAge, String residenceFilter) {
        this.maxDistance = maxDistance;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.residenceFilter = residenceFilter != null && !residenceFilter.isEmpty() ? residenceFilter : null;

        applyFilterToUsers(isLikesTabSelected ? usersWhoLikedMe : usersILiked);
        fragment.updateUserList(isLikesTabSelected ? usersWhoLikedMe : usersILiked);
    }

    private void applyFilterToUsers(List<qUser> users) {
        List<qUser> filteredUsers = new ArrayList<>();
        for (qUser user : users) {
            // Lọc theo khoảng cách
            double distance = calculateDistance(user);
            if (distance > maxDistance) {
                continue;
            }

            // Lọc theo độ tuổi
            int age = calculateAge(user);
            if (age < minAge || age > maxAge) {
                continue;
            }

            // Lọc theo nơi cư trú
            if (residenceFilter != null && !residenceFilter.equals(user.getResidence())) {
                continue;
            }

            filteredUsers.add(user);
        }
        users.clear();
        users.addAll(filteredUsers);
    }

    private double calculateDistance(qUser user) {
        // Tính khoảng cách dựa trên latitude và longitude (giả định)
        return 0; // Thay bằng logic thực tế nếu cần
    }

    private int calculateAge(qUser user) {
        // Tính tuổi dựa trên dateOfBirth (giả định)
        return 25; // Thay bằng logic thực tế nếu cần
    }

    public void onUserClicked(qUser user) {
        // Điều hướng đến ProfileMyFriendActivity hoặc fragment tương ứng
        Bundle bundle = new Bundle();
        bundle.putString("friendId", user.getUid());
        bundle.putBoolean("fromLikeFragment", true);
        fragment.getNavController().navigate(R.id.action_likeFragment_to_profileMyFriendActivity, bundle);
    }

    public List<qUser> getUsersWhoLikedMe() {
        return new ArrayList<>(usersWhoLikedMe);
    }

    public List<qUser> getUsersILiked() {
        return new ArrayList<>(usersILiked);
    }

    public boolean isLikesTabSelected() {
        return isLikesTabSelected;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getResidenceFilter() {
        return residenceFilter;
    }
}
