package vn.edu.tlu.cse.lovematch.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.controller.LikeController;
import vn.edu.tlu.cse.lovematch.model.data.qUser;
import vn.edu.tlu.cse.lovematch.model.repository.LikeRepository;

public class LikeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar loadingIndicator;
    private TextView errorText;
    private LikeController controller;
    private LikeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_like, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_likes);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        errorText = view.findViewById(R.id.error_text);

        adapter = new LikeAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        LikeRepository repository = new LikeRepository(); // Initialize repository
        controller = new LikeController(this, repository);
        adapter.setCurrentLocation(21.0278, 105.8342);
        // Example tab click listeners (assuming tabs are in the layout)
        view.findViewById(R.id.tab_likes).setOnClickListener(v -> controller.onLikesTabClicked());
        view.findViewById(R.id.tab_liked).setOnClickListener(v -> controller.onLikedTabClicked());
    }

    public void updateTabSelection(boolean isLikesTab) {
        // Update UI to reflect the selected tab (e.g., change tab indicator)
        Toast.makeText(getContext(), "Tab selected: " + (isLikesTab ? "Likes" : "Liked"), Toast.LENGTH_SHORT).show();
    }

    public void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        errorText.setVisibility(View.GONE);
    }

    public void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    public void updateUserList(List<qUser> users) {
        if (users == null) users = new ArrayList<>();
        adapter.setUsers(users);
        adapter.notifyDataSetChanged();
    }

    public void setCurrentLocation(double latitude, double longitude) {
        // Store or use location for UI (e.g., display current location)
    }

    public String getCurrentUserId() {
        // Assume FirebaseAuth is used
        return "current_user_id"; // Replace with actual implementation
    }

    public double getCurrentLatitude() {
        return 0.0; // Replace with actual latitude
    }

    public double getCurrentLongitude() {
        return 0.0; // Replace with actual longitude
    }

    public void navigateToUserProfile(String userId) {
        // Navigate to user profile (e.g., using Navigation component)
        Toast.makeText(getContext(), "Navigating to profile of user: " + userId, Toast.LENGTH_SHORT).show();
    }

    // Placeholder adapter
    private static class LikeAdapter extends RecyclerView.Adapter<LikeAdapter.ViewHolder> {
        private List<qUser> users = new ArrayList<>();
        private double currentLatitude = 0.0;
        private double currentLongitude = 0.0;

        public void setUsers(List<qUser> users) {
            this.users = users;
        }

        public void setCurrentLocation(double latitude, double longitude) {
            this.currentLatitude = latitude;
            this.currentLongitude = longitude;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            qUser user = users.get(position);
            holder.nameText.setText(user.getName());
            holder.ageText.setText("Age: " + calculateAge(user.getDateOfBirth()));
            double distance = calculateDistance(currentLatitude, currentLongitude, user.getLatitude(), user.getLongitude());
            holder.distanceText.setText(String.format("Distance: %.2f km", distance));
            holder.residenceText.setText("Residence: " + (user.getResidence() != null ? user.getResidence() : "N/A"));
        }

        private int calculateAge(String dateOfBirth) {
            if (dateOfBirth == null || dateOfBirth.isEmpty()) return -1;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date dob = sdf.parse(dateOfBirth);
                if (dob == null) return -1;
                Calendar dobCalendar = Calendar.getInstance();
                dobCalendar.setTime(dob);
                Calendar today = Calendar.getInstance();
                int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }
                return age;
            } catch (ParseException e) {
                return -1;
            }
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

        @Override
        public int getItemCount() {
            return users.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, ageText, distanceText, residenceText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.text_name);
                ageText = itemView.findViewById(R.id.text_age);
                distanceText = itemView.findViewById(R.id.text_distance);
                residenceText = itemView.findViewById(R.id.text_residence);
            }
        }
    }
}
