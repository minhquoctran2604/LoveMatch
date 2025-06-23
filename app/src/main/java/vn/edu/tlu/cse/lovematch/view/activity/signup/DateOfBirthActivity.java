package vn.edu.tlu.cse.lovematch.view.activity.signup;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Calendar;
import java.util.Locale;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.repository.UserRepository;

public class DateOfBirthActivity extends AppCompatActivity {

    private static final String TAG = "DateOfBirthActivity";
    private DatePicker datePicker;
    private MaterialButton btnNext;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_of_birth);

        userRepository = new UserRepository();

        datePicker = findViewById(R.id.date_picker);
        btnNext = findViewById(R.id.next_button);

        // Thiết lập ngày tối đa là 18 tuổi từ hôm nay
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -18);
        datePicker.setMaxDate(calendar.getTimeInMillis());

        // Thiết lập ngày tối thiểu là 100 tuổi từ hôm nay
        calendar.add(Calendar.YEAR, -82); // 100 - 18 = 82
        datePicker.setMinDate(calendar.getTimeInMillis());

        btnNext.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth() + 1; // Tháng bắt đầu từ 0
            int year = datePicker.getYear();

            // Kiểm tra tuổi
            Calendar today = Calendar.getInstance();
            Calendar birthDate = Calendar.getInstance();
            birthDate.set(year, month - 1, day);

            int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            if (age < 18) {
                Toast.makeText(this, "Bạn phải ít nhất 18 tuổi để sử dụng ứng dụng này", Toast.LENGTH_SHORT).show();
                return;
            }

            String dateOfBirth = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
            Log.d(TAG, "Selected date of birth: " + dateOfBirth);

            btnNext.setEnabled(false);

            userRepository.updateUserField("dob", dateOfBirth, new UserRepository.OnUserActionListener() {
                @Override
                public void onSuccess() {
                    Intent intent = new Intent(DateOfBirthActivity.this, vn.edu.tlu.cse.lovematch.view.activity.main.MainActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(DateOfBirthActivity.this, "Lỗi khi cập nhật ngày sinh: " + errorMessage, Toast.LENGTH_SHORT).show();
                    btnNext.setEnabled(true);
                }
            });
        });
    }
}
