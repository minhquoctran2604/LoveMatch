package vn.edu.tlu.cse.lovematch.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.User;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {
    private List<User> userList;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    public CardStackAdapter(List<User> userList) {
        this.userList = userList != null ? userList : new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < userList.size()) {
            User user = userList.get(position);
            holder.nameTextView.setText(user.getName());
            holder.bioTextView.setText(user.getBio() != null ? user.getBio() : "Chưa có tiểu sử");
            // Sử dụng Glide để tải ảnh đầu tiên trong danh sách photos (nếu có)
            if (user.getPhotos() != null && !user.getPhotos().isEmpty()) {
                Glide.with(holder.itemView.getContext()).load(user.getPhotos().get(0)).into(holder.profileImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public void setCurrentUserLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView bioTextView;
        public ImageView profileImage;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_name);
            bioTextView = itemView.findViewById(R.id.text_bio);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}