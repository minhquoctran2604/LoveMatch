package vn.edu.tlu.cse.lovematch.controller;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.Duration;
import com.yuyakaido.android.cardstackview.SwipeAnimationSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.qUser;
import vn.edu.tlu.cse.lovematch.view.adapter.nCardStackAdapter;
import vn.edu.tlu.cse.lovematch.view.fragment.nSwipeFragment;

public class nSwipeController {

    private static final String TAG = "SwipeController";
    private static final int PAGE_SIZE = 10;

    private final nSwipeFragment fragment;
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
    private final List<qUser> userList; // Initialized here
    private final nCardStackAdapter adapter;
    private final CardStackLayoutManager layoutManager;
    private Set<String> matchedUserIds;
    private Set<String> skippedUserIds;
    private boolean isSkipButtonPressed;
    private String lastUserId;
    private boolean isLoading;
    private final Stack<SwipeAction> swipeHistory;
    private qUser currentUser;

    private static class SwipeAction {
        qUser user;
        Direction direction;

        SwipeAction(qUser user, Direction direction) {
            this.user = user;
            this.direction = direction;
        }
    }

    public nSwipeController(nSwipeFragment fragment, CardStackView cardStackView, View skipCircle, View likeCircle,
                            ImageButton skipButton, ImageButton likeButton, TextView matchNotificationText,
                            View matchNotificationLayout, NavController navController,
                            List<qUser> userList, nCardStackAdapter adapter) {
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
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        this.userList = new ArrayList<>(); // Initialize userList here
        if (currentUserId == null) {
            Log.e(TAG, "No current user ID, initialization failed");
            fragment.showError("Không tìm thấy người dùng hiện tại");
        }
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
        SwipeAnimationSetting swipeAnimationSetting = new SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .build();
        layoutManager.setSwipeAnimationSetting(swipeAnimationSetting);

        skipButton.setOnClickListener(v -> {
            Log.d(TAG, "skipButton clicked: Performing swipe left");
            isSkipButtonPressed = true;
            SwipeAnimationSetting setting = new SwipeAnimationSetting.Builder()
                    .setDirection(Direction.Left)
                    .setDuration(Duration.Normal.duration)
                    .build();
            layoutManager.setSwipeAnimationSetting(setting);
            cardStackView.swipe();
            fragment.showSkipAnimationOnButton(skipButton);
        });

        likeButton.setOnClickListener(v -> {
            Log.d(TAG, "likeButton clicked: Performing swipe right");
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
        lastUserId = null;
        userList.clear();
        swipeHistory.clear();
        adapter.notifyDataSetChanged();
    }

    public void loadUsers() {
        if (isLoading) {
            Log.d(TAG, "loadUsers: Already loading, skipping...");
            return;
        }

        if (currentUser == null) {
            Log.d(TAG, "loadUsers: currentUser is null, waiting...");
            return;
        }

        isLoading = true;
        database.child("matches").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                matchedUserIds.clear();
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchedUserId = matchSnapshot.getKey();
                    if (matchedUserId != null) {
                        matchedUserIds.add(matchedUserId);
                    }
                }
                Log.d(TAG, "Matched users: " + matchedUserIds);

                Query query = database.child("qUser").orderByKey();
                if (lastUserId != null) {
                    query = query.startAfter(lastUserId);
                }
                query.limitToFirst(PAGE_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<qUser> newUsers = new ArrayList<>();
                        Log.d(TAG, "Loading users from Firebase (qUser node)...");

                        boolean hasOtherGender = false;
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            qUser user = userSnapshot.getValue(qUser.class);
                            if (user != null && "Khác".equals(user.getGender())) {
                                hasOtherGender = true;
                                break;
                            }
                        }
                        Log.d(TAG, "loadUsers: Has users with gender 'Khác': " + hasOtherGender);

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            qUser user = userSnapshot.getValue(qUser.class);
                            if (user != null && !user.getUid().equals(currentUserId)
                                    && !matchedUserIds.contains(user.getUid())
                                    && !skippedUserIds.contains(user.getUid())) {
                                String currentUserGender = currentUser.getGender() != null ? currentUser.getGender().trim() : "Chưa xác định";
                                String userGender = user.getGender() != null ? user.getGender().trim() : "Chưa xác định";

                                Log.d(TAG, "Current user gender: " + currentUserGender + ", User: " + (user != null ? user.getName() : "null") + ", Gender: " + userGender);

                                if ("Chưa xác định".equals(currentUserGender) || "Chưa xác định".equals(userGender)) {
                                    Log.w(TAG, "Skipping user " + (user != null ? user.getUid() : "null") + " - Gender not set properly");
                                    continue;
                                }

                                if (currentUserGender.equals(userGender)) {
                                    Log.d(TAG, "Skipping user " + user.getName() + " due to same gender");
                                    continue;
                                }

                                if ("Khac".equals(currentUserGender) && ("Nam".equals(userGender) || "Nu".equals(userGender))) {
                                    newUsers.add(user);
                                    Log.d(TAG, "Added user (Khác sees both Nam and Nữ): " + user.getName());
                                } else if ("Nữ".equals(currentUserGender) && ("Nam".equals(userGender) || ("Khac".equals(userGender) && hasOtherGender))) {
                                    newUsers.add(user);
                                    Log.d(TAG, "Added user (Nữ sees Nam and Khác): " + user.getName());
                                } else if ("Nam".equals(currentUserGender) && ("Nu".equals(userGender) || ("Khac".equals(userGender) && hasOtherGender))) {
                                    newUsers.add(user);
                                    Log.d(TAG, "Added user (Nam sees Nữ and Khác): " + user.getName());
                                } else {
                                    Log.d(TAG, "User skipped (gender mismatch): " + user.getName());
                                }
                            } else {
                                Log.d(TAG, "User skipped: " + (user == null ? "null user" :
                                        (matchedUserIds.contains(user.getUid()) ? "already matched" :
                                                (skippedUserIds.contains(user.getUid()) ? "already skipped" : "current user"))));
                            }
                        }

                        if (!newUsers.isEmpty()) {
                            lastUserId = newUsers.get(newUsers.size() - 1).getUid();
                            userList.addAll(newUsers);
                            adapter.notifyDataSetChanged();
                            fragment.showUsers();
                            Log.d(TAG, "Loaded " + newUsers.size() + " new users, total: " + userList.size());
                            for (qUser u : userList) {
                                Log.d(TAG, "User in list: " + u.getName() + ", Gender: " + u.getGender());
                            }
                        } else {
                            Log.w(TAG, "No users in this page, trying next page...");
                            if (lastUserId != null) {
                                loadUsers();
                            } else if (userList.isEmpty()) {
                                fragment.showError("Không có người dùng nào để hiển thị");
                            }
                        }
                        isLoading = false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error loading users: " + error.getMessage());
                        fragment.showError("Lỗi tải danh sách người dùng: " + error.getMessage());
                        isLoading = false;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading matches: " + error.getMessage());
                fragment.showError("Lỗi tải danh sách match: " + error.getMessage());
                isLoading = false;
            }
        });
    }

    private void loadCurrentUser() {
        database.child("qUser").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(qUser.class);
                if (currentUser != null) {
                    Log.d(TAG, "Current user loaded: " + currentUser.getName() + ", Gender: " + currentUser.getGender());
                    if (currentUser.isLocationEnabled()) {
                        adapter.setCurrentUserLocation(currentUser.getLatitude(), currentUser.getLongitude());
                    } else {
                        adapter.setCurrentUserLocation(0.0, 0.0);
                    }
                    loadUsers();
                } else {
                    Log.e(TAG, "Current user is null, snapshot: " + snapshot.toString());
                    fragment.showError("Không tìm thấy thông tin người dùng hiện tại");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> loadCurrentUser(), 2000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading current user: " + error.getMessage());
                fragment.showError("Lỗi tải thông tin người dùng: " + error.getMessage());
            }
        });
    }

    public void handleCardSwiped(Direction direction) {
        Log.d(TAG, "handleCardSwiped: Direction = " + direction.name() + ", isSkipButtonPressed = " + isSkipButtonPressed);
        if (userList.isEmpty()) {
            Log.w(TAG, "User list is empty during swipe");
            return;
        }

        int topPosition = layoutManager.getTopPosition();
        int index = topPosition;
        Log.d(TAG, "handleCardSwiped: topPosition = " + topPosition + ", userList size = " + userList.size());
        if (index < 0 || index >= userList.size()) {
            Log.e(TAG, "Invalid index: " + index + ", topPosition: " + topPosition + ", userList size = " + userList.size());
            if (topPosition <= 0 && !userList.isEmpty()) {
                Log.d(TAG, "handleCardSwiped: topPosition is " + topPosition + ", resetting CardStackView");
                adapter.notifyDataSetChanged();
                cardStackView.scheduleLayoutAnimation();
                return;
            }
            return;
        }

        qUser otherUser = userList.get(index);
        Log.d(TAG, "handleCardSwiped: Swiped user: " + otherUser.getName() + " (uid: " + otherUser.getUid() + ")");

        if (isSkipButtonPressed) {
            direction = Direction.Left;
            Log.d(TAG, "handleCardSwiped: Forcing direction to Left because skipButton was pressed");
            isSkipButtonPressed = false;
        }

        swipeHistory.push(new SwipeAction(otherUser, direction));

        if (direction == Direction.Right) {
            fragment.showLikeAnimation();
            likeUser(otherUser);
            checkForMatch(otherUser);
        } else if (direction == Direction.Left) {
            fragment.showSkipAnimation();
            skippedUserIds.add(otherUser.getUid());
            userList.remove(otherUser);
            database.child("likes").child(otherUser.getUid()).child(currentUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                database.child("likedBy").child(currentUserId).child(otherUser.getUid()).setValue(true)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "handleCardSwiped: Updated likedBy for user: " + otherUser.getUid());
                                            } else {
                                                Log.e(TAG, "handleCardSwiped: Error updating likedBy: " + task.getException().getMessage());
                                            }
                                        });
                            }
                            database.child("likes").child(otherUser.getUid()).child(currentUserId).removeValue();
                            adapter.notifyDataSetChanged();
                            cardStackView.scheduleLayoutAnimation();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error checking likes: " + error.getMessage());
                        }
                    });
        }
    }

    public void undoLastSwipe() {
        if (swipeHistory.isEmpty()) {
            Log.d(TAG, "undoLastSwipe: No actions to undo");
            Toast.makeText(fragment.getContext(), "Không có hành động để hoàn tác", Toast.LENGTH_SHORT).show();
            return;
        }

        SwipeAction lastAction = swipeHistory.pop();
        qUser user = lastAction.user;
        Direction direction = lastAction.direction;

        if (direction == Direction.Right) {
            database.child("likes").child(currentUserId).child(user.getUid()).removeValue();
            database.child("likedBy").child(user.getUid()).child(currentUserId).removeValue();
            matchedUserIds.remove(user.getUid());
        } else if (direction == Direction.Left) {
            skippedUserIds.remove(user.getUid());
            database.child("likedBy").child(currentUserId).child(user.getUid()).removeValue();
        }

        userList.add(0, user);
        adapter.notifyDataSetChanged();
        cardStackView.rewind();
        Toast.makeText(fragment.getContext(), "Đã hoàn tác", Toast.LENGTH_SHORT).show();
    }

    private void likeUser(qUser otherUser) {
        Log.d(TAG, "likeUser: Liking user: " + otherUser.getName() + " (uid: " + otherUser.getUid() + ")");
        database.child("likes").child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "likeUser: Successfully liked user: " + otherUser.getName());
                        database.child("likedBy").child(otherUser.getUid()).child(currentUserId).setValue(true)
                                .addOnCompleteListener(likedByTask -> {
                                    if (likedByTask.isSuccessful()) {
                                        Log.d(TAG, "likeUser: Successfully updated likedBy for user: " + otherUser.getUid());
                                    } else {
                                        Log.e(TAG, "likeUser: Error updating likedBy: " + likedByTask.getException().getMessage());
                                    }
                                });
                    } else {
                        Log.e(TAG, "likeUser: Error liking user: " + task.getException().getMessage());
                        fragment.showError("Lỗi khi thích: " + task.getException().getMessage());
                    }
                });
    }

    private void checkForMatch(qUser otherUser) {
        Log.d(TAG, "checkForMatch: Checking for match with user: " + otherUser.getName() + " (uid: " + otherUser.getUid() + ")");
        database.child("likes").child(otherUser.getUid()).child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "checkForMatch: Snapshot exists: " + snapshot.exists());
                        if (snapshot.exists()) {
                            Log.d(TAG, "Mutual like detected, match successful!");
                            String chatId = currentUserId.compareTo(otherUser.getUid()) < 0
                                    ? currentUserId + "_" + otherUser.getUid()
                                    : otherUser.getUid() + "_" + currentUserId;

                            Map<String, Object> matchInfo = new HashMap<>();
                            matchInfo.put("chatId", chatId);
                            matchInfo.put("timestamp", ServerValue.TIMESTAMP);

                            database.child("matches").child(currentUserId).child(otherUser.getUid()).setValue(matchInfo);
                            database.child("matches").child(otherUser.getUid()).child(currentUserId).setValue(matchInfo);

                            Map<String, Object> chatData = new HashMap<>();
                            chatData.put("participants/" + currentUserId, true);
                            chatData.put("participants/" + otherUser.getUid(), true);
                            chatData.put("lastMessage", "Bạn đã match với nhau!");
                            chatData.put("lastMessageTime", ServerValue.TIMESTAMP);
                            database.child("chats").child(chatId).updateChildren(chatData);

                            database.child("likes").child(currentUserId).child(otherUser.getUid()).removeValue();
                            database.child("likedBy").child(currentUserId).child(otherUser.getUid()).removeValue();
                            database.child("likes").child(otherUser.getUid()).child(currentUserId).removeValue();
                            database.child("likedBy").child(otherUser.getUid()).child(currentUserId).removeValue();

                            String matchedUserName = otherUser.getName() != null ? otherUser.getName() : "người dùng này";
                            Log.d(TAG, "Match successful with user: " + matchedUserName);

                            fragment.showMatchDialog(matchedUserName, chatId, otherUser);

                            pushMatchNotification(currentUserId, otherUser.getUid(), chatId);

                            matchedUserIds.add(otherUser.getUid());
                            userList.remove(otherUser);
                            adapter.notifyDataSetChanged();

                            Bundle refreshBundle = new Bundle();
                            refreshBundle.putBoolean("refresh", true);
                            fragment.getParentFragmentManager().setFragmentResult("refresh_chat_list", refreshBundle);
                        } else {
                            Log.d(TAG, "No mutual like found with user: " + otherUser.getName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking match: " + error.getMessage());
                        fragment.showError("Lỗi kiểm tra match: " + error.getMessage());
                    }
                });
    }

    private void pushMatchNotification(String currentUserId, String otherUserId, String chatId) {
        String matchId = String.valueOf(System.currentTimeMillis());
        Map<String, Object> notification = new HashMap<>();
        notification.put("otherUserId", currentUserId);
        notification.put("chatId", chatId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("type", "new_match");
        notification.put("seen", false);

        matchNotificationsRef.child(otherUserId).child(matchId).setValue(notification)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "pushMatchNotification: Successfully pushed match notification to user: " + otherUserId);
                        database.child("notification_counts").child(otherUserId).child("unreadMatches")
                                .setValue(ServerValue.increment(1));
                    } else {
                        Log.e(TAG, "pushMatchNotification: Error pushing match notification: " + task.getException().getMessage());
                    }
                });
    }
}