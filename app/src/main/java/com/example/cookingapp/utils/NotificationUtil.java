package com.example.cookingapp.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cookingapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationUtil {

    // Lấy ID của người dùng hiện tại
    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    // Lấy tham chiếu đến tài liệu của người dùng hiện tại
    public static DocumentReference getCurrentUserDetails() {
        return FirebaseFirestore.getInstance().collection("users").document(getCurrentUserId());
    }

    public static void notifyRecipeOwner(AppCompatActivity activity, String recipeId, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query Firestore to get the UID of the recipe owner
        db.collection("recipes")
                .document(recipeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String ownerId = documentSnapshot.getString("creator");
                    Log.d("Tìm thấy người dùng:",""+ownerId);

                    if (ownerId != null) {
                        // If owner ID found, query again to get owner's fcmToken
                        db.collection("users")
                                .document(ownerId)
                                .get()
                                .addOnSuccessListener(userDocument -> {
                                    String fcmToken = userDocument.getString("fcmToken");
                                    if (fcmToken != null) {
                                        // If fcmToken found, send notification
                                        sendNotification(recipeId,message, fcmToken);
                                    } else {
                                        DialogUtils.showInfoToast(activity, "Owner's fcmToken not found");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    DialogUtils.showErrorToast(activity, "Failed to get owner's fcmToken");
                                });
                    } else {
                        DialogUtils.showInfoToast(activity, "Không tìm được người dùng");
                    }
                })
                .addOnFailureListener(e -> {
                    DialogUtils.showErrorToast(activity, "Failed to get owner");
                });
    }

    public static void sendNotification(String data, String message, String otherUserToken) {

        getCurrentUserDetails().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User currentUser = task.getResult().toObject(User.class);
                try {
                    JSONObject jsonObject = new JSONObject();

                    // Tạo JSON object cho thông báo
                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", "Bạn có thông báo mới");
                    notificationObj.put("body", message);

                    // Tạo JSON object cho dữ liệu kèm theo thông báo
                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", getCurrentUserId());
                    dataObj.put("dataSend", data);
                    Log.d("Notification",data);
                    // Tổng hợp thông báo và dữ liệu vào một JSON object chính
                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("data", dataObj);
                    jsonObject.put("to", otherUserToken);

                    // Gọi API để gửi thông báo
                    callApi(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    // Gọi API để gửi thông báo
    private static void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer AAAA-6VPRl8:APA91bEmdWZXSynykGvXltm5R5GxI-2pEbPyKuDut_Ea8_GVEcNgkn72eyRIEjDMaWru_jIZfCVm1TlwT6NrizYvaR3x5ZOPu7gQYKH8dPKoGQNaWsc9GsKmktZkpcucjGVFYk9a5ZYy") // Thay YOUR_SERVER_KEY bằng key server của bạn
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                } else {
                }
            }
        });
    }
}
