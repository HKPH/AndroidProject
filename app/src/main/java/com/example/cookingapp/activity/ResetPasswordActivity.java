package com.example.cookingapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.R;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private EditText editTextEmail;
    private Button buttonResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initializeViews();
        buttonResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.edit_text_email);
        buttonResetPassword = findViewById(R.id.button_reset_password);
    }


    private void resetPassword() {
        String emailAddress = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(emailAddress)) {
            editTextEmail.setError("Vui lòng nhập địa chỉ Email");
//            DialogUtils.showErrorToast(ResetPasswordActivity.this, "Vui lòng nhập địa chỉ email");
            return;
        }



        FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DialogUtils.showSuccessToast(ResetPasswordActivity.this, "Đã gửi email đặt lại mật khẩu");
                    } else {
                        DialogUtils.showErrorToast(ResetPasswordActivity.this, "Gửi email đặt lại mật khẩu thất bại");
                    }
                });
    }
}
