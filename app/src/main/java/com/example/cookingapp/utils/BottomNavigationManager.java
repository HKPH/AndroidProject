package com.example.cookingapp.utils;

import android.app.Activity;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.cookingapp.R;
import com.example.cookingapp.fragment.AdminListFragment;
import com.example.cookingapp.fragment.LikedRecipeListFragment;
import com.example.cookingapp.fragment.RecipeCreatorListFragment;
import com.example.cookingapp.fragment.RecipeListFragment;
import com.example.cookingapp.fragment.UserProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BottomNavigationManager {

    private Activity mActivity;
    private BottomNavigationView mBottomNavigationView;
    private FragmentManager mFragmentManager;

    public BottomNavigationManager(Activity activity, BottomNavigationView bottomNavigationView, FragmentManager fragmentManager) {
        mActivity = activity;
        mBottomNavigationView = bottomNavigationView;
        mFragmentManager = fragmentManager;
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Set the first item as selected by default
        mBottomNavigationView.setSelectedItemId(R.id.home);

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.home:
                                isAdmin();
                                return true;
                            case R.id.creatorList:
                                replaceFragment(new RecipeCreatorListFragment());
                                return true;
                            case R.id.likedList:
                                replaceFragment(new LikedRecipeListFragment());
                                return true;
                            case R.id.profile:
                                replaceFragment(new UserProfileFragment());
                                return true;
                            default:
                                return false;
                        }
                    }
                });
    }

    private void replaceFragment(Fragment fragment) {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void isAdmin() {
        // Lấy UID của người dùng hiện tại từ Firebase Auth
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Thực hiện truy vấn để lấy thông tin về người dùng từ Firebase Firestore
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isAdmin = documentSnapshot.getBoolean("checkAuth");
                        if (isAdmin != null && isAdmin) {
                            replaceFragment(new AdminListFragment());
                        } else {

                            replaceFragment(new RecipeListFragment());
                        }
                    } else {
                        replaceFragment(new RecipeListFragment());
                    }
                })
                .addOnFailureListener(e -> {
                    replaceFragment(new RecipeListFragment());
                });
    }
}
