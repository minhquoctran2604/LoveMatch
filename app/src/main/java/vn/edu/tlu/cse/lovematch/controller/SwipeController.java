package vn.edu.tlu.cse.lovematch.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.User;
import vn.edu.tlu.cse.lovematch.view.adapter.CardStackAdapter;
import vn.edu.tlu.cse.lovematch.view.fragment.SwipeFragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class SwipeController {

    private static final String TAG = "SwipeController";
    private static final int PAGE_SIZE = 10;

    private final SwipeFragment fragment;
    private final CardStackView cardStackView;
    private final View skipCircle;
    private final View likeCircle;
    private final ImageButton skipButton;
    private final ImageButton likeButton;
    private final TextView matchNotificationText;
    private final View matchNotificationLayout;
    private final NavController navController;
    private final DatabaseReference database;
    private final DatabaseReference matchNotificationsRef;
    private final String currentUserId;
    private final List<User> userList;
    private final CardStackAdapter adapter;
    private final CardStackLayoutManager layoutManager;
    private final Set<String> matchedUserIds;
    private final Set<String> skippedUserIds;
    private boolean isSkipButtonPressed;
    private String lastUserId;
    private boolean isLoading;
    private final Stack<SwipeAction> swipeHistory;
    private User currentUser;

    private static class SwipeAction {
        User user;
        Direction direction;

        SwipeAction(User user, Direction direction) {
            this.user = user;
            this.direction = direction;
        }
    }

    public chSwipeController(SwipeFragment fragment, CardStackView cardStackView, View skipCircle, View likeCircle,
                             ImageButton skipButton, ImageButton likeButton, TextView matchNotificationText,
                             View matchNotificationLayout, NavController navController,
                             List<User> userList, CardStackAdapter adapter) {
        this.fragment = fragment;
        this.cardStackView = cardStackView;
        this.skipCircle = skipCircle;
        this.likeCircle = likeCircle;
        this.skipButton = skipButton;
        this.likeButton = likeButton;
        this.matchNotificationText = matchNotificationText;
        this.matchNotificationLayout = matchNotificationLayout;
        this.navController = navController;
        this.database = FirebaseDatabase.getInstance().getReference();
        this.matchNotificationsRef = database.child("match_notifications");
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.userList = userList;
        this.adapter = adapter;
        this.layoutManager = new CardStackLayoutManager(fragment.getContext());
        this.matchedUserIds = new HashSet<>();
        this.skippedUserIds = new HashSet<>();
        this.isSkipButtonPressed = false;
        this.lastUserId = null;
        this.isLoading = false;
        this.swipeHistory = new Stack<>();
        initializeCardStack();
        loadCurrentUser();
    }

    private void initializeCardStack() {
        // Set up swipe animation for the card stack
        SwipeAnimationSetting swipeAnimationSetting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build();
        layoutManager.setSwipeAnimationSetting(swipeAnimationSetting);
        cardStackView.setLayoutManager(layoutManager);

        // Skip button listener
        skipButton.setOnClickListener(v -> {
            Log.d(TAG, "Skip button clicked: Performing swipe left");
            isSkipButtonPressed = true;
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
            fragment.showSkipAnimationOnButton(skipButton);
        });

        // Like button listener
        likeButton.setOnClickListener(v -> {
            Log.d(TAG, "Like button clicked: Performing swipe right");
            isSkipButtonPressed = false;
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Right)
                    .setDuration(Duration.Normal.duration)
                    .build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
            fragment.showLikeAnimationOnButton(likeButton);
        });
    }

    public void resetPagination() {
        // Reset pagination state
        lastUserId = null;
        userList.clear();
        swipeHistory.clear();
        adapter.notifyDataSetChanged();
        loadUsers();
    }

    public void loadUsers() {
        if (isLoading) {
            Log.d(TAG, "loadUsers: Already loading, skipping...");
            return;
        }

        if (currentUser == null) {
            Log.d(TAG, "loadUsers: Current user is null, waiting...");
            return;
        }

        isLoading = true;
        // Fetch matched users
        database.child("matches").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchedUserIds.clear();
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    matchedUserIds.add(matchSnapshot.getKey());
                }
                Log.d(TAG, "Matched users: " + matchedUserIds);

                // Build query for users
                Query query = database.child("users").orderByKey();
                if (lastUserId != null) {
                    query = query.startAfter(lastUserId);
                }
                query.limitToFirst(PAGE_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> newUsers = new ArrayList<>();
                        boolean hasOtherGender = snapshot.getChildren().iterator().hasNext() &&
                                snapshot.getChildren().iterator().next().getValue(User.class).getGender().equals("Khác");

                        // Filter users based on gender and match status
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null && isValidUser(user)) {
                                String currentUserGender = currentUser.getGender();
                                String userGender = user.getGender();
                                if (shouldIncludeUser(currentUserGender, userGender, hasOtherGender)) {
                                    newUsers.add(user);
                                    Log.d(TAG, "User added: " + user.getName() + ", Gender: " + userGender);
                                }
                            }
                        }

                        updateUserList(newUsers);
                        isLoading = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleDatabaseError(error, "Error loading users");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error, "Error loading matches");
            }
        });
    }

    private boolean isValidUser(User user) {
        return user != null && !user.getUid().equals(currentUserId) &&
                !matchedUserIds.contains(user.getUid()) &&
                !skippedUserIds.contains(user.getUid());
    }

    private boolean shouldIncludeUser(String currentUserGender, String userGender, boolean hasOtherGender) {
        if (currentUserGender == null || userGender == null) return false;

        if ("Khác".equals(currentUserGender)) {
            return "Nam".equals(userGender) || "Nữ".equals(userGender);
        } else if ("Nữ".equals(currentUserGender)) {
            return hasOtherGender ? ("Khác".equals(userGender) || "Nam".equals(userGender)) : "Nam".equals(userGender);
        } else if ("Nam".equals(currentUserGender)) {
            return hasOtherGender ? ("Nữ".equals(userGender) || "Khác".equals(userGender)) : "Nữ".equals(userGender);
        }
        return false;
    }

    private void updateUserList(List<User> newUsers) {
        if (!newUsers.isEmpty()) {
            lastUserId = newUsers.get(newUsers.size() - 1).getUid();
            userList.addAll(newUsers);
            adapter.notifyDataSetChanged();
            fragment.showUsers();
            Log.d(TAG, "Loaded " + newUsers.size() + " new users, total: " + userList.size());
        } else if (lastUserId != null) {
            loadUsers();
        } else if (userList.isEmpty()) {
            fragment.showError("Không có người dùng nào để hiển thị");
        }
    }

    private void loadCurrentUser() {
        database.child("users").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    Log.d(TAG, "Current user loaded: " + currentUser.getName() + ", Gender: " + currentUser.getGender());
                    adapter.setCurrentUserLocation(
                            currentUser.isLocationEnabled() ? currentUser.getLatitude() : 0.0,
                            currentUser.isLocationEnabled() ? currentUser.getLongitude() : 0.0
                    );
                    loadUsers();
                } else {
                    handleDatabaseError(null, "Không tìm thấy thông tin người dùng hiện tại");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleDatabaseError(error, "Lỗi tải thông tin người dùng");
            }
        });
    }

    public void handleCardSwiped(Direction direction) {
        if (userList.isEmpty()) {
            Log.w(TAG, "User list is empty during swipe");
            return;
        }

        int index = layoutManager.getTopPosition();
        if (index < 0 || index >= userList.size()) {
            Log.e(TAG, "Invalid index: " + index);
            return;
        }

        User otherUser = userList.get(index);
        Log.d(TAG, "Swiped user: " + otherUser.getName() + " (uid: " + otherUser.getUid() + ")");

        if (isSkipButtonPressed) {
            direction = Direction.Left;
            isSkipButtonPressed = false;
        }

        swipeHistory.push(new SwipeAction(otherUser, direction));

        if (direction == Direction.Right) {
            fragment.showLikeAnimation();
            likeUser(otherUser);
            checkForMatch(otherUser);
        } else {
            fragment.showSkipAnimation();
            skippedUserIds.add(otherUser.getUid());
            userList.remove(otherUser);
            updateLikedBy(otherUser);
        }
    }

    private void updateLikedBy(User otherUser) {
        database.child("likes").child(otherUser.getUid()).child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            database.child("likedBy").child(currentUserId).child(otherUser.getUid()).setValue(true);
                        }
                        database.child("likes").child(otherUser.getUid()).child(currentUserId).removeValue();
                        adapter.notifyDataSetChanged();
                        cardStackView.scheduleLayoutAnimation();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleDatabaseError(error, "Error checking likes");
                    }
                });
    }

    public void undoLastSwipe() {
        if (swipeHistory.isEmpty()) {
            Toast.makeText(fragment.getContext(), "Không có hành động để hoàn tác", Toast.LENGTH_SHORT).show();
            return;
        }

        SwipeAction lastAction = swipeHistory.pop();
        User user = lastAction.user;
        Direction direction = lastAction.direction;

        if (direction == Direction.Right) {
            database.child("likes").child(currentUserId).child(user.getUid()).removeValue();
            matchedUserIds.remove(user.getUid());
        } else {
            skippedUserIds.remove(user.getUid());
        }

        userList.add(0, user);
        adapter.notifyDataSetChanged();
        cardStackView.rewind();
        Toast.makeText(fragment.getContext(), "Đã hoàn tác", Toast.LENGTH_SHORT).show();
    }

    private void likeUser(User otherUser) {
        database.child("likes").child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        database.child("likedBy").child(otherUser.getUid()).child(currentUserId).setValue(true);
                    } else {
                        handleDatabaseError(task.getException(), "Lỗi khi thích");
                    }
                });
    }

    private void checkForMatch(User otherUser) {
        database.child("likes").child(otherUser.getUid()).child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String chatId = createChatId(otherUser.getUid());
                            saveMatch(otherUser.getUid(), chatId);
                            fragment.showMatchDialog(otherUser.getName() != null ? otherUser.getName() : "người dùng này", chatId, otherUser);
                            pushMatchNotification(currentUserId, otherUser.getUid(), chatId);
                            matchedUserIds.add(otherUser.getUid());
                            userList.remove(otherUser);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleDatabaseError(error, "Lỗi kiểm tra match");
                    }
                });
    }

    private String createChatId(String otherUserId) {
        return currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;
    }

    private void saveMatch(String otherUserId, String chatId) {
        database.child("matches").child(currentUserId).child(otherUserId).setValue(true);
        database.child("matches").child(otherUserId).child(currentUserId).setValue(true);
        database.child("chats").child(chatId).child("participants").child(currentUserId).setValue(true);
        database.child("chats").child(chatId).child("participants").child(otherUserId).setValue(true);

        // Clear likes
        database.child("likes").child(currentUserId).child(otherUserId).removeValue();
        database.child("likedBy").child(currentUserId).child(otherUserId).removeValue();
        database.child("likes").child(otherUserId).child(currentUserId).removeValue();
        database.child("likedBy").child(otherUserId).child(currentUserId).removeValue();
    }

    private void pushMatchNotification(String currentUserId, String otherUserId, String chatId) {
        String matchId = String.valueOf(System.currentTimeMillis());
        Map<String, Object> notification = new HashMap<>();
        notification.put("otherUserId", currentUserId);
        notification.put("chatId", chatId);
        notification.put("timestamp", System.currentTimeMillis());

        matchNotificationsRef.child(otherUserId).child(matchId).setValue(notification)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        handleDatabaseError(task.getException(), "Error pushing match notification");
                    }
                });
    }

    private void handleDatabaseError(Exception exception, String message) {
        String errorMessage = exception != null ? message + ": " + exception.getMessage() : message;
        Log.e(TAG, errorMessage);
        fragment.showError(errorMessage);
        isLoading = false;
    }
}