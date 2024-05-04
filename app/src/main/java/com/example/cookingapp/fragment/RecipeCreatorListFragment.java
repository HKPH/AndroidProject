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
import com.example.cookingapp.activity.AddRecipeActivity;
import com.example.cookingapp.activity.RecipeUpdateActivity;
import com.example.cookingapp.adapter.RecipeAdapter;
import com.example.cookingapp.model.Recipe;
import com.example.cookingapp.utils.DialogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeCreatorListFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipes;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FloatingActionButton btAddRecipe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupFirebase();
        setupAddRecipeButton();
        loadRecipeList();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        btAddRecipe = view.findViewById(R.id.btAddRecipe);
        btAddRecipe.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        recipes = new ArrayList<>();
        adapter = new RecipeAdapter(getActivity(), recipes);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {
            Recipe selectedRecipe = recipes.get(position);
//            Log.d("RecipeCreatorListFragment", "Clicked on recipe: " + selectedRecipe.getTitle());
            Intent intent = new Intent(getActivity(), RecipeUpdateActivity.class);
            intent.putExtra("recipe", selectedRecipe);
            startActivity(intent);
        });
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupAddRecipeButton() {
        btAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddRecipeActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipeList();
    }

    private void loadRecipeList() {
        if (currentUser == null) {
            Log.e("RecipeCreatorListFragment", "Current user is null");
            return;
        }

        db.collection("recipes")
                .whereEqualTo("creator", currentUser.getUid())
                .whereEqualTo("approve", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recipes.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        recipes.add(recipe);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(getActivity(), "Không thể tải danh sách");
                });
    }
}
