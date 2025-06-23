package vn.edu.tlu.cse.lovematch.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.Snackbar;
import com.yuyakaido.android.cardstackview.CardStackView;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.controller.SwipeController;
import vn.edu.tlu.cse.lovematch.view.adapter.CardStackAdapter;
import vn.edu.tlu.cse.lovematch.model.data.User;

public class SwipeFragment extends Fragment {
    private CardStackView cardStackView;
    private SwipeController swipeController;
    private CardStackAdapter adapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe, container, false);

        cardStackView = view.findViewById(R.id.card_stack_view);
        View skipCircle = view.findViewById(R.id.skip_circle);
        View likeCircle = view.findViewById(R.id.like_circle);
        ImageButton skipButton = view.findViewById(R.id.skip_button);
        ImageButton likeButton = view.findViewById(R.id.like_button);
        TextView matchNotificationText = view.findViewById(R.id.match_notification_text);
        View matchNotificationLayout = view.findViewById(R.id.match_notification_layout);

        userList = new ArrayList<>();
        adapter = new CardStackAdapter(userList);
        cardStackView.setAdapter(adapter);

        swipeController = new SwipeController(this, cardStackView, skipCircle, likeCircle, skipButton, likeButton,
                matchNotificationText, matchNotificationLayout, NavHostFragment.findNavController(this), userList, adapter);
        swipeController.loadUsers();

        cardStackView.setCardEventListener(new com.yuyakaido.android.cardstackview.CardStackView.CardEventListener() {
            @Override
            public void onCardSwiped(Direction direction) { swipeController.handleCardSwiped(direction); }
            @Override public void onCardDragging(Direction direction, float ratio) {}
            @Override public void onCardRewound() {}
            @Override public void onCardCanceled() {}
            @Override public void onCardAppeared(View view, int position) {}
            @Override public void onCardDisappeared(View view, int position) {}
        });

        return view;
    }

    public void showSkipAnimation() {
        Snackbar.make(requireView(), "Đã bỏ qua", Snackbar.LENGTH_SHORT).show();
    }

    public void showLikeAnimation() {
        Snackbar.make(requireView(), "Đã thích", Snackbar.LENGTH_SHORT).show();
    }

    public void showSkipAnimationOnButton(ImageButton button) {
        button.setScaleX(1.2f);
        button.setScaleY(1.2f);
        button.postDelayed(() -> {
            button.setScaleX(1.0f);
            button.setScaleY(1.0f);
        }, 200);
    }

    public void showLikeAnimationOnButton(ImageButton button) {
        button.setScaleX(1.2f);
        button.setScaleY(1.2f);
        button.postDelayed(() -> {
            button.setScaleX(1.0f);
            button.setScaleY(1.0f);
        }, 200);
    }

    public void showMatchDialog(String name, String chatId, User user) {
        Snackbar.make(requireView(), "Match với " + name, Snackbar.LENGTH_LONG).show();
    }

    public void showUsers() {
        adapter.notifyDataSetChanged();
    }

    public void showError(String message) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
    }
}