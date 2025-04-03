package com.esaip.securoverse2;

import android.content.Context;
import android.content.Intent;

public interface MainContract {
    interface View {
        void navigateToLogin();
        void setupNavigationButtons();
        void startActivity(Intent intent);
        Context getContext(); // Pour obtenir le contexte nécessaire à la navigation
    }

    interface Presenter {
        void checkUserLoginStatus();
        void navigateToIpScanner();
        void navigateToDataLeakChecker();
        void navigateToFileScanner();
        void logout();
    }
}
