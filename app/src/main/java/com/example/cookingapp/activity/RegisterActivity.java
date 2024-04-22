package com.example.cookingapp.activity;

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

    private EditText editTextEmail, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
            DialogUtils.showErrorToast(RegisterActivity.this, "Please fill in all fields");
            return;
        }

        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            // Get FCM token
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful() && tokenTask.getResult() != null) {
                                            String fcmToken = tokenTask.getResult();
                                            // Save user info to Firestore
                                            saveUserInfoToFirestore(userId, email, fcmToken);
                                        } else {
                                            DialogUtils.showErrorToast(RegisterActivity.this, "Failed to get FCM token: " + tokenTask.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        DialogUtils.showErrorToast(RegisterActivity.this, "Registration failed");
                    }
                });
    }

    private void saveUserInfoToFirestore(String userId, String email, String fcmToken) {
        // Create a new User object
        User user = new User(email);
        user.setCheckAuth(false);
        user.setFcmToken(fcmToken);
        // Add user to Firestore
        db.collection("users")
                .document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DialogUtils.showSuccessToast(RegisterActivity.this, "Registration successful");
                        finish();
                    } else {
                        DialogUtils.showErrorToast(RegisterActivity.this, "Error: " + task.getException().getMessage());
                    }
                });
    }
}
