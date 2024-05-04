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
                throw new IllegalStateException("Lỗi khi lấy danh sách mã token của admin", exception);
            }
        });
    }

    public interface OnCompleteListener {
        void onSuccess(String message);
        void onError(String error);
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
}
