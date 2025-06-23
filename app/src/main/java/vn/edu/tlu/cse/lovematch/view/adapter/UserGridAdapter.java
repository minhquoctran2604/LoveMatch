package vn.edu.tlu.cse.lovematch.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.User;

public class UserGridAdapter extends RecyclerView.Adapter<UserGridAdapter.ViewHolder> {
    private List<User> userList;

    public UserGridAdapter(List<User> userList) {
        this.userList = userList != null ? userList : new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < userList.size()) {
            User user = userList.get(position);
            holder.nameTextView.setText(user.getName());
            holder.bioTextView.setText(user.getBio() != null ? user.getBio() : "Chưa có tiểu sử");
        }
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
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