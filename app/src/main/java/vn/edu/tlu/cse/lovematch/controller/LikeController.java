package vn.edu.tlu.cse.lovematch.controller;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.lovematch.model.data.qUser;
import vn.edu.tlu.cse.lovematch.model.repository.LikeRepository;
import vn.edu.tlu.cse.lovematch.view.fragment.LikeFragment;

public class LikeController {
    private static final String TAG = "LikeController";
    private final LikeFragment fragment;
    private final LikeRepository repository;
    private List<qUser> usersWhoLikedMe;
    private List<qUser> usersILiked;
    private boolean isLikesTabSelected;
    private double maxDistance = Double.MAX_VALUE;
    private int minAge = 0;
    private int maxAge = Integer.MAX_VALUE;
    private String residenceFilter = null;

    public LikeController(LikeFragment fragment) {
        this.fragment = fragment;
        this.repository = new LikeRepository();
        this.usersWhoLikedMe = new ArrayList<>();
        this.usersILiked = new ArrayList<>();
        this.isLikesTabSelected = true;
        loadInitialData();
    }

    private void loadInitialData() {
        repository.getCurrentUserLocation(new LikeRepository.OnLocationListener() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                fragment.setCurrentLocation(latitude, longitude);
                onLikesTabClicked();
                onLikedTabClicked();
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
        loadUsersWhoLikedMe(null, 10);
    }

    public void onLikedTabClicked() {
        isLikesTabSelected = false;
        fragment.updateTabSelection(false);
        loadUsersILiked(null, 10);
    }

    public void loadUsersWhoLikedMe(String lastUserId, int pageSize) {
        repository.getUsersWhoLikedMe(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<qUser> users) {
                usersWhoLikedMe.clear();
                usersWhoLikedMe.addAll(users);
                applyFilterAndUpdate();
            }

            @Override
            public void onEmpty() {
                usersWhoLikedMe.clear();
                applyFilterAndUpdate();
                fragment.showError("No users who liked you found");
            }

            @Override
            public void onError(String error) {
                fragment.showError("Error loading users who liked you: " + error);
            }

            @Override
            public void onLoading() {
                // Handle loading state if needed
            }
        }, lastUserId, pageSize);
    }

    public void loadUsersILiked(String lastUserId, int pageSize) {
        repository.getUsersILiked(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(List<qUser> users) {
                usersILiked.clear();
                usersILiked.addAll(users);
                applyFilterAndUpdate();
            }

            @Override
            public void onEmpty() {
                usersILiked.clear();
                applyFilterAndUpdate();
                fragment.showError("No users you liked found");
            }

            @Override
            public void onError(String error) {
                fragment.showError("Error loading users you liked: " + error);
            }

            @Override
            public void onLoading() {
                // Handle loading state if needed
            }
        }, lastUserId, pageSize);
    }

    public void applyFilter(double maxDistance, int minAge, int maxAge, String residenceFilter) {
        this.maxDistance = maxDistance;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.residenceFilter = residenceFilter;
        applyFilterAndUpdate();
    }

    private void applyFilterAndUpdate() {
        List<qUser> filteredUsers = new ArrayList<>();
        List<qUser> sourceList = isLikesTabSelected ? usersWhoLikedMe : usersILiked;

        for (qUser user : sourceList) {
            boolean matches = true;

            // Apply distance filter (simplified, requires actual distance calculation)
            if (maxDistance != Double.MAX_VALUE) {
                // Assume latitude/longitude are available and calculate distance (placeholder)
                double distance = calculateDistance(fragment.getCurrentLatitude(), fragment.getCurrentLongitude(),
                        user.getLatitude(), user.getLongitude());
                if (distance > maxDistance) matches = false;
            }

            // Apply age filter (simplified, requires dateOfBirth parsing)
            if (minAge > 0 || maxAge < Integer.MAX_VALUE) {
                int age = calculateAge(user.getDateOfBirth());
                if (age < minAge || age > maxAge) matches = false;
            }

            // Apply residence filter
            if (residenceFilter != null && !residenceFilter.isEmpty()) {
                if (user.getResidence() == null || !user.getResidence().equalsIgnoreCase(residenceFilter)) {
                    matches = false;
                }
            }

            if (matches) filteredUsers.add(user);
        }

        fragment.updateUserList(filteredUsers);
    }

    // Placeholder methods (implement based on your qUser model)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Implement Haversine formula or use a library
        return 0.0; // Placeholder
    }

    private int calculateAge(String dateOfBirth) {
        // Implement age calculation from dateOfBirth (e.g., "YYYY-MM-DD")
        return 0; // Placeholder
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

    public void onLikeUser(qUser user) {
        Log.d(TAG, "onLikeUser: Liked user " + user.getName());
        // Implement like logic (e.g., update Firebase)
    }

    public void onDislikeUser(qUser user) {
        Log.d(TAG, "onDislikeUser: Disliked user " + user.getName());
        // Implement dislike logic (e.g., update Firebase)
    }

    public void onUserClicked(qUser user) {
        Log.d(TAG, "onUserClicked: User " + user.getName());
        // Navigate to profile or other action
    }

    public void loadMoreUsers() {
        String lastUserId = isLikesTabSelected ? (usersWhoLikedMe.isEmpty() ? null : usersWhoLikedMe.get(usersWhoLikedMe.size() - 1).getUid())
                : (usersILiked.isEmpty() ? null : usersILiked.get(usersILiked.size() - 1).getUid());
        if (isLikesTabSelected) {
            loadUsersWhoLikedMe(lastUserId, 10);
        } else {
            loadUsersILiked(lastUserId, 10);
        }
    }
}