package com.esaip.securoverse2;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esaip.securoverse2.databinding.ActivityLoginBinding;
import com.esaip.securoverse2.model.FirebaseAuthManager;
import com.esaip.securoverse2.view.MainActivity;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private GitHubAuthManager githubAuthManager;
    private ActivityLoginBinding binding;
    private FirebaseAuthManager authManager;
    private ProgressDialog progressDialog;

    private static final String REDIRECT_URI = "myapp://callbackk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Connexion en cours...");
        progressDialog.setCancelable(false);
        githubAuthManager = new GitHubAuthManager(this, authManager);
        setupGitHubLogin();

        // Vérifier si l'utilisateur est déjà connecté
        if (authManager.isUserLoggedIn()) {
            startMainActivity();
            finish();
            return;
        }

        setupLoginButton();
        setupInputValidation();
    }

    private void setupGitHubLogin() {
        binding.githubLoginButton.setOnClickListener(v -> {
            startActivity(githubAuthManager.getAuthorizationIntent());
        });
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null && data.toString().startsWith(REDIRECT_URI)) {
            progressDialog.show();
            githubAuthManager.handleCallback(data, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressDialog.dismiss();
                    startMainActivity();
                }

                @Override
                public void onError(String error) {
                    progressDialog.dismiss();
                    showError("Erreur d'authentification GitHub : " + error);
                }
            });
        }
    }

    private void setupLoginButton() {
        binding.loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                performLogin();
            }
        });
    }

    private void setupInputValidation() {
        // Validation en temps réel de l'email
        binding.emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.emailLayout.setError(isValidEmail(s.toString()) ? null : "Email invalide");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Validation en temps réel du mot de passe
        binding.passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.passwordLayout.setError(s.length() >= 6 ? null :
                        "Le mot de passe doit contenir au moins 6 caractères");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateInputs() {
        String username = binding.usernameEditText.getText().toString();
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();

        boolean isValid = true;

        // Validation du nom d'utilisateur
        if (username.trim().isEmpty()) {
            binding.usernameLayout.setError("Le nom d'utilisateur est requis");
            isValid = false;
        } else {
            binding.usernameLayout.setError(null);
        }

        // Validation de l'email
        if (!isValidEmail(email)) {
            binding.emailLayout.setError("Email invalide");
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        // Validation du mot de passe
        if (password.length() < 6) {
            binding.passwordLayout.setError("Le mot de passe doit contenir au moins 6 caractères");
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void performLogin() {
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        String username = binding.usernameEditText.getText().toString();

        progressDialog.show();

        // Vérifier si l'utilisateur existe déjà
        authManager.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressDialog.dismiss();
                startMainActivity();
                finish();
            }

            @Override
            public void onError(String error) {
                // Si l'utilisateur n'existe pas, on le crée
                if (error.contains("There is no user record")) {
                    registerNewUser(email, password, username);
                } else {
                    progressDialog.dismiss();
                    showError("Erreur de connexion : " + error);
                }
            }
        });
    }

    private void registerNewUser(String email, String password, String username) {
        authManager.registerUser(email, password, username, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                progressDialog.dismiss();
                startMainActivity();
                finish();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                showError("Erreur d'inscription : " + error);
            }
        });
    }
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        binding = null;
    }
}