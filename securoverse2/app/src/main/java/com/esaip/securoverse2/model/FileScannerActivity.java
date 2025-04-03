package com.esaip.securoverse2.model;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.esaip.securoverse2.databinding.ActivityFileScannerBinding;
import com.esaip.securoverse2.prensenter.FileScannerPresenter;
import com.esaip.securoverse2.view.FileScannerView;


public class FileScannerActivity extends AppCompatActivity implements FileScannerView {
    private ActivityFileScannerBinding binding;
    private static final int PICK_FILE_REQUEST = 1;
    private FileScannerPresenter presenter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        presenter = new FileScannerPresenter(this);
        setupViews();
        setupProgressDialog();
    }

    private void setupViews() {
        binding.selectFileButton.setOnClickListener(v -> openFilePicker());
        binding.scanButton.setOnClickListener(v -> presenter.onScanClicked(this));
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Analyse du fichier en cours...");
        progressDialog.setCancelable(false);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            presenter.onFileSelected(data.getData(), this);
        }
    }

    @Override
    public void showFileName(String fileName) {
        binding.fileNameText.setText(fileName);
    }

    @Override
    public void enableScanButton(boolean enabled) {
        binding.scanButton.setEnabled(enabled);
    }

    @Override
    public void showProgress() {
        progressDialog.show();
    }

    @Override
    public void hideProgress() {
        progressDialog.dismiss();
    }

    @Override
    public void showScanResult(String result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Résultats de l'analyse");
        builder.setMessage(result);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
