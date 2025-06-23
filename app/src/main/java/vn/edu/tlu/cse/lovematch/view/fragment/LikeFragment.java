package vn.edu.tlu.cse.lovematch.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.view.adapter.UserGridAdapter;
import vn.edu.tlu.cse.lovematch.model.data.User;

public class LikeFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserGridAdapter adapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_like, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        userList = new ArrayList<>();
        adapter = new UserGridAdapter(userList);
        recyclerView.setAdapter(adapter);

        userList.add(new User("1", "User 1", "user1@example.com", "Nam", "1995-05-15", "Hà Nội", null, 21.0278, 105.8342, "Thích du lịch", 30));
        userList.add(new User("2", "User 2", "user2@example.com", "Nữ", "1998-08-22", "TP.HCM", null, 10.7769, 106.7009, "Yêu âm nhạc", 27));
        adapter.notifyDataSetChanged();

        return view;
    }
}