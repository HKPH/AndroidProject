package com.example.cookingapp.activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.R;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class OpenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        Bundle extras = getIntent().getExtras();
        String recipeId = (extras != null) ? extras.getString("dataSend") : null;
        checkUserLoginStatus(recipeId);
    }

    private void checkUserLoginStatus(String recipeId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // User not logged in, redirect to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            getFCMToken();
            if (recipeId == null || recipeId.equals("admin")) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipeId);
                startActivity(intent);
            }
            finish();
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    String token = task.getResult();
                    FirebaseFirestore.getInstance().collection("users").document(uid).update("fcmToken", token);
                }
            }
        });
    }
}
