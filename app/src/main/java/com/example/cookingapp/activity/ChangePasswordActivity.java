package com.example.cookingapp.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.R;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonChangePassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initializeViews();
        initializeFirebase();

        buttonChangePassword.setOnClickListener(v -> changePassword());
    }

    private void initializeViews() {
        editTextOldPassword = findViewById(R.id.edit_text_old_password);
        editTextNewPassword = findViewById(R.id.edit_text_new_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        buttonChangePassword = findViewById(R.id.button_change_password);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void changePassword() {
        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            DialogUtils.showErrorToast(this, "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            DialogUtils.showErrorToast(this, "Mật khẩu xác nhận không khớp");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DialogUtils.showSuccessToast(this, "Mật khẩu đã được thay đổi");
                        } else {
                            DialogUtils.showErrorToast(this, "Thay đổi mật khẩu thất bại");
                        }
                    });
        } else {
            DialogUtils.showErrorToast(this, "Không tìm thấy người dùng hiện tại");
        }
    }
}
