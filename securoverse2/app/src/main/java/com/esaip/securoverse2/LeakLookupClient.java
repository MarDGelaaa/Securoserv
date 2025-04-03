package com.esaip.securoverse2;

import android.util.Log;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class LeakLookupClient {
    private static final String API_KEY = "5b77087e4f0ec65eec3d3f1deec96e4e";
    private static final String API_BASE_URL = "https://leak-lookup.com/api/";
    private static final String CHANNEL_ID = "data_leak_channel";
    private static final String TAG = "LeakLookupClient";

    private Context context;
    private LeakLookupService leakLookupService;

    public LeakLookupClient(Context context) {
        this.context = context;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        leakLookupService = retrofit.create(LeakLookupService.class);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Data Leak Alerts";
            String description = "Notifications for data leak alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            Notification notification = new Notification.Builder(context, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .build();

            notificationManager.notify(1, notification);
        }
    }

    public void searchLeaks(String email, String phone, String username, String fullName) {
        searchAndProcessLeak("email_address", email);
        searchAndProcessLeak("phone", phone);
        searchAndProcessLeak("username", username);
        searchAndProcessLeak("fullname", fullName);
    }

    private void searchAndProcessLeak(String type, String query) {
        leakLookupService.searchLeaks(API_KEY, type, query).enqueue(new Callback<LeakLookupResponse>() {
            @Override
            public void onResponse(Call<LeakLookupResponse> call, Response<LeakLookupResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isError()) {
                    if (!response.body().getMessage().isEmpty()) {
                        sendNotification("Data Leak Alert", "Une fuite de données a été détectée pour le type : " + type);
                    }
                } else {
                    Log.e(TAG, "Erreur lors de la vérification de fuite pour " + type);
                }
            }

            @Override
            public void onFailure(Call<LeakLookupResponse> call, Throwable t) {
                Log.e(TAG, "Erreur de connexion lors de la vérification de fuite pour " + type + ": " + t.getMessage());
            }
        });
    }

    private interface LeakLookupService {
        @FormUrlEncoded
        @POST("search")
        Call<LeakLookupResponse> searchLeaks(@Field("key") String apiKey, @Field("type") String type, @Field("query") String query);
    }
}