package com.esaip.securoverse2;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IpScannerActivity extends AppCompatActivity {
    private EditText urlInput;
    private Button scanButton;
    private TextView resultText;
    private ProgressBar progressBar;

    private static final String API_KEY = "381687a387c15957e973f9fbde9898370f3e5f952dd4f25c06636d15d898a9da";
    private static final String BASE_URL = "https://www.virustotal.com/api/v3/";
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_scanner);

        // Initialisation des vues
        urlInput = findViewById(R.id.urlInput);
        scanButton = findViewById(R.id.scanButton);
        resultText = findViewById(R.id.resultText);
        progressBar = findViewById(R.id.progressBar);

        // Initialisation du client HTTP
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        scanButton.setOnClickListener(v -> performScan());
    }

    private void performScan() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            urlInput.setError("Veuillez entrer une URL");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        scanButton.setEnabled(false);
        resultText.setText("");

        // Première requête pour obtenir l'ID d'analyse
        RequestBody formBody = new FormBody.Builder()
                .add("url", url)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "urls")
                .post(formBody)
                .addHeader("x-apikey", API_KEY)
                .addHeader("accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> handleError("Erreur réseau: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> handleError("Erreur serveur: " + response.code()));
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    String analysisId = jsonResponse.getJSONObject("data").getString("id");
                    getAnalysisResults(analysisId);
                } catch (JSONException e) {
                    runOnUiThread(() -> handleError("Erreur de parsing JSON: " + e.getMessage()));
                }
            }
        });
    }

    private void getAnalysisResults(String analysisId) {
        Request request = new Request.Builder()
                .url(BASE_URL + "analyses/" + analysisId)
                .get()
                .addHeader("x-apikey", API_KEY)
                .addHeader("accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> handleError("Erreur réseau: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> handleError("Erreur serveur: " + response.code()));
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONObject attributes = jsonResponse.getJSONObject("data")
                            .getJSONObject("attributes");

                    String status = attributes.getString("status");
                    if (status.equals("queued") || status.equals("in-progress")) {
                        // Réessayer dans 5 secondes
                        new Handler(Looper.getMainLooper()).postDelayed(
                                () -> getAnalysisResults(analysisId), 5000);
                        return;
                    }

                    JSONObject stats = attributes.getJSONObject("stats");
                    final int malicious = stats.getInt("malicious");
                    final int suspicious = stats.getInt("suspicious");
                    final int harmless = stats.getInt("harmless");
                    final int undetected = stats.getInt("undetected");

                    runOnUiThread(() -> displayResults(malicious, suspicious, harmless, undetected));
                } catch (JSONException e) {
                    runOnUiThread(() -> handleError("Erreur de parsing JSON: " + e.getMessage()));
                }
            }
        });
    }

    private void displayResults(int malicious, int suspicious, int harmless, int undetected) {
        progressBar.setVisibility(View.GONE);
        scanButton.setEnabled(true);

        int total = malicious + suspicious + harmless + undetected;
        float score = 100 - ((float) (malicious + suspicious) / total * 100);

        String resultMessage = String.format(Locale.FRANCE,
                "Résultat du scan:\n\n" +
                        "Score de confiance: %.1f%%\n\n" +
                        "Détails:\n" +
                        "- Malicieux: %d\n" +
                        "- Suspect: %d\n" +
                        "- Inoffensif: %d\n" +
                        "- Non détecté: %d",
                score, malicious, suspicious, harmless, undetected);

        resultText.setText(resultMessage);
        resultText.setTextColor(getScoreColor(score));
    }

    private int getScoreColor(float score) {
        if (score >= 80) {
            return Color.GREEN;
        } else if (score >= 60) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private void handleError(String error) {
        progressBar.setVisibility(View.GONE);
        scanButton.setEnabled(true);
        resultText.setText("Erreur: " + error);
        resultText.setTextColor(Color.RED);
    }
}