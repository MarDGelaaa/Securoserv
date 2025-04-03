package com.esaip.securoverse2.model;

import com.esaip.securoverse2.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthManager {
    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;

    public FirebaseAuthManager() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }

    public void registerUser(String email, String password, String username, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        // Créer le profil utilisateur dans la base de données
                        FirebaseUser user = task.getResult().getUser();
                        UserProfile profile = new UserProfile(username, email);

                        usersRef.child(user.getUid()).setValue(profile)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Erreur d'inscription");
                    }
                });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        callback.onSuccess(task.getResult().getUser());
                    } else {
                        callback.onError(task.getException() != null ?
                                task.getException().getMessage() : "Erreur de connexion");
                    }
                });
    }

    public void signOut() {
        auth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
}