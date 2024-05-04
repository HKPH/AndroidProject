package com.example.cookingapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.R;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class ChangePasswordActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonChangePassword;
    private FirebaseAuth mAuth;
    private ImageButton buttonBack;


    private static final String ERROR_EMPTY_FIELDS = "Vui lòng nhập đầy đủ thông tin";
    private static final String ERROR_PASSWORD_MISMATCH = "Mật khẩu xác nhận không khớp";
    private static final String SUCCESS_PASSWORD_CHANGED = "Mật khẩu đã được thay đổi";
    private static final String ERROR_PASSWORD_MATCH = "Mật khẩu trùng với mật khẩu cũ";


    private static final String ERROR_PASSWORD_CHANGE_FAILED = "Thay đổi mật khẩu thất bại";
    private static final String ERROR_USER_NOT_FOUND = "Không tìm thấy người dùng hiện tại";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        initializeViews();
        initializeFirebase();
        setupClickListeners();


    }

    private void initializeViews() {

        editTextOldPassword = findViewById(R.id.edit_text_old_password);
        editTextNewPassword = findViewById(R.id.edit_text_new_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        buttonChangePassword = findViewById(R.id.button_change_password);
        buttonBack = findViewById(R.id.button_back);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }
    private void setupClickListeners(){
        buttonChangePassword.setOnClickListener(v ->
        {
            progressDialog.show();
            changePassword();
        });
        buttonBack.setOnClickListener(v -> onBackPressed());

    }
    private void changePassword() {
        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            DialogUtils.showErrorToast(this, ERROR_EMPTY_FIELDS);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            DialogUtils.showErrorToast(this, ERROR_PASSWORD_MISMATCH);
            return;
        }
        if (!newPassword.equals(oldPassword)) {
            DialogUtils.showErrorToast(this, ERROR_PASSWORD_MATCH);
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            DialogUtils.showSuccessToast(this, SUCCESS_PASSWORD_CHANGED);
                            clearEditTextFields();

                        } else {
                            progressDialog.dismiss();
                            DialogUtils.showErrorToast(this, ERROR_PASSWORD_CHANGE_FAILED);

                        }
                    }).addOnFailureListener(e ->
                    {
                        progressDialog.dismiss();
                        DialogUtils.showErrorToast(this, e.getMessage());
                    });
        } else {
            DialogUtils.showErrorToast(this, ERROR_USER_NOT_FOUND);
            progressDialog.dismiss();

        }
    }

    private void clearEditTextFields() {
        editTextOldPassword.setText("");
        editTextNewPassword.setText("");
        editTextConfirmPassword.setText("");
    }
}
