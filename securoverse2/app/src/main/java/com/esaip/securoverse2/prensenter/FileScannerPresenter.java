package com.esaip.securoverse2.prensenter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;

import com.esaip.securoverse2.VirusScanResult;
import com.esaip.securoverse2.VirusTotalCallback;
import com.esaip.securoverse2.VirusTotalService;
import com.esaip.securoverse2.view.FileScannerView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileScannerPresenter {
    private FileScannerView view;
    private VirusTotalService virusTotalService;
    private Uri selectedFileUri;

    public FileScannerPresenter(FileScannerView view) {
        this.view = view;
        this.virusTotalService = new VirusTotalService();
    }

    public void onFileSelected(Uri fileUri, Context context) {
        this.selectedFileUri = fileUri;
        String fileName = getFileName(fileUri, context);
        view.showFileName("Fichier sélectionné : " + fileName);
        view.enableScanButton(true);
    }

    public void onScanClicked(Context context) {
        if (selectedFileUri == null) {
            view.showError("Veuillez sélectionner un fichier");
            return;
        }

        view.showProgress();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(selectedFileUri);
            File file = createTempFile(inputStream, context);

            virusTotalService.scanFile(file, new VirusTotalCallback() {
                @Override
                public void onSuccess(VirusScanResult result) {
                    // Revenir sur le thread principal avant de mettre à jour l'UI
                    new Handler(Looper.getMainLooper()).post(() -> {
                        view.hideProgress();
                        view.showScanResult(result.toString());
                    });
                }

                @Override
                public void onError(String error) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        view.hideProgress();
                        view.showError("Erreur : " + error);
                    });
                }
            });
        } catch (IOException e) {
            view.hideProgress();
            view.showError("Erreur lors de la lecture du fichier");
        }
    }


    private String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private File createTempFile(InputStream inputStream, Context context) throws IOException {
        File tempFile = File.createTempFile("scan_", null, context.getCacheDir());
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }
}

