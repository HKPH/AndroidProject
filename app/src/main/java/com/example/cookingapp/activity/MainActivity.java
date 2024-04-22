package com.example.cookingapp.activity;

import android.app.NotificationManager;
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

    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNavigationView = findViewById(R.id.navigation);
        FragmentManager fragmentManager = getSupportFragmentManager();
        BottomNavigationManager bottomNavigationManager = new BottomNavigationManager(this, mBottomNavigationView, fragmentManager);
        mBottomNavigationView.setSelectedItemId(R.id.home);


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//
//                requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 1);
//
//            }
//            else {
//
//            }
//        }



    }
    private void checkUserLoginStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Người dùng chưa đăng nhập, chuyển hướng tới màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            getFCMToken();

            //Thêm chuyển hướng chọn nơi truy cập dựa theo user(user thì có recipeId) hoặc admin

        }

    }
    void getFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid=user.getUid();
                String token = task.getResult();
                FirebaseFirestore.getInstance().collection("users").document(uid).update("fcmToken",token);

            }
        });
    }



}
