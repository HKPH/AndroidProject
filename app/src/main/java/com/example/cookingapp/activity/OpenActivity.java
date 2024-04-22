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
        String data = (extras != null) ? extras.getString("dataSend") : null;
        checkUserLoginStatus(data);
    }


    private void checkUserLoginStatus(String data) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("Đã duyệt người dùng","ok");
        if (user == null) {
            // Người dùng chưa đăng nhập, chuyển hướng tới màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            getFCMToken();
            DialogUtils.showInfoToast(this,data);
            if (data==null||data.equals("admin")) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra("recipeId", data);
                startActivity(intent);
                finish();
            }

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