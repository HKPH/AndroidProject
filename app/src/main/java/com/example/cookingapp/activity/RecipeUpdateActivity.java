package com.example.cookingapp.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookingapp.R;
import com.example.cookingapp.adapter.StepAdapter;
import com.example.cookingapp.fragment.StepDetailFragment;
import com.example.cookingapp.model.Recipe;
import com.example.cookingapp.model.Step;
import com.example.cookingapp.utils.DialogUtils;
import com.example.cookingapp.utils.FirebaseUtil;
import com.example.cookingapp.utils.NotificationUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeUpdateActivity extends AppCompatActivity {
    private View shadowLayout;
    private FrameLayout fragmentContainer;
    private StepAdapter stepAdapter;
    private List<Step> steps = new ArrayList<>();
    private EditText editTextTitle, editTextDescription, editTextIngredients, editTextSteps, editTextVideoUrl;
    private ImageView imageViewPreview;
    private Button updateRecipeButton;
    private Recipe selectedRecipe;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private FirebaseUser currentUser;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        fragmentContainer= findViewById(R.id.fragment_container_step_detail);

        editTextTitle = findViewById(R.id.recipe_title_edit);
        editTextDescription = findViewById(R.id.recipe_description_edit);
        editTextIngredients = findViewById(R.id.recipe_ingredients_edit);
        editTextSteps = findViewById(R.id.recipe_step_edit);
        editTextVideoUrl = findViewById(R.id.recipe_url_edit);
        imageViewPreview = findViewById(R.id.recipe_image);
        updateRecipeButton = findViewById(R.id.button_update_recipe);
        shadowLayout= findViewById(R.id.overlay);
        selectedRecipe = (Recipe) getIntent().getSerializableExtra("recipe");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        ImageView imageViewAddStep = findViewById(R.id.button_add_step);
        imageViewAddStep.setOnClickListener(v -> addEachStep());
        shadowLayout.setOnClickListener(v-> closeFragment());
        populateViews();

        imageViewPreview.setOnClickListener(v -> openImagePicker());

        updateRecipeButton.setOnClickListener(v -> updateRecipe());
    }
    private void addEachStep()
    {
        EditText editTextSteps = findViewById(R.id.recipe_step_edit);
        String[] parts = editTextSteps.getText().toString().split(":");

        if (parts.length == 2) {
            steps.add(new Step(parts[0], parts[1]));
            stepAdapter.notifyDataSetChanged();
//            stepAdapter.updateSteps(steps);
            editTextSteps.setText("");
        } else {
            DialogUtils.showErrorToast(RecipeUpdateActivity.this,"Có lỗi khi thêm các bước");
        }
    }
    private StepAdapter.StepClickListener stepClickListener = new StepAdapter.StepClickListener() {
        @Override
        public void onStepClicked(String stepDetail) {

            StepDetailFragment stepDetailFragment = new StepDetailFragment();
            Log.d("Fragment nhận được",""+stepDetail);
            replaceFragment(stepDetailFragment,stepDetail);
        }
    };
    private void replaceFragment(Fragment fragment, String detail) {
        fragmentContainer.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putString("detail", detail); // Thay "key" và "value" bằng dữ liệu cụ thể của bạn
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.setArguments(bundle);
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_step_detail, fragment)
                .commit();
        shadowLayout.setVisibility(View.VISIBLE);



    }
    private void closeFragment() {
        fragmentContainer.setVisibility(View.GONE);
        shadowLayout.setVisibility(View.GONE);
    }
    private void populateViews() {
        stepAdapter = new StepAdapter(steps, stepClickListener);
        editTextTitle.setText(selectedRecipe.getTitle());
        editTextDescription.setText(selectedRecipe.getDescription());
        editTextIngredients.setText(convertListToString(selectedRecipe.getIngredients()));
        steps = selectedRecipe.getSteps();
        editTextVideoUrl.setText(selectedRecipe.getVideoUrl());


        stepAdapter = new StepAdapter(steps, stepClickListener);
        RecyclerView recyclerView = findViewById(R.id.step_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(stepAdapter);

        Glide.with(this)
                .load(selectedRecipe.getImageUrl())
                .into(imageViewPreview);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imageViewPreview);
        }
    }

    private void updateRecipe() {
        String updatedTitle = editTextTitle.getText().toString();
        String updatedDescription = editTextDescription.getText().toString();
        String updatedIngredients = editTextIngredients.getText().toString();
        String updatedVideoUrl = editTextVideoUrl.getText().toString();

        selectedRecipe.setTitle(updatedTitle);
        selectedRecipe.setDescription(updatedDescription);

        List<String> updatedIngredientsList = Arrays.asList(updatedIngredients.split("\n"));
        List<Step> updatedStepsList = steps;

        selectedRecipe.setIngredients(updatedIngredientsList);
        selectedRecipe.setSteps(updatedStepsList);
        selectedRecipe.setVideoUrl(updatedVideoUrl);

        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri);
        } else {
            updateRecipeData(null);
        }
    }
    private String listStepInList(List<Step> list) {
        StringBuilder builder = new StringBuilder();
        int stepNumber = 1;
        for (Step step : list) {
            builder.append(stepNumber).append(". ").append(step.getStepName()).append("\n");
            builder.append("   ").append(step.getStepDetail()).append("\n");
            stepNumber++;
        }
        return builder.toString();
    }
    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference recipeImageRef = storageRef.child("RecipeImage");
        String fileName = System.currentTimeMillis() + "." + getFileExtension(imageUri);
        final StorageReference fileRef = recipeImageRef.child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        updateRecipeData(imageUrl);
                    });
                })
                .addOnFailureListener(e -> DialogUtils.showErrorToast(RecipeUpdateActivity.this, "Upload failed: " + e.getMessage()));
    }

    private void updateRecipeData(String imageUrl) {
        if (imageUrl != null) {
            selectedRecipe.setImageUrl(imageUrl);
        }
        selectedRecipe.setApprove(false);

        db.collection("recipes")
                .document(selectedRecipe.getId())
                .set(selectedRecipe)
                .addOnSuccessListener(aVoid -> {
                    sendNotificationToAdmin();
                    DialogUtils.showSuccessToast(RecipeUpdateActivity.this, "Recipe updated successfully");
                    finish();
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(RecipeUpdateActivity.this, "Error: " + e.getMessage());
                });
    }

    private void sendNotificationToAdmin() {
        FirebaseUtil.getAdminTokens().addOnSuccessListener(adminTokens -> {
            for (String adminToken : adminTokens) {
                NotificationUtil notificationUtil = new NotificationUtil();
                notificationUtil.sendNotification("admin", "A recipe got updated by user", adminToken);
            }
        }).addOnFailureListener(e -> DialogUtils.showErrorToast(RecipeUpdateActivity.this, "Error: " + e.getMessage()));
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String convertListToString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String item : list) {
            stringBuilder.append(item).append("\n");
        }
        return stringBuilder.toString();
    }
}
