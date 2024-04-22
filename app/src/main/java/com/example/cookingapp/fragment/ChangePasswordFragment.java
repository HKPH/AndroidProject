package com.example.cookingapp.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cookingapp.R;
import com.example.cookingapp.utils.DialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {

    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonChangePassword;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_change_password, container, false);

        editTextOldPassword = view.findViewById(R.id.edit_text_old_password);
        editTextNewPassword = view.findViewById(R.id.edit_text_new_password);
        editTextConfirmPassword = view.findViewById(R.id.edit_text_confirm_password);
        buttonChangePassword = view.findViewById(R.id.button_change_password);

        mAuth = FirebaseAuth.getInstance();

        buttonChangePassword.setOnClickListener(v -> changePassword());

        return view;
    }

    private void changePassword() {
        String oldPassword = editTextOldPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            DialogUtils.showErrorToast(getActivity(), "Vui lòng nhập đầy đủ thông tin");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            DialogUtils.showErrorToast(getActivity(), "Mật khẩu xác nhận không khớp");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DialogUtils.showSuccessToast(getActivity(), "Mật khẩu đã được thay đổi");
                        } else {
                            DialogUtils.showErrorToast(getActivity(), "Thay đổi mật khẩu thất bại");
                        }
                    });
        } else {
            DialogUtils.showErrorToast(getActivity(), "Không tìm thấy người dùng hiện tại");
        }
    }
}
