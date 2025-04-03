package com.esaip.securoverse2.view;

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
import com.esaip.securoverse2.prensenter.DataLeakCheckerPresenter;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DataLeakCheckerActivity extends AppCompatActivity {
    private DataLeakCheckerPresenter presenter;
    private Spinner dataTypeSpinner;
    private EditText inputField;
    private Button saveButton;
    private RecyclerView dataList;
    private DataAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_leak_checker);

        presenter = new DataLeakCheckerPresenter(this, new UserRepository(this));
        initializeViews();
        setupSpinner();
        setupRecyclerView();
        setupSaveButton();
        presenter.scheduleMonthlyCheck();
    }

    private void initializeViews() {
        dataTypeSpinner = findViewById(R.id.dataTypeSpinner);
        inputField = findViewById(R.id.inputField);
        saveButton = findViewById(R.id.saveButton);
        dataList = findViewById(R.id.dataList);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DataLeakCheckerPresenter.DATA_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataTypeSpinner.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        dataAdapter = new DataAdapter();
        dataList.setLayoutManager(new LinearLayoutManager(this));
        dataList.setAdapter(dataAdapter);
        presenter.loadData();
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> presenter.saveData(
                dataTypeSpinner.getSelectedItem().toString(),
                inputField.getText().toString().trim()
        ));
    }

    public void updateRecyclerView(List<UserEntity> users) {
        dataAdapter.setData(users);
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
