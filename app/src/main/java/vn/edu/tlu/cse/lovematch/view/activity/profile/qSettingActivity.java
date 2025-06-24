package vn.edu.tlu.cse.lovematch.view.activity.profile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; 
import android.widget.Toast;
import androidx.annotation.NonNull; 
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat; 
import androidx.core.content.ContextCompat; 
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.view.activity.signup.qMapActivity;
import vn.edu.tlu.cse.lovematch.view.activity.signup.qSignInActivity;
import android.Manifest; 

public class qSettingActivity extends AppCompatActivity {

    private Button notificationButton;
    private Button locationButton;
    private TextView locationPermissionStatusText; 
    private Button enableLocationPermissionButton; 
    private Button changePasswordButton;
    private Button logoutButton;

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AppSettingsPrefs";
    private static final String NOTIFICATIONS_ENABLED_KEY = "notificationsEnabled";
    private static final String TAG = "SettingActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Khởi tạo các view
        notificationButton = findViewById(R.id.notification_button);
        locationButton = findViewById(R.id.location_button);
        locationPermissionStatusText = findViewById(R.id.location_permission_status_text); // Ánh xạ TextView
        enableLocationPermissionButton = findViewById(R.id.enable_location_permission_button); // Ánh xạ Button
        changePasswordButton = findViewById(R.id.change_password_button);
        logoutButton = findViewById(R.id.logout_button);

        // Cập nhật trạng thái nút thông báo khi khởi động
        updateNotificationButtonState();

        // Kiểm tra và cập nhật UI cho quyền vị trí
        checkLocationPermissionAndUI();

        // --- Xử lý sự kiện click ---
        notificationButton.setOnClickListener(v -> toggleNotifications());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật trạng thái quyền vị trí mỗi khi Activity trở lại foreground
        checkLocationPermissionAndUI();
    }

    private static final int REQUEST_CODE_LOCATION = 102; // Hằng số cho request code

    private void checkLocationPermissionAndUI() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Quyền đã được cấp
            locationPermissionStatusText.setVisibility(View.GONE);
            enableLocationPermissionButton.setVisibility(View.GONE);
            locationButton.setVisibility(View.VISIBLE);
            locationButton.setOnClickListener(v -> {
                Intent intent = new Intent(qSettingActivity.this, qMapActivity.class);
                intent.putExtra("fromSettings", true); // Gửi extra để báo rằng người dùng vào từ Settings
                startActivity(intent);
            });
        } else {
            // Quyền chưa được cấp
            locationPermissionStatusText.setVisibility(View.VISIBLE);
            enableLocationPermissionButton.setVisibility(View.VISIBLE);
            locationButton.setVisibility(View.GONE);
            enableLocationPermissionButton.setOnClickListener(v -> {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền vị trí đã được cấp.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền truy cập vị trí để hoạt động.", Toast.LENGTH_LONG).show();
            }
            // Cập nhật lại UI sau khi người dùng phản hồi yêu cầu quyền
            checkLocationPermissionAndUI();
        }
    }

    // --- Logic cho các chức năng ---

    private void updateNotificationButtonState() {
        boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
        notificationButton.setText(notificationsEnabled ? "Tắt thông báo" : "Bật thông báo");
    }

    private void toggleNotifications() {
        boolean currentStatus = sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true);
        boolean newStatus = !currentStatus;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NOTIFICATIONS_ENABLED_KEY, newStatus);
        editor.apply();

        Toast.makeText(qSettingActivity.this, newStatus ? "Đã bật thông báo" : "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
        updateNotificationButtonState();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        final EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        final EditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_new_password);

        builder.setTitle("Đổi Mật Khẩu");
        builder.setPositiveButton("Xác nhận", null);
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                etCurrentPassword.setError(null);
                etNewPassword.setError(null);
                etConfirmNewPassword.setError(null);

                String currentPassword = etCurrentPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

                if (TextUtils.isEmpty(currentPassword)) {
                    etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
                    etCurrentPassword.requestFocus();
                    return;
                }

                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null || user.getEmail() == null) {
                    Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
                Toast.makeText(qSettingActivity.this, "Đang xác thực...", Toast.LENGTH_SHORT).show();
                positiveButton.setEnabled(false);

                user.reauthenticate(credential)
                        .addOnCompleteListener(reauthTask -> {
                            positiveButton.setEnabled(true);

                            if (reauthTask.isSuccessful()) {
                                Log.d(TAG, "Re-authentication successful.");

                                boolean newPasswordValid = true;
                                if (TextUtils.isEmpty(newPassword)) {
                                    etNewPassword.setError("Vui lòng nhập mật khẩu mới");
                                    etNewPassword.requestFocus();
                                    newPasswordValid = false;
                                } else if (newPassword.length() < 6) {
                                    etNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
                                    etNewPassword.requestFocus();
                                    newPasswordValid = false;
                                } else if (TextUtils.isEmpty(confirmNewPassword)) {
                                    etConfirmNewPassword.setError("Vui lòng xác nhận mật khẩu mới");
                                    etConfirmNewPassword.requestFocus();
                                    newPasswordValid = false;
                                } else if (!newPassword.equals(confirmNewPassword)) {
                                    etConfirmNewPassword.setError("Mật khẩu xác nhận không khớp");
                                    etConfirmNewPassword.requestFocus();
                                    newPasswordValid = false;
                                }

                                if (newPasswordValid) {
                                    updatePasswordInFirebase(user, newPassword, dialog, positiveButton);
                                }
                            } else {
                                Log.w(TAG, "Re-authentication failed", reauthTask.getException());
                                if (reauthTask.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                                } else {
                                    etCurrentPassword.setError("Xác thực thất bại");
                                    Toast.makeText(qSettingActivity.this, "Lỗi: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                etCurrentPassword.requestFocus();
                            }
                        });
            });
        });

        dialog.show();
    }

    private void updatePasswordInFirebase(FirebaseUser user, String newPassword, AlertDialog dialog, Button positiveButton) {
        Toast.makeText(qSettingActivity.this, "Đang cập nhật mật khẩu...", Toast.LENGTH_SHORT).show();
        if (positiveButton != null) positiveButton.setEnabled(false);

        user.updatePassword(newPassword)
                .addOnCompleteListener(updateTask -> {
                    if (positiveButton != null) positiveButton.setEnabled(true);

                    if (updateTask.isSuccessful()) {
                        Log.d(TAG, "User password updated.");
                        Toast.makeText(qSettingActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Log.w(TAG, "Error updating password", updateTask.getException());
                        Toast.makeText(qSettingActivity.this, "Đổi mật khẩu thất bại: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(qSettingActivity.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(qSettingActivity.this, qSignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
