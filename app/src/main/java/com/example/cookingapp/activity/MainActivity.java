package com.example.cookingapp.activity;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cookingapp.R;
import com.example.cookingapp.activity.LoginActivity;
import com.example.cookingapp.activity.RecipeDetailActivity;
import com.example.cookingapp.utils.BottomNavigationManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import android.Manifest;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    private BottomNavigationView mBottomNavigationView;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        mBottomNavigationView = findViewById(R.id.navigation);
        FragmentManager fragmentManager = getSupportFragmentManager();
        BottomNavigationManager bottomNavigationManager = new BottomNavigationManager(this, mBottomNavigationView, fragmentManager);
        mBottomNavigationView.setSelectedItemId(R.id.home);

//        checkUserLoginStatus();

    }

    private void checkUserLoginStatus() {
//        progressDialog.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressDialog.dismiss();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
//            progressDialog.dismiss();
            getFCMToken();
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
                    Log.d("FCM Token", "Token: " + token);
                }
            } else {
                Log.e("FCM Token", "Failed to get token: " + task.getException());
            }
        });
    }
}
