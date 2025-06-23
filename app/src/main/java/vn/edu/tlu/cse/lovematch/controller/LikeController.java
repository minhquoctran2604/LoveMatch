package vn.edu.tlu.cse.lovematch.controller;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import vn.edu.tlu.cse.lovematch.model.data.User;

public class LikeController {
    private final Context context;
    private final DatabaseReference likesRef;

    public LikeController(Context context) {
        this.context = context;
        this.likesRef = FirebaseDatabase.getInstance().getReference().child("likes");
    }

    public void onLikeClick(View view, String currentUserId, User otherUser) {
        if (otherUser == null || otherUser.getUid() == null || currentUserId == null) {
            Toast.makeText(context, "Dữ liệu người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        likesRef.child(currentUserId).child(otherUser.getUid()).setValue(true)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã thích " + otherUser.getName(), Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi thích", Toast.LENGTH_SHORT).show());
    }
}