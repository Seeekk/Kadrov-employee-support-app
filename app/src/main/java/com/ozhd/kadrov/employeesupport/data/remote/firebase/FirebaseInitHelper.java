package com.ozhd.kadrov.employeesupport.data.remote.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;

/**
 * Проверка, что Firebase инициализирован (иначе Firebase Auth/Firestore могут падать).
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
