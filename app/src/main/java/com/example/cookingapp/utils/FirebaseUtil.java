package com.example.cookingapp.utils;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.model.Like;
import com.example.cookingapp.model.Rating;
import com.example.cookingapp.model.Recipe;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUtil {

    public static Task<List<String>> getAdminTokens() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");

        Query query = usersRef.whereEqualTo("checkAuth", true);

        return query.get().continueWith(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                List<String> adminTokens = new ArrayList<>();
                for (DocumentSnapshot document : documents) {
                    String adminToken = document.getString("fcmToken");
                    if (adminToken != null) {
                        adminTokens.add(adminToken);
                    }
                }
                if (adminTokens.isEmpty()) {
                    throw new IllegalStateException("Không tìm thấy mã token của admin");
                }
                return adminTokens;
            } else {
                Exception exception = task.getException();
                Log.e("FirebaseUtil", "Lỗi khi lấy danh sách mã token của admin", exception);
                throw new IllegalStateException("Lỗi khi lấy danh sách mã token của admin", exception);
            }
        });
    }

    public interface OnCompleteListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public static void likeRecipe(FirebaseFirestore db, String userId, String recipeId, OnCompleteListener listener) {
        db.collection("likes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("recipeId", recipeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Nếu không có cặp uid và recipeId nào tồn tại, thêm mới vào Firebase
                        db.collection("likes")
                                .add(new Like(userId, recipeId))
                                .addOnSuccessListener(documentReference -> {
                                    listener.onSuccess("Recipe liked");
                                })
                                .addOnFailureListener(e -> {
                                    listener.onError("Failed to like recipe");
                                });
                    } else {
                        // Nếu cặp uid và recipeId đã tồn tại, thông báo cho người dùng
                        listener.onError("You have already liked this recipe");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to check like status");
                });
    }

    public interface OnRecipeLoadListener {
        void onRecipeLoaded(DocumentSnapshot documentSnapshot);
        void onError(String error);
    }

    public static void getRecipeById(String recipeId, final OnRecipeLoadListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference recipeRef = db.collection("recipes").document(recipeId);
        recipeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    listener.onRecipeLoaded(document);
                } else {
                    listener.onError("Recipe not found");
                }
            } else {
                listener.onError("Error fetching recipe: " + task.getException());
            }
        });
    }
    public static void saveRatingToFirestore(AppCompatActivity activity, Rating rating, Recipe recipe, OnCompleteListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Save review data to Firestore here
        db.collection("ratings")
                .add(rating)
                .addOnSuccessListener(documentReference -> {
                    // Show success message
                    listener.onSuccess("Review submitted successfully");
                    // Notify recipe owner
                    NotificationUtil.notifyRecipeOwner(activity, recipe.getId(), "Your recipe has received a new review!");
                })
                .addOnFailureListener(e -> {
                    // Show error message
                    listener.onError("Failed to submit review");
                });
    }

    public static void checkIfUserReviewed(FirebaseFirestore db, String userId, String recipeId, OnCompleteListener listener) {
        db.collection("ratings")
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // If the user hasn't reviewed the recipe yet, submit the review
                        listener.onSuccess("User can review");
                    } else {
                        // If the user has already reviewed the recipe, show a message
                        listener.onError("User has already reviewed this recipe");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to check review status");
                });
    }
}
