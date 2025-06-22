package vn.edu.tlu.cse.lovematch.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.data.xUser;
import vn.edu.tlu.cse.lovematch.model.repository.xUserRepository;

public class xSelectGenderActivity extends AppCompatActivity {

    private RadioGroup genderRadioGroup;
    private Button confirmButton;
    private FirebaseUser currentUser;
    private xUserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_gender);

        // Khởi tạo Firebase Auth và lấy người dùng hiện tại
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish(); // Nếu không có người dùng, đóng activity
            return;
        }

        userRepository = new xUserRepository();

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
            String gender = radioButton.getText().toString();
            updateUserGender(gender);
        });
    }    private void updateUserGender(String gender) {
        Toast.makeText(this, "Đang cập nhật giới tính...", Toast.LENGTH_SHORT).show();
        
        userRepository.updateUserField("gender", gender, new xUserRepository.OnUserActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(xSelectGenderActivity.this, "Đã chọn giới tính: " + gender, Toast.LENGTH_SHORT).show();
                // Chuyển đến activity tiếp theo - chọn giới tính ưa thích
                Intent intent = new Intent(xSelectGenderActivity.this, xPreferGenderActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(xSelectGenderActivity.this, "Lỗi khi cập nhật giới tính: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
