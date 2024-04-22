package com.example.cookingapp.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookingapp.R;
import com.example.cookingapp.model.Rating;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {

    private List<Rating> ratings;
    private Map<String, String> userNameCache;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    public RatingAdapter(List<Rating> ratings) {
        this.ratings = ratings;
        this.userNameCache = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rating_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rating rating = ratings.get(position);

        // Bind data to views
        holder.review.setText(rating.getReview());


        // Check if the user name is already cached
        if (userNameCache.containsKey(rating.getUserId())) {
            holder.userName.setText(userNameCache.get(rating.getUserId()));
        } else {
            // Fetch user name asynchronously and cache it
            fetchUserNameFromUID(rating.getUserId(), holder.userName);
        }
        getUserAvatar(rating.getUserId(), holder.imgUser);
    }
    private void getUserAvatar(String userId, ImageView imgUser) {
        // Truy vấn tài liệu người dùng từ Firestore
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Lấy URL của ảnh đại diện từ tài liệu người dùng
                    String photoUrl = document.getString("photo");
                    if (photoUrl != null) {
                        // Sử dụng Glide để tải ảnh đại diện từ URL và hiển thị nó
                        Glide.with(imgUser.getContext())
                                .load(photoUrl)
                                .placeholder(R.drawable.default_profile_image)
                                .into(imgUser);
                    }
                } else {
                    Log.d("RatingAdapter", "No such document");
                }
            } else {
                Log.d("RatingAdapter", "get failed with ", task.getException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView userName, review;
        ImageView imgUser;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.text_user_name);
            review = itemView.findViewById(R.id.text_review);
            imgUser = itemView.findViewById(R.id.user_photo);
        }
    }

    private void fetchUserNameFromUID(String userId, TextView textView) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        userNameCache.put(userId, userName);
                        textView.setText(userName);
                    } else {
                        textView.setText("Unknown User");
                    }
                })
                .addOnFailureListener(e -> {
                    textView.setText("Unknown User");
                });
    }
}
