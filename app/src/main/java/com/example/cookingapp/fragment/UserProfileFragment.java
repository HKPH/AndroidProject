package com.example.cookingapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.cookingapp.R;
import com.example.cookingapp.activity.LoginActivity;
import com.example.cookingapp.activity.UpdateProfileActivity;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileFragment extends Fragment {

    private ImageView imageViewProfile;
    private TextView textViewName, textViewEmail, textViewPhone;
    private Button buttonLogout;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        initializeViews(view);
        initializeFirebase();

        if (mUser != null) {
            loadUserData();
        }
        ImageView imageEditProfile = view.findViewById(R.id.image_edit_profile);
        imageEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditProfileActivity();
            }
        });
        return view;
    }
    private void openEditProfileActivity() {
        Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
        startActivity(intent);
    }
    private void initializeViews(View view) {
        imageViewProfile = view.findViewById(R.id.image_view_profile);
        textViewName = view.findViewById(R.id.text_name);
        textViewEmail = view.findViewById(R.id.text_email);
        textViewPhone = view.findViewById(R.id.text_phone);
        buttonLogout = view.findViewById(R.id.button_logout);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mFirestore = FirebaseFirestore.getInstance();
    }

    private void loadUserData() {
        mFirestore.collection("users").document(mUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String phone = document.getString("phone");
                            String photoUrl = document.getString("photo");

                            textViewName.setText(name);
                            textViewEmail.setText(email);
                            textViewPhone.setText(phone);
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(photoUrl)
                                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                        .into(imageViewProfile);
                            }
                        } else {
                            DialogUtils.showErrorToast(getActivity(), "Không tìm thấy thông tin người dùng");
                        }
                    } else {
                        DialogUtils.showErrorToast(getActivity(), "Lỗi khi tải thông tin người dùng");
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
