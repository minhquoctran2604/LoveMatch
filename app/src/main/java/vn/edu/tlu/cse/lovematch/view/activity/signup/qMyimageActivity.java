package vn.edu.tlu.cse.lovematch.view.activity.signup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import vn.edu.tlu.cse.lovematch.R;
import vn.edu.tlu.cse.lovematch.model.repository.qUserRepository;

public class qMyimageActivity extends AppCompatActivity {

    private static final String TAG = "MyImageActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private ImageView selectedPhotoPreview;
    private qUserRepository qUserRepository;
    private StorageReference storageRef;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myimage);

        qUserRepository = new qUserRepository();
        storageRef = FirebaseStorage.getInstance().getReference("user_photos");

        selectedPhotoPreview = findViewById(R.id.selected_photo_preview);
        Button btnUploadPhoto = findViewById(R.id.upload_button);
        Button btnTakePhoto = findViewById(R.id.camera_button);
        Button btnNext = findViewById(R.id.next_button);
        Button btnSkip = findViewById(R.id.skip_button);

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                Log.d(TAG, "Image selected: " + uri.toString());
                displayImage(uri);
            }
        });

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (photoUri != null) {
                    Log.d(TAG, "Photo captured: " + photoUri.toString());
                    displayImage(photoUri);
                }
            }
        });

        btnUploadPhoto.setOnClickListener(v -> {
            Log.d(TAG, "Upload photo button clicked");
            pickImageLauncher.launch("image/*");
        });

        btnTakePhoto.setOnClickListener(v -> {
            Log.d(TAG, "Take photo button clicked");
            checkAndRequestCameraPermission();
        });

        btnNext.setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked");
            goToNextActivity();
        });

        btnSkip.setOnClickListener(v -> {
            Log.d(TAG, "Skip button clicked");
            goToNextActivity();
        });
    }

    private void displayImage(Uri imageUri) {
        if (imageUri != null && selectedPhotoPreview != null) {
            Log.d(TAG, "Displaying image: " + imageUri.toString());
            selectedPhotoPreview.setImageURI(imageUri);
        } else {
            Log.e(TAG, "Cannot display image - URI or ImageView is null");
        }
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission not granted, requesting...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Camera permission already granted");
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                openCamera();
            } else {
                Log.d(TAG, "Camera permission denied");
                Toast.makeText(this, "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file: ", ex);
                Toast.makeText(this, "Không thể tạo tệp để lưu ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                String authority = getPackageName() + ".fileprovider";
                try {
                    photoUri = FileProvider.getUriForFile(this, authority, photoFile);
                    Log.d(TAG, "Photo URI created: " + photoUri.toString());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    takePictureLauncher.launch(takePictureIntent);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error creating FileProvider URI: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tạo URI cho camera", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void goToNextActivity() {
        Intent intent = new Intent(qMyimageActivity.this, qMapActivity.class);
        startActivity(intent);
        finish();
    }
}
