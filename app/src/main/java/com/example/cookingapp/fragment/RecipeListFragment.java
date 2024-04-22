package com.example.cookingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.R;
import com.example.cookingapp.activity.RecipeDetailActivity;
import com.example.cookingapp.adapter.RecipeAdapter;
import com.example.cookingapp.model.Recipe;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecipeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> recipes;
    private FirebaseFirestore db;

    public RecipeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipes = new ArrayList<>();
        adapter = new RecipeAdapter(getContext(), recipes);
        recyclerView.setAdapter(adapter);

        FloatingActionButton btAddRecipe = view.findViewById(R.id.btAddRecipe);
        btAddRecipe.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the recipe list by title
                filter(newText);
                return true;
            }
        });

        // Query recipes from Firestore
        loadRecipesFromFirestore();
        setOnRecipeItemClickListener();

        return view;
    }

    private void loadRecipesFromFirestore() {
        db.collection("recipes")
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
                .addOnFailureListener(e -> DialogUtils.showErrorToast(requireContext(), "Error loading recipes"));
    }

    private void setOnRecipeItemClickListener() {
        adapter.setOnItemClickListener(position -> {
            Recipe selectedRecipe = recipes.get(position);
            Log.d("RecipeListFragment", "Clicked on recipe: " + selectedRecipe.getTitle());
            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("recipeId", selectedRecipe.getId());
            startActivity(intent);
        });
    }

    private void filter(String searchText) {
        ArrayList<Recipe> filteredList = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (recipe.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(recipe);
            }
        }
        adapter.filterList(filteredList);
    }
}
