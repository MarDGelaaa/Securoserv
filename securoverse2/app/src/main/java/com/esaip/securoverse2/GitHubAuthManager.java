package com.esaip.securoverse2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import com.esaip.securoverse2.model.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GitHubAuthManager {
    private static final String CLIENT_ID = "Iv23livLkYllUFTbU17y";
    private static final String CLIENT_SECRET = "b289fc3f4a3ba387080a8bc2cd741b1e8200e8ff";
    private static final String REDIRECT_URI = "myapp://callbackk";
    private static final String AUTH_URL = "https://github.com/login/oauth/authorize";
    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String SCOPE = "user:email";

    private final Context context;
    private final FirebaseAuthManager firebaseAuthManager;
    private String state;
    private SharedPreferences preferences;

    public GitHubAuthManager(Context context, FirebaseAuthManager firebaseAuthManager) {
        this.context = context;
        this.firebaseAuthManager = firebaseAuthManager;
        this.preferences = context.getSharedPreferences("GitHubAuth", Context.MODE_PRIVATE);
        generateState();
    }

    private void generateState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        state = Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        preferences.edit().putString("oauth_state", state).apply();
    }

    public Intent getAuthorizationIntent() {
        Uri authorizeUrl = Uri.parse(AUTH_URL)
                .buildUpon()
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("scope", SCOPE)
                .appendQueryParameter("state", state)
                .build();

        return new Intent(Intent.ACTION_VIEW, authorizeUrl);
    }

    public void handleCallback(Uri callbackUri, FirebaseAuthManager.AuthCallback callback) {
        String receivedState = callbackUri.getQueryParameter("state");
        String code = callbackUri.getQueryParameter("code");
        String savedState = preferences.getString("oauth_state", null);

        if (code == null) {
            callback.onError("Code d'autorisation manquant");
            return;
        }

        if (receivedState == null || !receivedState.equals(savedState)) {
            callback.onError("État CSRF invalide");
            return;
        }

        exchangeCodeForToken(code, callback);
    }

    private void exchangeCodeForToken(String code, FirebaseAuthManager.AuthCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(TOKEN_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    String postData = String.format("client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                            CLIENT_ID, CLIENT_SECRET, code, REDIRECT_URI);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(postData.getBytes(StandardCharsets.UTF_8));
                    }

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }
                        return response.toString();
                    }
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String accessToken = jsonResponse.getString("access_token");
                        fetchGitHubUserAndSignIn(accessToken, callback);
                    } catch (JSONException e) {
                        callback.onError("Erreur de parsing de la réponse");
                    }
                } else {
                    callback.onError("Erreur lors de l'échange du code");
                }
            }
        }.execute();
    }

    private void fetchGitHubUserAndSignIn(String accessToken, FirebaseAuthManager.AuthCallback callback) {
        new AsyncTask<Void, Void, GitHubUser>() {
            @Override
            protected GitHubUser doInBackground(Void... params) {
                try {
                    URL url = new URL("https://api.github.com/user");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization", "token " + accessToken);
                    conn.setRequestProperty("Accept", "application/json");

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            response.append(line);
                        }

                        JSONObject userJson = new JSONObject(response.toString());
                        return new GitHubUser(
                                userJson.getString("login"),
                                userJson.getString("email"),
                                userJson.getString("avatar_url")
                        );
                    }
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(GitHubUser githubUser) {
                if (githubUser != null) {
                    // Utilise FirebaseAuthManager pour créer/connecter l'utilisateur
                    String email = githubUser.email != null ? githubUser.email : githubUser.login + "@github.com";
                    // Génère un mot de passe aléatoire pour l'authentification Firebase
                    String password = UUID.randomUUID().toString();

                    // Vérifie d'abord si l'utilisateur existe
                    firebaseAuthManager.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            callback.onSuccess(user);
                        }

                        @Override
                        public void onError(String error) {
                            // Si l'utilisateur n'existe pas, on le crée
                            if (error.contains("There is no user record")) {
                                firebaseAuthManager.registerUser(email, password, githubUser.login, new FirebaseAuthManager.AuthCallback() {
                                    @Override
                                    public void onSuccess(FirebaseUser user) {
                                        // Ajoute les informations GitHub au profil utilisateur
                                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(user.getUid());

                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("githubUsername", githubUser.login);
                                        updates.put("githubAvatar", githubUser.avatarUrl);
                                        updates.put("githubToken", accessToken);

                                        userRef.updateChildren(updates)
                                                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                                    }

                                    @Override
                                    public void onError(String error) {
                                        callback.onError("Erreur lors de la création du compte : " + error);
                                    }
                                });
                            } else {
                                callback.onError(error);
                            }
                        }
                    });
                } else {
                    callback.onError("Erreur lors de la récupération des informations utilisateur GitHub");
                }
            }
        }.execute();
    }

    private static class GitHubUser {
        private final String login;
        private final String email;
        private final String avatarUrl;

        public GitHubUser(String login, String email, String avatarUrl) {
            this.login = login;
            this.email = email;
            this.avatarUrl = avatarUrl;
        }
    }
}