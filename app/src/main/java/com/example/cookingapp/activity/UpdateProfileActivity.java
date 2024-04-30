package com.example.cookingapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cookingapp.R;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private ImageView imageViewProfile;
    private EditText editTextName;
    private EditText editTextPhone, editTextEmail;
    private Button buttonUpdateProfile, buttonLogout;
    private TextView textViewChangePassword;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirestore;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        initializeViews();
        setupListeners();
        initializeFirebase();

        if (mUser != null) {
            loadUserData();
        }
    }

    private void initializeViews() {
        imageViewProfile = findViewById(R.id.image_view_profile);
        editTextName = findViewById(R.id.edit_text_name);
        editTextPhone = findViewById(R.id.edit_text_phone);
        editTextEmail = findViewById(R.id.edit_text_email);
        buttonUpdateProfile = findViewById(R.id.button_update_profile);
        textViewChangePassword = findViewById(R.id.text_view_change_password);
    }

    private void setupListeners() {

        buttonUpdateProfile.setOnClickListener(v -> updateProfile());
        textViewChangePassword.setOnClickListener(v -> navigateToChangePassword());
        imageViewProfile.setOnClickListener(v -> selectImage());
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
    }

    private void loadUserData() {
        mFirestore.collection("users").document(mUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String photoUrl = document.getString("photo");

                            editTextName.setText(name);
                            editTextEmail.setText(email);
                            editTextPhone.setText(phone);
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Glide.with(this).load(photoUrl).into(imageViewProfile);
                            }
                        } else {
                            DialogUtils.showErrorToast(this, "Không tìm thấy thông tin người dùng");
                        }
                    } else {
                        DialogUtils.showErrorToast(this, "Lỗi khi tải thông tin người dùng");
                    }
                });
    }

    private void updateProfile() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty()) {
            editTextName.setError("Vui lòng nhập tên");
            editTextName.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("Vui lòng nhập số điện thoại");
            editTextPhone.requestFocus();
            return;
        }
        progressDialog.show();
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("phone", phone);
        uploadImageToFirebase(imageUri);
        mFirestore.collection("users").document(mUser.getUid())
                .set(user, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DialogUtils.showSuccessToast(this, "Cập nhật thông tin thành công");
                    } else {
                        DialogUtils.showErrorToast(this, "Lỗi khi cập nhật thông tin");
                    }
                });
        progressDialog.dismiss();
    }

    private void navigateToChangePassword() {
        startActivity(new Intent(this, ChangePasswordActivity.class));
    }


    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageViewProfile);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + mUser.getUid());

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        mFirestore.collection("users").document(mUser.getUid())
                                .update("photo", imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Glide.with(this).load(imageUrl).into(imageViewProfile);
                                    DialogUtils.showSuccessToast(this, "Tải ảnh lên thành công");
                                })
                                .addOnFailureListener(e -> {
                                    DialogUtils.showErrorToast(this, "Lỗi khi tải ảnh lên");
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(this, "Lỗi khi tải ảnh lên");
                });
    }
}
