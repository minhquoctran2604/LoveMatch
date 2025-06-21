package vn.edu.tlu.cse.lovematch.view.activity.signup;

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
    }

    private void updateUserGender(String gender) {
        // TODO: Cập nhật thông tin giới tính của người dùng vào database
        Toast.makeText(this, "Đã chọn giới tính: " + gender, Toast.LENGTH_SHORT).show();
        finish(); // Hoàn tất quá trình đăng ký và đóng activity
    }
}
