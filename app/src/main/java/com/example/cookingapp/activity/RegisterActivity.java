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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editTextEmail = findViewById(R.id.edit_email);
        editTextPassword = findViewById(R.id.edit_password);
        Button registerButton = findViewById(R.id.button_register);
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            DialogUtils.showErrorToast(RegisterActivity.this, "Hãy nhập đầy đủ thông tin");
            return;
        }

        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                            String fcmToken = tokenTask.getResult();
                                            saveUserInfoToFirestore(userId, email, fcmToken);
                                        } else {
//                                            DialogUtils.showErrorToast(RegisterActivity.this, "Failed to get FCM token: " + tokenTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        DialogUtils.showErrorToast(RegisterActivity.this, "Đăng kí thất bại");
                    }
                });
        progressDialog.dismiss();
    }

    private void saveUserInfoToFirestore(String userId, String email, String fcmToken) {
        User user = new User(email);
        user.setCheckAuth(false);
        user.setFcmToken(fcmToken);
        db.collection("users")
                .document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DialogUtils.showSuccessToast(RegisterActivity.this, "Thành công");
                        finish();
                    } else {
                        DialogUtils.showErrorToast(RegisterActivity.this, "Đăng kí thất bại" );
                    }
                });
    }
}
