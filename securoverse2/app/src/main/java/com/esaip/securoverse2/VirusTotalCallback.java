package com.esaip.securoverse2;

import android.bluetooth.le.ScanResult;

public interface VirusTotalCallback {
    void onSuccess(VirusScanResult result);
    void onError(String error);
}
