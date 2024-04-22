package com.example.cookingapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cookingapp.R;
import com.example.cookingapp.fragment.StepDetailFragment;
import com.example.cookingapp.model.Like;
import com.example.cookingapp.model.Rating;
import com.example.cookingapp.model.Recipe;
import com.example.cookingapp.model.Step;
import com.example.cookingapp.utils.DialogUtils;
import com.example.cookingapp.utils.FirebaseUtil;
import com.example.cookingapp.utils.NotificationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    private Recipe recipe;
    private String recipeId;
    private ImageView likeButton;
    private FirebaseUser currentUser;
    private Rating rating;
    private EditText editTextReview;
    private ImageView buttonSubmitReview;
    private ImageView buttonAdd;
    private ImageView buttonDelete;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        isUserAdmin();
        // Get recipeId from Intent
        recipeId = getIntent().getStringExtra("recipeId");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Load recipe details
        loadRecipeDetails();

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        likeButton = findViewById(R.id.button_like);
        editTextReview = findViewById(R.id.edit_review);
        buttonSubmitReview = findViewById(R.id.button_submit_review);
        buttonAdd=findViewById(R.id.button_add);
        buttonDelete=findViewById(R.id.button_delete);
    }

    private void loadRecipeDetails() {
        FirebaseUtil.getRecipeById(recipeId, new FirebaseUtil.OnRecipeLoadListener() {
            @Override
            public void onRecipeLoaded(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    recipe = documentSnapshot.toObject(Recipe.class);
                    // Populate recipe details to views
                    populateRecipeDetails();
                } else {
                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Recipe not found");
                }
            }

            @Override
            public void onError(String error) {
                DialogUtils.showErrorToast(RecipeDetailActivity.this, error);
            }
        });
    }

    private void populateRecipeDetails() {
        TextView textViewTitle = findViewById(R.id.text_recipe_title);
        TextView textViewDescription = findViewById(R.id.text_recipe_description);
        TextView textViewIngredients = findViewById(R.id.text_recipe_ingredients);
        TextView textViewSteps = findViewById(R.id.text_recipe_steps);
        ImageView imageViewRecipe = findViewById(R.id.recipe_image);
        textViewTitle.setText(recipe.getTitle());
        textViewDescription.setText(recipe.getDescription());
        textViewIngredients.setText(formatListToString(recipe.getIngredients()));
        textViewSteps.setText(formatListStepToString(recipe.getSteps()));
        Glide.with(RecipeDetailActivity.this).load(recipe.getImageUrl()).into(imageViewRecipe);
    }
    private String formatListStepToString(List<Step> list) {
        StringBuilder builder = new StringBuilder();
        int stepNumber = 1;
        for (Step step : list) {
            builder.append(stepNumber).append(". ").append(step.getStepName()).append("\n");
            builder.append("   ").append(step.getStepDetail()).append("\n");
            stepNumber++;
        }
        return builder.toString();
    }

    private void setClickListeners() {
        likeButton.setOnClickListener(v -> toggleLikeStatus());
        buttonSubmitReview.setOnClickListener(v -> submitReview());
        findViewById(R.id.text_video_url).setOnClickListener(v -> openVideoGuide());
        findViewById(R.id.text_rating_list).setOnClickListener(v -> openRatingActivity());
        buttonAdd.setOnClickListener(v -> approveRecipe());
        buttonDelete.setOnClickListener(v -> deleteRecipe());
    }

    private void toggleLikeStatus() {
        String userId = currentUser.getUid();
        db.collection("likes")
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        DialogUtils.showSuccessToast(RecipeDetailActivity.this, "Thích");
                        addLike(userId);
                    } else {
                        DialogUtils.showSuccessToast(RecipeDetailActivity.this, "Bỏ thích");
                        removeLike(queryDocumentSnapshots);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void addLike(String userId) {
        Like like = new Like();
        like.setUserId(userId);
        like.setRecipeId(recipeId);
        FirebaseUtil.likeRecipe(db, userId, recipeId, new FirebaseUtil.OnCompleteListener() {
            @Override
            public void onSuccess(String message) {
                likeButton.setImageResource(R.drawable.baseline_favorite_24);
//                DialogUtils.showSuccessToast(RecipeDetailActivity.this, message);
                // Notify recipe owner
                NotificationUtil.notifyRecipeOwner(RecipeDetailActivity.this, recipeId, "Có người đã thích công thức của bạn!");
            }

            @Override
            public void onError(String error) {
//                DialogUtils.showErrorToast(RecipeDetailActivity.this, error);
            }
        });
    }

    private void removeLike(QuerySnapshot queryDocumentSnapshots) {
        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
            String likeId = snapshot.getId();
            db.collection("likes").document(likeId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        likeButton.setImageResource(R.drawable.baseline_favorite_border_24);
//                        DialogUtils.showSuccessToast(RecipeDetailActivity.this, "Removed like");
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                    });
        }
    }

    private void submitReview() {
        String review = editTextReview.getText().toString();
        rating = new Rating();
        rating.setReview(review);
        rating.setRecipeId(recipeId);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        rating.setUserId(currentUser.getUid());
        checkIfUserReviewed(currentUser.getUid(), recipeId);
    }

    private void checkIfUserReviewed(String userId, String recipeId) {
        db.collection("ratings")
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        saveRatingToFirestore();
                    } else {
                        DialogUtils.showInfoToast(RecipeDetailActivity.this, "Bạn đã có đánh giá");
                    }
                })
                .addOnFailureListener(e -> {
//                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Failed to check review status");
                });
    }

    private void saveRatingToFirestore() {
        db.collection("ratings")
                .add(rating)
                .addOnSuccessListener(documentReference -> {
                    DialogUtils.showInfoToast(RecipeDetailActivity.this, "Đánh giá thành công");
                    NotificationUtil.notifyRecipeOwner(RecipeDetailActivity.this, recipeId, "Công thức của bạn nhận được 1 đánh giá");
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Failed to submit review");
                });
    }

    private void openVideoGuide() {
        String videoUrl = recipe.getVideoUrl();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(intent);
        } else {
            DialogUtils.showErrorToast(RecipeDetailActivity.this, "Video không khả dụng");
        }
    }

    private void openRatingActivity() {
        Intent intent = new Intent(RecipeDetailActivity.this, RatingActivity.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
    }
    private void isUserAdmin() {
        // Lấy UID của người dùng hiện tại từ Firebase Auth
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Thực hiện truy vấn để lấy thông tin về người dùng từ Firebase Firestore
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("checkAuth");
                        if (isAdmin != null) {
                            checkRecipeApproval();
                        }
                    }
//                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Người dùng không phải quản trị viên");
                })
                .addOnFailureListener(e -> {
//                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Có lỗi xảy ra");
                });
    }
    private void checkRecipeApproval() {
        FirebaseUtil.getRecipeById(recipeId, new FirebaseUtil.OnRecipeLoadListener() {
            @Override
            public void onRecipeLoaded(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Recipe recipe = documentSnapshot.toObject(Recipe.class);
                    boolean isApproved = recipe.isApprove();
                    if (!isApproved) {
                        buttonAdd.setVisibility(View.VISIBLE);
                        buttonDelete.setVisibility(View.VISIBLE);
                    }
                } else {
//                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Recipe not found");
                }
            }

            @Override
            public void onError(String error) {
//                DialogUtils.showErrorToast(RecipeDetailActivity.this, error);
            }
        });
    }


    private void approveRecipe() {
        NotificationUtil.notifyRecipeOwner(RecipeDetailActivity.this,recipeId, "Công thức của bạn đã được duyệt");
        // Update "approved" field of recipe to true
        db.collection("recipes").document(recipeId)
                .update("approve", true)
                .addOnSuccessListener(aVoid -> {
                    DialogUtils.showSuccessToast(RecipeDetailActivity.this, "Công thức đã được thêm");
                    // Cập nhật giao diện
                    buttonAdd.setVisibility(View.INVISIBLE);
                    buttonDelete.setVisibility(View.INVISIBLE);
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Công thức chưa được thêm");
                });
    }

    private void deleteRecipe() {
        NotificationUtil.notifyRecipeOwner(RecipeDetailActivity.this,recipeId, "Công thức của bạn đã bị từ chối");
        // Xóa recipe khỏi Firestore
        db.collection("recipes").document(recipeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    DialogUtils.showSuccessToast(RecipeDetailActivity.this, "Đã xóa công thức");
                    // Finish activity
                    finish();
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(RecipeDetailActivity.this, "Chưa xóa công thức");
                });
    }

    private String formatListToString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String item : list) {
            builder.append("• ").append(item).append("\n");
        }
        return builder.toString();
    }
}
