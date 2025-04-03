package com.esaip.securoverse2.view;

public interface FileScannerView {
    void showFileName(String fileName);
    void enableScanButton(boolean enabled);
    void showProgress();
    void hideProgress();
    void showScanResult(String result);
    void showError(String message);
}

