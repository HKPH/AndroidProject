package com.example.cookingapp.activity;

import android.app.ProgressDialog;
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

public class AddRecipeActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    private Uri imageUri;
    private ImageView selectedImageView;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private List<Step> steps = new ArrayList<>();
    private FirebaseAuth mAuth;
    private StepAdapter stepAdapter;
    private View shadowLayout;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        setContentView(R.layout.activity_add_recipe);
        initializeViews();
        setupListeners();
        initializeFirebase();
    }

    private void initializeViews() {
        shadowLayout = findViewById(R.id.overlay);
        selectedImageView = findViewById(R.id.recipe_image);
        stepAdapter = new StepAdapter(steps, stepClickListener);
        RecyclerView recyclerView = findViewById(R.id.step_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(stepAdapter);
        fragmentContainer = findViewById(R.id.fragment_container_step_detail);
    }

    private void setupListeners() {
        selectedImageView.setOnClickListener(v -> openFileChooser());
        ImageView imageViewAddStep = findViewById(R.id.button_add_step);
        imageViewAddStep.setOnClickListener(v -> addEachStep());
        Button addRecipeButton = findViewById(R.id.add_recipe_button);
        addRecipeButton.setOnClickListener(v -> uploadImageAndAddRecipe());
        shadowLayout.setOnClickListener(v -> closeFragment());
    }

    private void closeFragment() {
        fragmentContainer.setVisibility(View.GONE);
        shadowLayout.setVisibility(View.GONE);
    }

    private void addEachStep() {
        EditText editTextSteps = findViewById(R.id.recipe_step_edit);
        String[] parts = editTextSteps.getText().toString().split(":");

        if (parts.length == 2) {
            steps.add(new Step(parts[0], parts[1]));
            stepAdapter.notifyDataSetChanged();
            editTextSteps.setText("");
        } else {
            DialogUtils.showErrorToast(AddRecipeActivity.this, "Có lỗi khi thêm các bước");
        }
    }

    private void initializeFirebase() {
        storageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private StepAdapter.StepClickListener stepClickListener = new StepAdapter.StepClickListener() {
        @Override
        public void onStepClicked(String stepDetail) {
            StepDetailFragment stepDetailFragment = new StepDetailFragment();
            replaceFragment(stepDetailFragment, stepDetail);
        }
    };

    private void replaceFragment(Fragment fragment, String detail) {
        fragmentContainer.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putString("detail", detail);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.fragment_container_step_detail, fragment).commit();
        shadowLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImageView.setImageURI(imageUri);
            selectedImageView.setVisibility(View.VISIBLE);
        }
    }

    private void uploadImageAndAddRecipe() {
        progressDialog.show();
        if (imageUri != null) {
            String imageName = System.currentTimeMillis() + "." + getFileExtension(imageUri);
            final StorageReference fileRef = storageRef.child("RecipeImage").child(imageName);

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> addRecipe(uri.toString()));
                    })
                    .addOnFailureListener(e -> DialogUtils.showErrorToast(AddRecipeActivity.this, "Tải ảnh thất bại: " + e.getMessage()));
        } else {
            DialogUtils.showErrorToast(AddRecipeActivity.this, "Không có ảnh được chọn");
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void addRecipe(String imageUrl) {
        EditText editTextTitle = findViewById(R.id.recipe_title_edit);
        EditText editTextDescription = findViewById(R.id.recipe_description_edit);
        EditText editTextIngredients = findViewById(R.id.recipe_ingredients_edit);
        EditText editTextVideoUrl = findViewById(R.id.recipe_url_edit);

        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        List<String> ingredients = Arrays.asList(editTextIngredients.getText().toString().split("\n"));
        String videoUrl = editTextVideoUrl.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || videoUrl.isEmpty()) {
            progressDialog.dismiss();
            DialogUtils.showErrorToast(AddRecipeActivity.this, "Điền vào tất cả thông tin");
            return;
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
//            DialogUtils.showErrorToast(AddRecipeActivity.this, "User not logged in");
            progressDialog.dismiss();
            return;
        }

        String userId = firebaseUser.getUid();
        Recipe recipe = new Recipe(null, title, description, ingredients, steps, imageUrl, videoUrl, false, userId);

        db.collection("recipes")
                .add(recipe)
                .addOnSuccessListener(documentReference -> {
                    String newRecipeId = documentReference.getId();
                    documentReference.update("id", newRecipeId)
                            .addOnSuccessListener(aVoid -> {
                                notifyAdmins();
                                DialogUtils.showSuccessToast(AddRecipeActivity.this, "Công thức được thêm thành công");
                                progressDialog.dismiss();
                                finish();
                            })
                            .addOnFailureListener(e ->
                            {
                                progressDialog.dismiss();
                                DialogUtils.showErrorToast(AddRecipeActivity.this, "Không thể tạo công thức" );

                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    DialogUtils.showErrorToast(AddRecipeActivity.this, "Không thể tạo công thức");
                });
    }

    private void notifyAdmins() {
        FirebaseUtil.getAdminTokens().addOnSuccessListener(adminTokens -> {
            for (String adminToken : adminTokens) {
                NotificationUtil notificationUtil = new NotificationUtil();
                notificationUtil.sendNotification("admin", "Có công thức mới được thêm. Hãy kiểm tra!", adminToken);
            }
//        }).addOnFailureListener(e -> DialogUtils.showErrorToast(AddRecipeActivity.this, "Error: " + e.getMessage()));
        }).addOnFailureListener(e -> Log.d("Error: ", "" + e.getMessage()));

    }
}
