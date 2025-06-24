package vn.edu.tlu.cse.lovematch.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.controller.LikeController;
import vn.edu.tlu.cse.lovematch.model.data.qUser;
import vn.edu.tlu.cse.lovematch.databinding.FragmentLikeBinding;
import vn.edu.tlu.cse.lovematch.view.adapter.UserGridAdapter;

public class LikeFragment extends Fragment {

    private static final String TAG = "LikeFragment";
    private static final String KEY_USERS_WHO_LIKED_ME = "usersWhoLikedMe";
    private static final String KEY_USERS_I_LIKED = "usersILiked";
    private static final String KEY_IS_LIKES_TAB_SELECTED = "isLikesTabSelected";
    private static final String KEY_MAX_DISTANCE = "maxDistance";
    private static final String KEY_MIN_AGE = "minAge";
    private static final String KEY_MAX_AGE = "maxAge";
    private static final String KEY_RESIDENCE_FILTER = "residenceFilter";
    private static final String KEY_FILTER_APPLIED = "filterApplied";

    private FragmentLikeBinding binding;
    private UserGridAdapter userAdapter;
    private List<qUser> userList;
    private List<qUser> usersWhoLikedMe;
    private List<qUser> usersILiked;
    private NavController navController;
    private LikeController controller;
    private DatabaseReference matchNotificationsRef;
    private ValueEventListener matchListener;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private boolean isLikesTabSelected = true;
    private boolean isFilterApplied = false;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLikeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        userList = new ArrayList<>();
        usersWhoLikedMe = new ArrayList<>();
        usersILiked = new ArrayList<>();
        userAdapter = new UserGridAdapter(userList, this::onUserClicked, currentLatitude, currentLongitude);
        binding.recyclerView.setAdapter(userAdapter);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        controller = new LikeController(this);
        if (savedInstanceState != null) {
            usersWhoLikedMe = savedInstanceState.getParcelableArrayList(KEY_USERS_WHO_LIKED_ME);
            usersILiked = savedInstanceState.getParcelableArrayList(KEY_USERS_I_LIKED);
            isLikesTabSelected = savedInstanceState.getBoolean(KEY_IS_LIKES_TAB_SELECTED, true);
            controller.applyFilter(
                    savedInstanceState.getDouble(KEY_MAX_DISTANCE, Double.MAX_VALUE),
                    savedInstanceState.getInt(KEY_MIN_AGE, 0),
                    savedInstanceState.getInt(KEY_MAX_AGE, Integer.MAX_VALUE),
                    savedInstanceState.getString(KEY_RESIDENCE_FILTER)
            );
            isFilterApplied = savedInstanceState.getBoolean(KEY_FILTER_APPLIED, false);
            if (!isLikesTabSelected) controller.onLikedTabClicked();
            userList = isLikesTabSelected ? usersWhoLikedMe : usersILiked;
            userAdapter.updateList(userList);
        } else {
            controller.onLikesTabClicked();
        }

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        matchNotificationsRef = FirebaseDatabase.getInstance().getReference("match_notifications").child(currentUserId);
        setupMatchListener();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_USERS_WHO_LIKED_ME, new ArrayList<>(controller.getUsersWhoLikedMe()));
        outState.putParcelableArrayList(KEY_USERS_I_LIKED, new ArrayList<>(controller.getUsersILiked()));
        outState.putBoolean(KEY_IS_LIKES_TAB_SELECTED, controller.isLikesTabSelected());
        outState.putDouble(KEY_MAX_DISTANCE, controller.getMaxDistance());
        outState.putInt(KEY_MIN_AGE, controller.getMinAge());
        outState.putInt(KEY_MAX_AGE, controller.getMaxAge());
        outState.putString(KEY_RESIDENCE_FILTER, controller.getResidenceFilter());
        outState.putBoolean(KEY_FILTER_APPLIED, isFilterApplied);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (matchNotificationsRef != null && matchListener != null) {
            matchNotificationsRef.removeEventListener(matchListener);
        }
    }

    private void setupMatchListener() {
        matchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot matchSnapshot : snapshot.getChildren()) {
                    String matchId = matchSnapshot.getKey();
                    String otherUserId = matchSnapshot.child("otherUserId").getValue(String.class);
                    String chatId = matchSnapshot.child("chatId").getValue(String.class);

                    if (otherUserId != null && chatId != null) {
                        DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
                        otherUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                qUser otherUser = userSnapshot.getValue(qUser.class);
                                if (otherUser != null) {
                                    String matchedUserName = otherUser.getName() != null ? otherUser.getName() : "Người dùng này";
                                    showMatchDialog(matchedUserName, chatId, otherUser);
                                }
                                matchNotificationsRef.child(matchId).removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error loading matched user: " + error.getMessage());
                                showError("Lỗi tải thông tin người dùng match: " + error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error listening for match notifications: " + error.getMessage());
                showError("Lỗi lắng nghe thông báo match: " + error.getMessage());
            }
        };
        matchNotificationsRef.addValueEventListener(matchListener);
    }

    private void showFilterDialog() {
        if (!isAdded() || getContext() == null) {
            Log.e(TAG, "Cannot show filter dialog: Fragment is not attached to an Activity");
            return;
        }
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_filter);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(params);

        EditText distanceFilter = dialog.findViewById(R.id.distance_filter);
        EditText ageMinFilter = dialog.findViewById(R.id.age_min_filter);
        EditText ageMaxFilter = dialog.findViewById(R.id.age_max_filter);
        EditText residenceFilter = dialog.findViewById(R.id.residence_filter);
        Button applyButton = dialog.findViewById(R.id.apply_filter_button);
        Button clearFilterButton = dialog.findViewById(R.id.clear_filter_button);

        if (applyButton == null || clearFilterButton == null) {
            Log.e(TAG, "Filter dialog views not found");
            return;
        }

        applyButton.setOnClickListener(v -> {
            double maxDistance = Double.MAX_VALUE;
            int minAge = 0;
            int maxAge = Integer.MAX_VALUE;
            String residence = residenceFilter.getText().toString().trim();

            try {
                String distanceStr = distanceFilter.getText().toString().trim();
                if (!distanceStr.isEmpty()) maxDistance = Double.parseDouble(distanceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Khoảng cách không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String minAgeStr = ageMinFilter.getText().toString().trim();
                if (!minAgeStr.isEmpty()) minAge = Integer.parseInt(minAgeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Tuổi tối thiểu không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String maxAgeStr = ageMaxFilter.getText().toString().trim();
                if (!maxAgeStr.isEmpty()) maxAge = Integer.parseInt(maxAgeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Tuổi tối đa không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            controller.applyFilter(maxDistance, minAge, maxAge, residence);
            isFilterApplied = true;
            Log.d(TAG, "Applied filter - isFilterApplied: " + isFilterApplied);
            dialog.dismiss();
        });

        clearFilterButton.setOnClickListener(v -> {
            controller.applyFilter(Double.MAX_VALUE, 0, Integer.MAX_VALUE, null);
            isFilterApplied = false;
            Log.d(TAG, "Cleared filter - isFilterApplied: " + isFilterApplied);
            dialog.dismiss();
        });

        dialog.show();
    }

    public void setCurrentLocation(double latitude, double longitude) {
        currentLatitude = latitude;
        currentLongitude = longitude;
        if (userAdapter != null) {
            userAdapter = new UserGridAdapter(userList, this::onUserClicked, currentLatitude, currentLongitude);
            binding.recyclerView.setAdapter(userAdapter);
        }
    }

    public void updateUserList(List<qUser> users) {
        Log.d(TAG, "updateUserList: Updating with " + (users != null ? users.size() : 0) + " users");
        if (users == null) users = new ArrayList<>();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new UserDiffCallback(userList, users));
        userList.clear();
        userList.addAll(users);

        if (isLikesTabSelected) {
            usersWhoLikedMe.clear();
            usersWhoLikedMe.addAll(users);
        } else {
            usersILiked.clear();
            usersILiked.addAll(users);
        }

        diffResult.dispatchUpdatesTo(userAdapter);
        isLoading = false;
        if (users.isEmpty() && isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "Không có người dùng nào để hiển thị", Toast.LENGTH_SHORT).show();
        }
    }

    public void showMatchDialog(String matchedUserName, String chatId, qUser otherUser) {
        Log.d(TAG, "showMatchDialog: Attempting to show match dialog for user: " + matchedUserName);
        if (!isAdded() || getContext() == null) {
            Log.e(TAG, "Cannot show match dialog: Fragment is not attached to an Activity");
            return;
        }

        Dialog matchDialog = new Dialog(getContext());
        matchDialog.setContentView(R.layout.match_dialog);

        TextView matchTitle = matchDialog.findViewById(R.id.match_title);
        ImageView currentUserImage = matchDialog.findViewById(R.id.current_user_image);
        ImageView otherUserImage = matchDialog.findViewById(R.id.other_user_image);
        Button sendMessageButton = matchDialog.findViewById(R.id.send_message_button);
        Button keepSwipingButton = matchDialog.findViewById(R.id.keep_swiping_button);

        if (matchTitle == null || currentUserImage == null || otherUserImage == null ||
                sendMessageButton == null || keepSwipingButton == null) {
            Log.e(TAG, "showMatchDialog: One or more views in match_dialog.xml are null");
            return;
        }

        String message = "Bạn và " + matchedUserName + " đã match thành công!";
        matchTitle.setText(message);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                qUser currentUser = snapshot.getValue(qUser.class);
                if (currentUser != null && currentUser.getPhotos() != null && !currentUser.getPhotos().isEmpty()) {
                    Glide.with(getContext())
                            .load(currentUser.getPhotos().get(0))
                            .placeholder(R.drawable.gai1)
                            .error(R.drawable.gai1)
                            .into(currentUserImage);
                } else {
                    currentUserImage.setImageResource(R.drawable.gai1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading current user: " + error.getMessage());
                currentUserImage.setImageResource(R.drawable.gai1);
            }
        });

        if (otherUser != null && otherUser.getPhotos() != null && !otherUser.getPhotos().isEmpty()) {
            Glide.with(getContext())
                    .load(otherUser.getPhotos().get(0))
                    .placeholder(R.drawable.gai1)
                    .error(R.drawable.gai1)
                    .into(otherUserImage);
        } else {
            otherUserImage.setImageResource(R.drawable.gai1);
        }

        sendMessageButton.setOnClickListener(v -> {
            Log.d(TAG, "Send message button clicked, navigating to chat with chatId: " + chatId);
            matchDialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putString("chatId", chatId);
            if (navController != null) {
                navController.navigate(R.id.action_likeFragment_to_listChatFragment, bundle);
            } else {
                Log.e(TAG, "NavController is null");
            }
        });

        keepSwipingButton.setOnClickListener(v -> {
            Log.d(TAG, "Keep swiping button clicked, dismissing dialog");
            matchDialog.dismiss();
        });

        matchDialog.show();
    }

    public void showError(String error) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Cannot show error: Fragment is not attached to an Activity");
        }
    }

    private void onUserClicked(qUser user) {
        if (controller != null) {
            controller.onUserClicked(user);
        } else {
            Log.e(TAG, "Controller is null in onUserClicked");
        }
    }

    private static class UserDiffCallback extends DiffUtil.Callback {
        private final List<qUser> oldList;
        private final List<qUser> newList;

        UserDiffCallback(List<qUser> oldList, List<qUser> newList) {
            this.oldList = oldList != null ? oldList : new ArrayList<>();
            this.newList = newList != null ? newList : new ArrayList<>();
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getUid().equals(newList.get(newItemPosition).getUid());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            qUser oldUser = oldList.get(oldItemPosition);
            qUser newUser = newList.get(newItemPosition);
            return oldUser.getName() != null && oldUser.getName().equals(newUser.getName()) &&
                    oldUser.getBio() != null && oldUser.getBio().equals(newUser.getBio()) &&
                    oldUser.getLatitude() == newUser.getLatitude() &&
                    oldUser.getLongitude() == newUser.getLongitude();
        }
    }
}