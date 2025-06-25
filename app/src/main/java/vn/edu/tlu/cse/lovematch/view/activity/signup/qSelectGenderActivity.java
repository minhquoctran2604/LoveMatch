package vn.edu.tlu.cse.lovematch.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.repository.qUserRepository;

public class qSelectGenderActivity extends AppCompatActivity {

    private static final String TAG = "qSelectGenderActivity";

    private RadioGroup genderRadioGroup;
    private Button confirmButton;
    private FirebaseUser currentUser;
    private qUserRepository qUserRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_gender);

        // Khởi tạo Firebase Auth và lấy người dùng hiện tại
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No current user found, finishing activity");
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        qUserRepository = new qUserRepository();

        // Khởi tạo các view
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        confirmButton = findViewById(R.id.confirm_button);

        // Handle button click event
        confirmButton.setOnClickListener(v -> {
            int selectedId = genderRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton radioButton = findViewById(selectedId);
            String gender = radioButton.getText().toString().trim(); // Loại bỏ khoảng trắng thừa
            if ("Nam".equals(gender) || "Nữ".equals(gender) || "Khác".equals(gender)) {
                updateUserGender(gender);
            } else {
                Toast.makeText(this, "Giới tính không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserGender(String gender) {
        if (currentUser == null) {
            Log.e(TAG, "Current user is null during update");
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Updating gender for user: " + currentUser.getUid() + " to " + gender);

        Toast.makeText(this, "Đang cập nhật giới tính...", Toast.LENGTH_SHORT).show();

        // Gọi updateUserField mà không cần truyền userId, vì nó tự lấy từ FirebaseAuth
        qUserRepository.updateUserField("gender", gender, new qUserRepository.OnUserActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Gender updated successfully for user: " + currentUser.getUid());
                Toast.makeText(qSelectGenderActivity.this, "Đã chọn giới tính: " + gender, Toast.LENGTH_SHORT).show();
                // Chuyển đến activity chọn giới tính ưa thích
                Intent intent = new Intent(qSelectGenderActivity.this, qPreferGenderActivity.class);
                intent.putExtra("userGender", gender); // Truyền giới tính để sử dụng sau
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to update gender: " + errorMessage);
                Toast.makeText(qSelectGenderActivity.this, "Lỗi khi cập nhật giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dọn dẹp nếu cần
    }
}