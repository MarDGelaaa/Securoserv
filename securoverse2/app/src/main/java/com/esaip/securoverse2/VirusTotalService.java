package com.esaip.securoverse2;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.esaip.securoverse2.databinding.ActivityFileScannerBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.esaip.securoverse2.VirusScanResult;

public class VirusTotalService {
    private static final String API_URL = "https://www.virustotal.com/api/v3/files";
    private static final String API_KEY = "381687a387c15957e973f9fbde9898370f3e5f952dd4f25c06636d15d898a9da";
    private final OkHttpClient client;

    public VirusTotalService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void scanFile(File file, VirusTotalCallback callback) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("x-apikey", API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Erreur : " + response.code());
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String analysisId = jsonObject.getJSONObject("data").getString("id");
                    checkAnalysisStatus(analysisId, callback);
                } catch (JSONException e) {
                    callback.onError("Erreur lors de l'analyse de la réponse");
                }
            }
        });
    }

    private void checkAnalysisStatus(String analysisId, VirusTotalCallback callback) {
        String analysisUrl = "https://www.virustotal.com/api/v3/analyses/" + analysisId;

        Request request = new Request.Builder()
                .url(analysisUrl)
                .addHeader("x-apikey", API_KEY)
                .get()
                .build();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("Erreur : " + response.code());
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        VirusScanResult result = VirusScanResult.fromJson(jsonObject);
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        callback.onError("Erreur lors de l'analyse des résultats");
                    }
                }
            });
        }, 15000);
    }
}

