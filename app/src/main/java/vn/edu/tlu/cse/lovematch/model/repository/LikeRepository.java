package vn.edu.tlu.cse.lovematch.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LikeRepository {
    private final DatabaseReference likesRef;

    public LikeRepository() {
        likesRef = FirebaseDatabase.getInstance().getReference().child("likes");
    }

    public void saveLike(String userId, String targetId) {
        likesRef.child(userId).child(targetId).setValue(true);
    }

    public void removeLike(String userId, String targetId) {
        likesRef.child(userId).child(targetId).removeValue();
    }
}