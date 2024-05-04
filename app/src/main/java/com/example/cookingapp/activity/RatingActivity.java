package com.example.cookingapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.R;
import com.example.cookingapp.adapter.RatingAdapter;
import com.example.cookingapp.model.Rating;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RatingActivity extends AppCompatActivity {
    private TextView textHeader;
    private RecyclerView recyclerView;
    private RatingAdapter ratingAdapter;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        initializeViews();
        initializeFirebase();
        setupRecyclerView();

        String recipeId = getIntent().getStringExtra("recipeId");
        getRatingsForRecipe(recipeId);
    }

    private void initializeViews() {
        textHeader = findViewById(R.id.text_header);
        recyclerView = findViewById(R.id.recycler_ratings);
        progressDialog = new ProgressDialog(this);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getRatingsForRecipe(String recipeId) {
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        CollectionReference ratingsRef = db.collection("ratings");
        ratingsRef.whereEqualTo("recipeId", recipeId)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        handleRatingsResult(task.getResult());
                    } else {
                        DialogUtils.showErrorToast(RatingActivity.this, "Error getting ratings");
                    }
                });
    }

    private void handleRatingsResult(QuerySnapshot querySnapshot) {
        if (querySnapshot != null && !querySnapshot.isEmpty()) {
            List<Rating> ratings = querySnapshot.toObjects(Rating.class);
            showRatings(ratings);
        } else {
            showNoRatingsMessage();
        }
    }

    private void showRatings(List<Rating> ratings) {
        ratingAdapter = new RatingAdapter(ratings);
        recyclerView.setAdapter(ratingAdapter);
    }

    private void showNoRatingsMessage() {
        textHeader.setText("Chưa có đánh giá");
    }
}
