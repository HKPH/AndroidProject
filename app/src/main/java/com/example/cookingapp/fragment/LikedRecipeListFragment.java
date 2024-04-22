package com.example.cookingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.R;
import com.example.cookingapp.activity.RecipeDetailActivity;
import com.example.cookingapp.adapter.RecipeAdapter;
import com.example.cookingapp.model.Recipe;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LikedRecipeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> likedRecipes;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupFirebase();
        loadLikedRecipes();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        likedRecipes = new ArrayList<>();
        adapter = new RecipeAdapter(getActivity(), likedRecipes);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Recipe selectedRecipe = likedRecipes.get(position);
            Log.d("LikedRecipeListFragment", "Clicked on recipe: " + selectedRecipe.getTitle());
            Intent intent = new Intent(getActivity(), RecipeDetailActivity.class);
            intent.putExtra("recipeId", selectedRecipe.getId());
            startActivity(intent);
        });
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void loadLikedRecipes() {
        if (currentUser == null) {
//            Log.e("LikedRecipeListFragment", "Current user is null");
            return;
        }

        db.collection("likes")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> recipeIds = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String recipeId = documentSnapshot.getString("recipeId");
                        if (recipeId != null && !recipeId.isEmpty()) {
                            recipeIds.add(recipeId);
                        }
                    }
                    if (!recipeIds.isEmpty()) {
                        loadRecipes(recipeIds);
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(getActivity(), "Không thể tải danh sách công thức yêu thích");
                });
    }

    private void loadRecipes(List<String> recipeIds) {
        db.collection("recipes")
                .whereIn("id", recipeIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    likedRecipes.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        likedRecipes.add(recipe);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(getActivity(), "Không thể tải danh sách công thức yêu thích");
                });
    }
}
