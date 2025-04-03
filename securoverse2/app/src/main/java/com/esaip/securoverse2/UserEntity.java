package com.esaip.securoverse2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String email;
    private String phone;
    private String username;
    private String fullName;

    // Constructeur sans arguments
    public UserEntity() {
    }

    // Constructeur avec arguments
    public UserEntity(String email, String phone, String username, String fullName) {
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.fullName = fullName;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
