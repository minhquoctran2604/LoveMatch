package vn.edu.tlu.cse.lovematch.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.qUser;

public class nCardStackAdapter extends RecyclerView.Adapter<nCardStackAdapter.ViewHolder> {

    private List<qUser> userList;
    private double currentLatitude;
    private double currentLongitude;

    public nCardStackAdapter(List<qUser> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        qUser user = userList.get(position);

        // Hiển thị tên và tuổi
        holder.userName.setText(user.getName());
        holder.userAge.setText(String.valueOf(user.getAge()));

        if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getPhotos().get(0))
                    .placeholder(R.drawable.gai1)
                    .error(R.drawable.gai1)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.gai1);
        }
        
        if (holder.userBio != null) {
            String bio = user.getDescription() != null && !user.getDescription().isEmpty() ? user.getDescription() : "";
            holder.userBio.setText(bio);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private String calculateDistance(double latitude, double longitude) {
        if (currentLatitude == 0 || currentLongitude == 0) {
            return "N/A";
        }

        android.location.Location currentLocation = new android.location.Location("");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);

        android.location.Location userLocation = new android.location.Location("");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        float distanceInMeters = currentLocation.distanceTo(userLocation);
        float distanceInKm = distanceInMeters / 1000;
        return String.format("%.1f KM", distanceInKm);
    }

    public void setCurrentUserLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;
        TextView userAge;
        TextView userBio;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.image);
            userName = itemView.findViewById(R.id.name);
            userAge = itemView.findViewById(R.id.age);
            userBio = itemView.findViewById(R.id.bio);
        }
    }
}