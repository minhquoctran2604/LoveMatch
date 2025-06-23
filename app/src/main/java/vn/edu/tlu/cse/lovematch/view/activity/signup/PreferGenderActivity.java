package vn.edu.tlu.cse.lovematch.view.activity.signup;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import vn.edu.tlu.cse.lovematch.model.repository.UserRepository;
import vn.edu.tlu.cse.lovematch.R;

public class PreferGenderActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private MaterialButton btnAll;
    private MaterialButton btnMale;
    private MaterialButton btnFemale;
    private MaterialButton btnNext;
    private String selectedPreferredGender = null;
    private int selectedStrokeWidth;
    private ColorStateList selectedStrokeColor;
    private final int defaultStrokeWidth = 0;
    private final float SELECTED_ALPHA = 1.0f;
    private final float UNSELECTED_ALPHA = 0.65f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefer_gender);

        userRepository = new UserRepository();

        btnAll = findViewById(R.id.all_button);
        btnMale = findViewById(R.id.male_button);
        btnFemale = findViewById(R.id.female_button);
        btnNext = findViewById(R.id.next_button);

        selectedStrokeWidth = getResources().getDimensionPixelSize(R.dimen.selected_stroke_width);
        selectedStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary));

        View.OnClickListener onGenderSelected = v -> {
            setButtonState(btnAll, false);
            setButtonState(btnMale, false);
            setButtonState(btnFemale, false);

            setButtonState((MaterialButton) v, true);

            int id = v.getId();
            if (id == R.id.all_button) {
                selectedPreferredGender = "Tất cả";
            } else if (id == R.id.male_button) {
                selectedPreferredGender = "Nam";
            } else if (id == R.id.female_button) {
                selectedPreferredGender = "Nữ";
            }

            btnNext.setEnabled(selectedPreferredGender != null);
        };

        btnAll.setOnClickListener(onGenderSelected);
        btnMale.setOnClickListener(onGenderSelected);
        btnFemale.setOnClickListener(onGenderSelected);

        btnNext.setOnClickListener(v -> {
            if (selectedPreferredGender != null) {
                btnNext.setEnabled(false);
                btnAll.setEnabled(false);
                btnMale.setEnabled(false);
                btnFemale.setEnabled(false);

                userRepository.updateUserField("preferredGender", selectedPreferredGender, new UserRepository.OnUserActionListener() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(PreferGenderActivity.this, vn.edu.tlu.cse.lovematch.view.activity.main.MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(PreferGenderActivity.this, "Lỗi khi cập nhật giới tính ưa thích: " + errorMessage, Toast.LENGTH_SHORT).show();
                        btnNext.setEnabled(true);
                        btnAll.setEnabled(true);
                        btnMale.setEnabled(true);
                        btnFemale.setEnabled(true);
                    }
                });
            }
        });
    }

    private void setButtonState(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setAlpha(SELECTED_ALPHA);
            button.setStrokeWidth(selectedStrokeWidth);
            button.setStrokeColor(selectedStrokeColor);
        } else {
            button.setAlpha(UNSELECTED_ALPHA);
            button.setStrokeWidth(defaultStrokeWidth);
            button.setStrokeColor(null);
        }
    }
}
