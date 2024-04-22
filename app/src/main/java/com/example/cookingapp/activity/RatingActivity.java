package com.example.cookingapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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
        db = FirebaseFirestore.getInstance();

        textHeader = findViewById(R.id.text_header);
        recyclerView = findViewById(R.id.recycler_ratings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        String recipeId = getIntent().getStringExtra("recipeId");
        getRatingsForRecipe(recipeId);
    }

    private void getRatingsForRecipe(String recipeId) {
        progressDialog.show();

        CollectionReference ratingsRef = db.collection("ratings");
        ratingsRef.whereEqualTo("recipeId", recipeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Rating> ratings = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (Rating rating : querySnapshot.toObjects(Rating.class)) {
                                ratings.add(rating);
                            }
                            ratingAdapter = new RatingAdapter(ratings);
                            recyclerView.setAdapter(ratingAdapter);
                        } else {
                            showNoRatingsMessage();
                        }
                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        DialogUtils.showErrorToast(RatingActivity.this, "Error getting ratings");
                    }
                });
    }
    private void showNoRatingsMessage() {
        textHeader.setText("Chưa có đánh giá");
    }
}
