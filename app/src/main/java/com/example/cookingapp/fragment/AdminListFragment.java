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

public class AdminListFragment extends Fragment {

    private RecyclerView recyclerViewUnapproved, recyclerViewApproved;
    private RecipeAdapter unapprovedAdapter, approvedAdapter;
    private List<Recipe> unapprovedRecipes, approvedRecipes;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    public AdminListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_admin_recipe_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerViews();
        setupFirebase();
        loadRecipeList(false);
        setOnUnapprovedRecipeItemClickListener();
        loadRecipeList(true);
        setOnApprovedRecipeItemClickListener();
    }

    private void initializeViews(View view) {
        recyclerViewUnapproved = view.findViewById(R.id.recyclerViewUnapproved);
        recyclerViewApproved = view.findViewById(R.id.recyclerViewApproved);
    }

    private void setupRecyclerViews() {
        unapprovedRecipes = new ArrayList<>();
        approvedRecipes = new ArrayList<>();

        unapprovedAdapter = new RecipeAdapter(getContext(), unapprovedRecipes);
        approvedAdapter = new RecipeAdapter(getContext(), approvedRecipes);

        recyclerViewUnapproved.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewUnapproved.setAdapter(unapprovedAdapter);

        recyclerViewApproved.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewApproved.setAdapter(approvedAdapter);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }
    private void setOnUnapprovedRecipeItemClickListener() {
        unapprovedAdapter.setOnItemClickListener(position -> {
            Recipe selectedRecipe = unapprovedRecipes.get(position);
            Log.d("RecipeListFragment", "Clicked on recipe: " + selectedRecipe.getTitle());
            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("recipeId", selectedRecipe.getId());
            startActivity(intent);
        });
    }
    private void setOnApprovedRecipeItemClickListener() {
        approvedAdapter.setOnItemClickListener(position -> {
            Recipe selectedRecipe = approvedRecipes.get(position);
            Log.d("RecipeListFragment", "Clicked on recipe: " + selectedRecipe.getTitle());
            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("recipeId", selectedRecipe.getId());
            startActivity(intent);
        });
    }

    private void loadRecipeList(boolean approved) {
        db.collection("recipes")
                .whereEqualTo("approve", approved)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Recipe> recipes = approved ? approvedRecipes : unapprovedRecipes;
                    recipes.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
                        recipes.add(recipe);
                    }
                    if (approved) {
                        approvedAdapter.notifyDataSetChanged();
                    } else {
                        unapprovedAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> DialogUtils.showErrorToast(getContext(), "Failed to load recipes"));
    }
}
