package vn.edu.tlu.cse.lovematch.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.qUser;

public class UserGridAdapter extends RecyclerView.Adapter<UserGridAdapter.ViewHolder> {
    private List<qUser> userList;
    private final double currentLatitude;
    private final double currentLongitude;
    private final OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClicked(qUser user);
    }

    public UserGridAdapter(List<qUser> userList, OnUserClickListener listener, double currentLatitude, double currentLongitude) {
        this.userList = userList != null ? userList : new ArrayList<>();
        this.onUserClickListener = listener;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < userList.size()) {
            qUser user = userList.get(position);
            holder.nameTextView.setText(user.getName());
            holder.bioTextView.setText(user.getBio() != null ? user.getBio() : "Chưa có tiểu sử");
            if (onUserClickListener != null) {
                holder.itemView.setOnClickListener(v -> onUserClickListener.onUserClicked(user));
            }
        }
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public void updateList(List<qUser> newList) {
        this.userList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView bioTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_name);
            bioTextView = itemView.findViewById(R.id.text_bio);
        }
    }
}