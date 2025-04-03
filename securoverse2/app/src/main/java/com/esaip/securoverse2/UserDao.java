package com.esaip.securoverse2;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    @Query("SELECT COUNT(*) FROM users WHERE " +
            "CASE :type " +
            "WHEN 'Email' THEN email IS NOT NULL " +
            "WHEN 'Téléphone' THEN phone IS NOT NULL " +
            "WHEN 'Nom d''utilisateur' THEN username IS NOT NULL " +
            "WHEN 'Nom complet' THEN fullName IS NOT NULL " +
            "ELSE false END")
    int getCountByType(String type);


}