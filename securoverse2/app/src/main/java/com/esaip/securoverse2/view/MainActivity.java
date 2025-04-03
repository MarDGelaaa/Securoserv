package com.esaip.securoverse2.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.esaip.securoverse2.IpScannerActivity;
import com.esaip.securoverse2.MainContract;
import com.esaip.securoverse2.model.FileScannerActivity;
import com.esaip.securoverse2.model.FirebaseAuthManager;
import com.esaip.securoverse2.LoginActivity;
import com.esaip.securoverse2.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager();

        // Vérifier si l'utilisateur est connecté
        if (!authManager.isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        binding.btnIpScanner.setOnClickListener(v -> {
            startActivity(new Intent(this, IpScannerActivity.class));
        });

        binding.btnDataLeakChecker.setOnClickListener(v -> {
            startActivity(new Intent(this, DataLeakCheckerActivity.class));
        });

        binding.btnFileScanner.setOnClickListener(v -> {
            startActivity(new Intent(this, FileScannerActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> {
            authManager.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
