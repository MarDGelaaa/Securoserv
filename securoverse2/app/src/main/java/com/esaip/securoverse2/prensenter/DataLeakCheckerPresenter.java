package com.esaip.securoverse2.prensenter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.util.Log;

import com.esaip.securoverse2.DataAdapter;
import com.esaip.securoverse2.R;
import com.esaip.securoverse2.UserDao;
import com.esaip.securoverse2.UserDatabase;
import com.esaip.securoverse2.UserEntity;
import com.esaip.securoverse2.model.UserRepository;
import com.esaip.securoverse2.view.DataLeakCheckerActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DataLeakCheckerPresenter {
    private static final String TAG = "DataLeakCheckerPresenter";
    public static final String[] DATA_TYPES = {"Email", "Téléphone", "Nom d'utilisateur", "Nom complet"};
    private static final int MAX_ENTRIES_PER_TYPE = 2;

    private final DataLeakCheckerActivity view;
    private final UserRepository repository;

    public DataLeakCheckerPresenter(DataLeakCheckerActivity view, UserRepository repository) {
        this.view = view;
        this.repository = repository;
    }

    public void saveData(String type, String value) {
        if (value.isEmpty()) {
            view.showToast("Veuillez entrer une valeur");
            return;
        }

        if (repository.getCountByType(type) >= MAX_ENTRIES_PER_TYPE) {
            view.showToast("Maximum de " + MAX_ENTRIES_PER_TYPE + " entrées atteint pour " + type);
            return;
        }

        UserEntity user = new UserEntity();
        switch (type) {
            case "Email":
                user.setEmail(value);
                break;
            case "Téléphone":
                user.setPhone(value);
                break;
            case "Nom d'utilisateur":
                user.setUsername(value);
                break;
            case "Nom complet":
                user.setFullName(value);
                break;
        }

        repository.insert(user);
        view.updateRecyclerView(repository.getAllUsers());
        view.showToast("Données enregistrées");
    }

    public void loadData() {
        view.updateRecyclerView(repository.getAllUsers());
    }

    public void scheduleMonthlyCheck() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForLeaks();
            }
        }, 0, 30L * 24 * 60 * 60 * 1000);
    }

    private void checkForLeaks() {
        List<UserEntity> users = repository.getAllUsers();
        for (UserEntity user : users) {
            if (user.getEmail() != null) checkLeak("email_address", user.getEmail());
            if (user.getPhone() != null) checkLeak("phone", user.getPhone());
            if (user.getUsername() != null) checkLeak("username", user.getUsername());
            if (user.getFullName() != null) checkLeak("fullname", user.getFullName());
        }
    }

    private void checkLeak(String type, String value) {
        Log.d(TAG, "Checking leak for " + type + ": " + value);
    }
}
