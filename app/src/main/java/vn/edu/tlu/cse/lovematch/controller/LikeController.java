package vn.edu.tlu.cse.lovematch.controller;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private double currentUserLatitude = 0.0;
    private double currentUserLongitude = 0.0;

    public LikeController(@NonNull LikeFragment fragment, @NonNull LikeRepository repository) {
        this.fragment = fragment;
        if (repository == null) {
            throw new IllegalArgumentException("LikeRepository cannot be null");
        }
        this.repository = repository;
        this.usersWhoLikedMe = new ArrayList<>();
        this.usersILiked = new ArrayList<>();
        this.isLikesTabSelected = true;
        loadInitialData();
    }

    private void loadInitialData() {
        repository.getCurrentUserLocation(new LikeRepository.OnLocationListener() {
            @Override
            public void onSuccess(double latitude, double longitude) {
                currentUserLatitude = latitude;
                currentUserLongitude = longitude;
                fragment.setCurrentLocation(latitude, longitude);
                loadUsersWhoLikedMe(null, 10);
                loadUsersILiked(null, 10);
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error getting current user location: " + error);
                fragment.showError("Không thể lấy vị trí hiện tại: " + error);
                loadUsersWhoLikedMe(null, 10);
                loadUsersILiked(null, 10);
            }
        });
    }

    public void onLikesTabClicked() {
        isLikesTabSelected = true;
        fragment.updateTabSelection(true);
        applyFilterAndUpdate();
        if (usersWhoLikedMe.isEmpty()) {
            loadUsersWhoLikedMe(null, 10);
        }
    }

    public void onLikedTabClicked() {
        isLikesTabSelected = false;
        fragment.updateTabSelection(false);
        applyFilterAndUpdate();
        if (usersILiked.isEmpty()) {
            loadUsersILiked(null, 10);
        }
    }

    public void loadUsersWhoLikedMe(String lastUserId, int pageSize) {
        if (repository == null) {
            Log.e(TAG, "Repository is null, cannot load users who liked me");
            fragment.showError("Lỗi hệ thống: Không thể tải dữ liệu.");
            return;
        }
        fragment.showLoading(true);
        repository.getUsersWhoLikedMe(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(@NonNull List<qUser> users) {
                if (lastUserId == null) {
                    usersWhoLikedMe.clear();
                }
                usersWhoLikedMe.addAll(users);
                applyFilterAndUpdate();
                fragment.showLoading(false);
            }

            @Override
            public void onEmpty() {
                if (lastUserId == null) {
                    usersWhoLikedMe.clear();
                }
                applyFilterAndUpdate();
                fragment.showError("Không tìm thấy người dùng nào thích bạn.");
                fragment.showLoading(false);
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error loading users who liked you: " + error);
                fragment.showError("Lỗi khi tải người dùng thích bạn: " + error);
                fragment.showLoading(false);
            }

            @Override
            public void onLoading() {
            }
        }, lastUserId, pageSize);
    }

    public void loadUsersILiked(String lastUserId, int pageSize) {
        if (repository == null) {
            Log.e(TAG, "Repository is null, cannot load users I liked");
            fragment.showError("Lỗi hệ thống: Không thể tải dữ liệu.");
            return;
        }
        fragment.showLoading(true);
        repository.getUsersILiked(new LikeRepository.OnResultListener() {
            @Override
            public void onSuccess(@NonNull List<qUser> users) {
                if (lastUserId == null) {
                    usersILiked.clear();
                }
                usersILiked.addAll(users);
                applyFilterAndUpdate();
                fragment.showLoading(false);
            }

            @Override
            public void onEmpty() {
                if (lastUserId == null) {
                    usersILiked.clear();
                }
                applyFilterAndUpdate();
                fragment.showError("Bạn chưa thích ai cả.");
                fragment.showLoading(false);
            }

            @Override
            public void onError(@NonNull String error) {
                Log.e(TAG, "Error loading users you liked: " + error);
                fragment.showError("Lỗi khi tải người dùng bạn đã thích: " + error);
                fragment.showLoading(false);
            }

            @Override
            public void onLoading() {
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

            if (maxDistance != Double.MAX_VALUE && currentUserLatitude != 0.0 && currentUserLongitude != 0.0) {
                double distance = calculateDistance(currentUserLatitude, currentUserLongitude,
                        user.getLatitude(), user.getLongitude());
                if (distance > maxDistance) {
                    matches = false;
                }
            }

            if ((minAge > 0 || maxAge < Integer.MAX_VALUE) && user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
                int age = calculateAge(user.getDateOfBirth());
                if (age == -1 || age < minAge || age > maxAge) {
                    matches = false;
                }
            }

            if (residenceFilter != null && !residenceFilter.trim().isEmpty()) {
                if (user.getResidence() == null || !user.getResidence().toLowerCase(Locale.getDefault()).contains(residenceFilter.toLowerCase(Locale.getDefault()))) {
                    matches = false;
                }
            }

            if (matches) {
                filteredUsers.add(user);
            }
        }
        fragment.updateUserList(filteredUsers);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int calculateAge(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return -1;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date dob = sdf.parse(dateOfBirth);
            if (dob == null) return -1;

            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);

            Calendar today = Calendar.getInstance();
            today.setTime(new Date()); // Use current date (June 24, 2025, 06:44 PM +07)

            int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date of birth: " + dateOfBirth, e);
            return -1;
        }
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

    public void onLikeUser(@NonNull qUser user) {
        Log.d(TAG, "onLikeUser: Liked user " + user.getName());
        if (repository != null) {
            repository.likeUser(user.getUid(), new LikeRepository.OnActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "User " + user.getName() + " liked successfully.");
                    if (isLikesTabSelected) {
                        loadUsersWhoLikedMe(null, 10);
                    } else {
                        loadUsersILiked(null, 10);
                    }
                }

                @Override
                public void onError(@NonNull String error) {
                    Log.e(TAG, "Error liking user " + user.getName() + ": " + error);
                    fragment.showError("Lỗi khi thích người dùng: " + error);
                }
            });
        }
    }

    public void onDislikeUser(@NonNull qUser user) {
        Log.d(TAG, "onDislikeUser: Disliked user " + user.getName());
        if (repository != null) {
            repository.dislikeUser(user.getUid(), new LikeRepository.OnActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "User " + user.getName() + " disliked successfully.");
                    if (isLikesTabSelected) {
                        loadUsersWhoLikedMe(null, 10);
                    } else {
                        loadUsersILiked(null, 10);
                    }
                }

                @Override
                public void onError(@NonNull String error) {
                    Log.e(TAG, "Error disliking user " + user.getName() + ": " + error);
                    fragment.showError("Lỗi khi bỏ thích người dùng: " + error);
                }
            });
        }
    }

    public void onUserClicked(@NonNull qUser user) {
        Log.d(TAG, "onUserClicked: User " + user.getName() + " (UID: " + user.getUid() + ")");
        fragment.navigateToUserProfile(user.getUid());
    }

    public void loadMoreUsers() {
        String lastUserId = null;
        if (isLikesTabSelected && !usersWhoLikedMe.isEmpty()) {
            lastUserId = usersWhoLikedMe.get(usersWhoLikedMe.size() - 1).getUid();
        } else if (!isLikesTabSelected && !usersILiked.isEmpty()) {
            lastUserId = usersILiked.get(usersILiked.size() - 1).getUid();
        }

        if (isLikesTabSelected) {
            loadUsersWhoLikedMe(lastUserId, 10);
        } else {
            loadUsersILiked(lastUserId, 10);
        }
    }
}