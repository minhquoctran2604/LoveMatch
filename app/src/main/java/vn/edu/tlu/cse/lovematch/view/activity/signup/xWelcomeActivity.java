package vn.edu.tlu.cse.lovematch.view.activity.signup;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.view.activity.main.MainActivity;
import android.animation.ObjectAnimator;
import java.util.Random;

public class xWelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";

    private Button btnSignUp;
    private Button btnLogin;
    private FirebaseAuth auth;
    private RelativeLayout heartContainer;
    private Random random;
    private Handler handler;
    private static final int HEART_COUNT = 20; // Số lượng trái tim
    private static final int ANIMATION_DURATION = 2000; // Thời gian mỗi trái tim rơi xuống (ms)
    private static final int HEART_SPAWN_INTERVAL = 200; // Khoảng thời gian giữa các trái tim (ms)
    private static final int SHOW_BUTTONS_DELAY = 3000; // Thời gian chờ trước khi hiển thị nút (ms)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Setting content view from activity_welcomscreen.xml");
        setContentView(R.layout.activity_welcomscreen);

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Khởi tạo các view bằng findViewById
        btnSignUp = findViewById(R.id.btnSignUp);
        btnLogin = findViewById(R.id.btnLogin);
        heartContainer = findViewById(R.id.heart_container);

        // Kiểm tra các view
        if (btnSignUp == null || btnLogin == null) {
            Log.e(TAG, "onCreate: Button (btnSignUp or btnLogin) is null. Check activity_welcomscreen.xml layout.");
            return;
        }
        if (heartContainer == null) {
            Log.e(TAG, "onCreate: heartContainer is null. Check activity_welcomscreen.xml layout.");
        }

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(xWelcomeActivity.this, xSignInActivity.class));
        });        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(xWelcomeActivity.this, xSignUpActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra nếu người dùng đã đăng nhập (non-null) và chuyển màn hình.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            Log.d(TAG, "User is already logged in. Redirecting to MainActivity.");
            Intent intent = new Intent(xWelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Đóng WelcomeActivity để người dùng không thể quay lại
        } else {
            Log.d(TAG, "No user logged in. Showing Welcome screen.");
        }
    }

    private void startHeartAnimation() {
        Log.d(TAG, "startHeartAnimation: Waiting for heartContainer layout");
        heartContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove listener to avoid multiple calls
                heartContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (heartContainer.getWidth() > 30) {
                    Log.d(TAG, "startHeartAnimation: Layout ready, starting heart animation");
                    for (int i = 0; i < HEART_COUNT; i++) {
                        handler.postDelayed(() -> createHeart(), i * HEART_SPAWN_INTERVAL);
                    }
                } else {
                    Log.e(TAG, "startHeartAnimation: heartContainer width too small: " + heartContainer.getWidth());
                }
            }
        });
    }

    private void createHeart() {
        // Tạo một ImageView cho trái tim
        ImageView heartView = new ImageView(this);
        heartView.setImageResource(R.drawable.ic_heartwel);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200); // Kích thước trái tim
        params.leftMargin = random.nextInt(heartContainer.getWidth() - 30); // Vị trí ngẫu nhiên theo chiều ngang
        params.topMargin = -30; // Bắt đầu từ trên cùng
        heartView.setLayoutParams(params);

        // Thêm trái tim vào container
        heartContainer.addView(heartView);

        // Hiệu ứng rơi xuống
        ObjectAnimator moveDown = ObjectAnimator.ofFloat(heartView, "translationY", heartContainer.getHeight());
        moveDown.setDuration(ANIMATION_DURATION);
        moveDown.setRepeatCount(0);

        // Hiệu ứng mờ dần
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(heartView, "alpha", 1f, 0f);
        fadeOut.setDuration(ANIMATION_DURATION);
        fadeOut.setRepeatCount(0);

        // Bắt đầu hiệu ứng
        moveDown.start();
        fadeOut.start();

        // Xóa trái tim sau khi hiệu ứng hoàn tất
        moveDown.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d(TAG, "Heart animation started");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                heartContainer.removeView(heartView);
                Log.d(TAG, "Heart animation ended and view removed");
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void showButtonsWithFadeIn() {
        // Hiển thị nút với hiệu ứng mờ dần
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000); // Thời gian mờ dần: 1000ms
        fadeIn.setFillAfter(true); // Giữ trạng thái sau khi animation kết thúc

        btnSignUp.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);

        btnSignUp.startAnimation(fadeIn);
        btnLogin.startAnimation(fadeIn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng tạo trái tim khi Activity bị hủy
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            Log.d(TAG, "onDestroy: Stopped heart animation");
        }
    }
}