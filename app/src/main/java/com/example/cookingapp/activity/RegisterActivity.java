package com.example.cookingapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.R;
import com.example.cookingapp.model.User;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        initializeViews();
        setClickListeners();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        registerButton = findViewById(R.id.button_register);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setClickListeners() {
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Vui lòng nhập địa chỉ Email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            saveUserInfoToFirestore(userId, email);
                        }
                    } else {
                        progressDialog.dismiss();
                        DialogUtils.showErrorToast(RegisterActivity.this, "Mật khẩu yêu cầu ít nhất 6 kí tự");
                    }
                });
    }

    private void saveUserInfoToFirestore(String userId, String email) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(tokenTask -> {
                    if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                        String fcmToken = tokenTask.getResult();
                        User user = new User(email);
                        user.setCheckAuth(false);
                        user.setFcmToken(fcmToken);
                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        DialogUtils.showSuccessToast(RegisterActivity.this, "Thành công");
                                        finish();
                                    } else {
                                        DialogUtils.showErrorToast(RegisterActivity.this, "Đăng kí thất bại");
                                    }
                                });
                    } else {
                        progressDialog.dismiss();
                        DialogUtils.showErrorToast(RegisterActivity.this, "Lấy token thất bại");
                    }
                });
    }
}
