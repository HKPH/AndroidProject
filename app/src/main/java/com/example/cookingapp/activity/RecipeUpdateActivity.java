package com.example.cookingapp.activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
import com.example.cookingapp.utils.NotificationUtil;;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeUpdateActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private View shadowLayout;
    private FrameLayout fragmentContainer;
    private StepAdapter stepAdapter;
    private List<Step> steps = new ArrayList<>();
    private EditText editTextTitle, editTextDescription, editTextIngredients, editTextSteps, editTextVideoUrl;
    private ImageView imageViewPreview;
    private ImageButton backButton;
    private Button updateRecipeButton;
    private Recipe selectedRecipe;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        initializeViews();
        setupFirebase();
        setupClickListener();
        populateViews();

    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.recipe_title_edit);
        editTextDescription = findViewById(R.id.recipe_description_edit);
        editTextIngredients = findViewById(R.id.recipe_ingredients_edit);
        editTextSteps = findViewById(R.id.recipe_step_edit);
        editTextVideoUrl = findViewById(R.id.recipe_url_edit);
        imageViewPreview = findViewById(R.id.recipe_image);
        updateRecipeButton = findViewById(R.id.button_update_recipe);
        shadowLayout = findViewById(R.id.overlay);
        fragmentContainer = findViewById(R.id.fragment_container_step_detail);
        backButton = findViewById(R.id.button_back);
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    private void setupClickListener() {
        findViewById(R.id.button_add_step).setOnClickListener(v -> addStep());
        imageViewPreview.setOnClickListener(v -> openImagePicker());
        updateRecipeButton.setOnClickListener(v -> updateRecipe());
        shadowLayout.setOnClickListener(v -> closeFragment());
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void populateViews() {
        selectedRecipe = (Recipe) getIntent().getSerializableExtra("recipe");

        editTextTitle.setText(selectedRecipe.getTitle());
        editTextDescription.setText(selectedRecipe.getDescription());
        editTextIngredients.setText(convertListToString(selectedRecipe.getIngredients()));
        editTextVideoUrl.setText(selectedRecipe.getVideoUrl());

        steps.addAll(selectedRecipe.getSteps());
        stepAdapter = new StepAdapter(steps, this::showStepDetail);
        RecyclerView recyclerView = findViewById(R.id.step_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(stepAdapter);

        Glide.with(this).load(selectedRecipe.getImageUrl()).into(imageViewPreview);
    }

    private void addStep() {
        String[] parts = editTextSteps.getText().toString().split(":");
        if (parts.length == 2) {
            steps.add(new Step(parts[0], parts[1]));
            stepAdapter.notifyDataSetChanged();
            editTextSteps.setText("");
        } else {
            DialogUtils.showErrorToast(this, "Có lỗi khi hiển thị các bước");
        }
    }

    private void showStepDetail(String stepDetail) {
        StepDetailFragment stepDetailFragment = new StepDetailFragment();
        replaceFragment(stepDetailFragment, stepDetail);
    }

    private void replaceFragment(Fragment fragment, String detail) {
        fragmentContainer.setVisibility(View.VISIBLE);
        Bundle bundle = new Bundle();
        bundle.putString("detail", detail);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_step_detail, fragment).commit();
        shadowLayout.setVisibility(View.VISIBLE);
    }

    private void closeFragment() {
        fragmentContainer.setVisibility(View.GONE);
        shadowLayout.setVisibility(View.GONE);
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
            Glide.with(this).load(selectedImageUri).into(imageViewPreview);
        }
    }

    private void updateRecipe() {
        progressDialog.show();
        selectedRecipe.setTitle(editTextTitle.getText().toString());
        selectedRecipe.setDescription(editTextDescription.getText().toString());
        selectedRecipe.setIngredients(Arrays.asList(editTextIngredients.getText().toString().split("\n")));
        selectedRecipe.setSteps(steps);
        selectedRecipe.setVideoUrl(editTextVideoUrl.getText().toString());

        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri);
        } else {
            updateRecipeData(null);
        }
        progressDialog.dismiss();
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String fileName = System.currentTimeMillis() + "." + getFileExtension(imageUri);
        StorageReference fileRef = storageRef.child("RecipeImage").child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> updateRecipeData(uri.toString())))
                .addOnFailureListener(e -> DialogUtils.showErrorToast(this, "Có lỗi xảy ra" ));
    }

    private void updateRecipeData(String imageUrl) {
        if (imageUrl != null) {
            selectedRecipe.setImageUrl(imageUrl);
        }
        selectedRecipe.setApprove(false);

        db.collection("recipes").document(selectedRecipe.getId()).set(selectedRecipe)
                .addOnSuccessListener(aVoid -> {
                    sendNotificationToAdmin();
                    DialogUtils.showSuccessToast(this, "Công thức đã được cập nhật");
                    finish();
                })
                .addOnFailureListener(e -> DialogUtils.showErrorToast(this, "Cập nhật công thức thất bại"));
    }

    private void sendNotificationToAdmin() {
        FirebaseUtil.getAdminTokens()
                .addOnSuccessListener(adminTokens -> {
                    for (String adminToken : adminTokens) {
                        NotificationUtil.sendNotification("admin", "Có người đã cập nhật công thức", adminToken);
                    }
                })
                .addOnFailureListener(e -> DialogUtils.showErrorToast(this, ""));
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
