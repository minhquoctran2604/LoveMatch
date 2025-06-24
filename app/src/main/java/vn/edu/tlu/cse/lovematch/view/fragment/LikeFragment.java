package vn.edu.tlu.cse.lovematch.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import vn.edu.tlu.cse.lovematch.R;

public class LikeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate a simple layout with just a TextView for now
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);

        // Find the TextView and set its text
        TextView textView = view.findViewById(R.id.text_placeholder);
        textView.setText("Like Fragment");

        return view;
    }
}
