package com.esaip.securoverse2.model;

import android.content.Context;

import androidx.room.Room;

import com.esaip.securoverse2.UserDao;
import com.esaip.securoverse2.UserDatabase;
import com.esaip.securoverse2.UserEntity;

import java.util.List;

public class UserRepository {
    private final UserDao userDao;

    public UserRepository(Context context) {
        UserDatabase db = Room.databaseBuilder(context.getApplicationContext(),
                        UserDatabase.class, "user-database")
                .allowMainThreadQueries()
                .build();
        this.userDao = db.userDao();
    }

    public void insert(UserEntity user) {
        userDao.insert(user);
    }

    public List<UserEntity> getAllUsers() {
        return userDao.getAllUsers();
    }

    public int getCountByType(String type) {
        return userDao.getCountByType(type);
    }
}

