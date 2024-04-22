package com.example.cookingapp.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cookingapp.R;

public class DialogUtils {

    public static void showSuccessToast(Context context, String message) {
        showToast(context, message, R.drawable.success, Color.GREEN, Gravity.BOTTOM);
    }

    public static void showErrorToast(Context context, String message) {
        showToast(context, message, R.drawable.error, Color.RED, Gravity.BOTTOM);
    }

    public static void showInfoToast(Context context, String message) {
        showToast(context, message, 0, 0, Gravity.BOTTOM);
    }

    private static void showToast(Context context, String message, int iconResId, int iconColor, int gravity) {
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(gravity, 0, 100);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        if (iconResId != 0) {
            ImageView iconView = new ImageView(context);
            iconView.setImageResource(iconResId);
            iconView.setColorFilter(iconColor); // Đặt màu cho icon
            iconView.setPadding(0, 0, 20, 0);
            layout.addView(iconView);
        }

        TextView textView = new TextView(context);
        textView.setText(message);
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        layout.addView(textView);
        toast.setView(layout);
        toast.show();
    }
}
