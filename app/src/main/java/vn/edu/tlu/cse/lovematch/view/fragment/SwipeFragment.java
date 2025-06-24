package vn.edu.tlu.cse.lovematch.view.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.yuyakaido.android.cardstackview.CardStackView;
import java.util.ArrayList;
import java.util.List;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.controller.SwipeController;
import vn.edu.tlu.cse.lovematch.model.data.qUser;
import vn.edu.tlu.cse.lovematch.view.adapter.CardStackAdapter;

public class SwipeFragment extends Fragment {

    private ImageButton skipCircle, likeCircle;
    private Button likeButton;
    private CardStackView cardStackView;
    private ProgressBar loadingIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View errorLayout;
    private TextView errorMessage;
    private Button retryButton;
    private SwipeController controller;
    private List<qUser> userList;
    private CardStackAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_swipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        likeButton = view.findViewById(R.id.like_button);
        skipCircle = view.findViewById(R.id.skip_circle);
        likeCircle = view.findViewById(R.id.like_circle);
        cardStackView = view.findViewById(R.id.card_stack_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        errorLayout = view.findViewById(R.id.error_layout);
        errorMessage = view.findViewById(R.id.error_message);
        retryButton = view.findViewById(R.id.retry_button);

        userList = new ArrayList<>();
        adapter = new CardStackAdapter(userList); // Match constructor
        NavController navController = Navigation.findNavController(view);

        // Initialize controller (remove match_notification_text and layout for now)
        controller = new SwipeController(this, cardStackView, skipCircle, likeCircle,
                skipCircle.findViewById(R.id.skip_button), likeCircle.findViewById(R.id.like_button),
                null, null, navController, userList, adapter);

        controller.loadUsers();

        skipCircle.setOnClickListener(v -> cardStackView.swipe());
        likeCircle.setOnClickListener(v -> cardStackView.swipe());
        likeButton.setOnClickListener(v -> cardStackView.swipe());
        retryButton.setOnClickListener(v -> controller.loadUsers());
        swipeRefreshLayout.setOnRefreshListener(() -> controller.loadUsers());
    }

    public void showUsers() {
        loadingIndicator.setVisibility(View.GONE);
        cardStackView.setVisibility(View.VISIBLE);
    }

    public void showError(String message) {
        errorMessage.setText(message);
        errorLayout.setVisibility(View.VISIBLE);
        cardStackView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }

    public void showLikeAnimation() {
        Toast.makeText(getContext(), "Liked", Toast.LENGTH_SHORT).show();
    }

    public void showSkipAnimation() {
        Toast.makeText(getContext(), "Skipped", Toast.LENGTH_SHORT).show();
    }

    public void showLikeAnimationOnButton(ImageButton button) {
        // Placeholder
    }

    public void showSkipAnimationOnButton(ImageButton button) {
        // Placeholder
    }

    public void showMatchDialog(String userName, String chatId, qUser otherUser) {
        Toast.makeText(getContext(), "Matched with " + userName, Toast.LENGTH_SHORT).show();
    }

    private void playSound() {
        if (getContext() != null) {
            // Nếu bạn có file swipe_sound2.mp3 trong res/raw thì giữ dòng dưới, nếu không thì comment lại
            // MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.swipe_sound2);
            // if (mediaPlayer != null) {
            //     mediaPlayer.start();
            //     mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            // }
        }
    }
}
