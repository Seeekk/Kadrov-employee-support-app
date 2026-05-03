package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;

/**
 * Проверка, что Firebase инициализирован (иначе {@link com.google.firebase.auth.FirebaseAuth}
 * и {@link com.google.firebase.firestore.FirebaseFirestore} падают с {@link IllegalStateException}).
 */
public final class FirebaseInitHelper {

    private FirebaseInitHelper() {
    }

    public static boolean isDefaultAppReady() {
        try {
            FirebaseApp.getInstance();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static void runIfReady(@NonNull Runnable ifReady) {
        if (isDefaultAppReady()) {
            ifReady.run();
        }
    }
}
