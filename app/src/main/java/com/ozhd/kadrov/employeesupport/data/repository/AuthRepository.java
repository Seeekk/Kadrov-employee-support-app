package com.ozhd.kadrov.employeesupport.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ozhd.kadrov.employeesupport.BuildConfig;
import com.ozhd.kadrov.employeesupport.core.DemoAccounts;
import com.ozhd.kadrov.employeesupport.core.SessionManager;
import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.local.entity.NotificationEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;
import com.ozhd.kadrov.employeesupport.data.model.ApprovalStatus;
import com.ozhd.kadrov.employeesupport.data.model.Gender;
import com.ozhd.kadrov.employeesupport.data.model.NotificationType;
import com.ozhd.kadrov.employeesupport.data.model.UserRole;
import com.ozhd.kadrov.employeesupport.data.remote.RetrofitModule;
import com.ozhd.kadrov.employeesupport.data.remote.dto.LoginRequest;
import com.ozhd.kadrov.employeesupport.data.remote.dto.LoginResponse;
import com.ozhd.kadrov.employeesupport.data.remote.dto.UserDto;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirebaseAuthHelper;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirebaseInitHelper;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirestoreNotificationSync;
import com.ozhd.kadrov.employeesupport.data.remote.firebase.FirestoreUserSync;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Response;

/**
 * Репозиторий авторизации: REST + Firebase Auth + кэш пользователя в Room.
 */
public class AuthRepository {

    private final SessionManager sessionManager;
    private final AppDatabase database;
    private final FirestoreNotificationSync firestoreNotificationSync;
    private final FirestoreUserSync firestoreUserSync;

    public AuthRepository(@NonNull Context context) {
        this.sessionManager = new SessionManager(context);
        this.database = AppDatabase.getInstance(context);
        this.firestoreNotificationSync = new FirestoreNotificationSync(database);
        this.firestoreUserSync = new FirestoreUserSync();
    }

    @NonNull
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * Вход: сначала REST; при успехе пробуем Firebase Auth для realtime-функций;
     * при недоступности API в debug включается демо-режим.
     */
    public AuthResult login(@NonNull String email, @NonNull String password) {
        String normalizedEmail = email.trim().toLowerCase();
        if (BuildConfig.DEBUG
                && (DemoAccounts.isEmployeeLogin(normalizedEmail, password)
                || DemoAccounts.isHrLogin(normalizedEmail, password))) {
            return demoLogin(normalizedEmail, password);
        }
        try {
            Response<LoginResponse> response = RetrofitModule.apiService()
                    .login(new LoginRequest(normalizedEmail, password))
                    .execute();

            if (response.isSuccessful() && response.body() != null && response.body().user != null) {
                return finishSuccessfulLogin(response.body(), normalizedEmail, password);
            }

            if (BuildConfig.DEBUG) {
                AuthResult firebaseFirst = tryLoginViaFirebaseAuthAndFirestore(normalizedEmail, password);
                if (firebaseFirst != null) {
                    return firebaseFirst;
                }
            }

            String err = "Ошибка сервера";
            try {
                if (response.errorBody() != null) {
                    err = response.errorBody().string();
                }
            } catch (IOException ignored) {
                err = "Ошибка сервера (" + response.code() + ")";
            }
            if (BuildConfig.DEBUG && isUnreachableResponse(response.code())) {
                return demoLogin(normalizedEmail, password);
            }
            return AuthResult.error(err);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                AuthResult firebaseFirst = tryLoginViaFirebaseAuthAndFirestore(normalizedEmail, password);
                if (firebaseFirst != null) {
                    return firebaseFirst;
                }
                return demoLogin(normalizedEmail, password);
            }
            return AuthResult.error("Нет сети или сервер недоступен");
        }
    }

    /**
     * Вход по email/паролю Firebase и профилю {@code users/{uid}} в Firestore (без REST).
     * Возвращает {@code null}, если такого пользователя в Firebase Auth нет — тогда можно пробовать демо.
     */
    @Nullable
    private AuthResult tryLoginViaFirebaseAuthAndFirestore(
            @NonNull String email,
            @NonNull String password
    ) {
        if (!FirebaseInitHelper.isDefaultAppReady()) {
            return null;
        }
        Task<com.google.firebase.auth.AuthResult> authTask =
                FirebaseAuthHelper.signInEmailPassword(email, password);
        if (authTask == null) {
            return null;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AuthResult> resultRef = new AtomicReference<>(null);

        authTask.addOnSuccessListener(authResult -> {
            com.google.firebase.auth.FirebaseUser fbUser =
                    authResult != null ? authResult.getUser() : null;
            if (fbUser == null) {
                resultRef.set(AuthResult.error("Ошибка входа Firebase"));
                latch.countDown();
                return;
            }
            String uid = fbUser.getUid();
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        UserEntity profile = FirestoreUserSync.userEntityFromFirestoreDoc(snapshot);
                        if (profile == null) {
                            if (!snapshot.exists()) {
                                UserEntity bootstrap =
                                        buildBootstrapProfileFromFirebaseUser(fbUser);
                                firestoreUserSync.pushUser(bootstrap);
                                profile = bootstrap;
                            } else {
                                resultRef.set(AuthResult.error(
                                        "Не удалось прочитать профиль в облаке"));
                                latch.countDown();
                                return;
                            }
                        }
                        resultRef.set(finishSuccessfulLoginFromFirestoreProfile(profile, email, password));
                        latch.countDown();
                    })
                    .addOnFailureListener(e -> {
                        resultRef.set(AuthResult.error("Не удалось загрузить профиль"));
                        latch.countDown();
                    });
        }).addOnFailureListener(e -> {
            if (e instanceof FirebaseAuthInvalidUserException) {
                resultRef.set(null);
            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                resultRef.set(AuthResult.error("Неверный пароль"));
            } else {
                resultRef.set(AuthResult.error(
                        e.getMessage() != null ? e.getMessage() : "Ошибка входа через Firebase"));
            }
            latch.countDown();
        });

        try {
            latch.await(25, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AuthResult.error("Вход прерван");
        }
        return resultRef.get();
    }

    /**
     * Локальная регистрация сотрудника (offline/debug).
     */
    public AuthResult registerEmployee(
            @NonNull String fullName,
            @NonNull String email,
            @NonNull String password,
            @NonNull Gender gender,
            boolean militaryLiable
    ) {
        String normalizedName = fullName.trim();
        String e = email.trim().toLowerCase();
        String p = password;
        if (normalizedName.isEmpty()) {
            return AuthResult.error("Введите ФИО");
        }
        if (e.isEmpty() || p.isEmpty()) {
            return AuthResult.error("Заполните email и пароль");
        }
        if (!e.contains("@")) {
            return AuthResult.error("Введите корректный email");
        }
        if (p.length() < 6) {
            return AuthResult.error("Пароль должен быть не короче 6 символов");
        }

        UserEntity existing = database.userDao().findByEmail(e);
        if (existing != null) {
            return AuthResult.error("Пользователь с таким email уже существует");
        }

        UserEntity u = new UserEntity();
        String firebaseUid = createFirebaseUserBlocking(e, p);
        if (firebaseUid == null) {
            return AuthResult.error("Не удалось создать аккаунт в Firebase (возможно, email уже занят)");
        }
        u.id = firebaseUid;
        u.email = e;
        u.passwordHash = p;
        u.fullName = normalizedName;
        u.position = "";
        u.gender = gender;
        u.militaryDocument = null;
        u.role = UserRole.EMPLOYEE;
        u.approvalStatus = ApprovalStatus.PENDING;
        u.isMilitaryLiable = gender == Gender.MALE && militaryLiable;
        u.isActive = false;
        u.updatedAt = System.currentTimeMillis();
        u.isSynced = false;
        database.userDao().upsert(u);
        firestoreUserSync.pushUser(u);
        createHrRegistrationNotifications(u);

        return AuthResult.success(UserRole.EMPLOYEE);
    }

    /** Эндпоинт отсутствует или сервер недоступен — в debug включаем демо-вход. */
    private static boolean isUnreachableResponse(int code) {
        return code >= 500 || code == 404;
    }

    /**
     * Демо: только для отладки без бэкенда. HR — если email содержит "hr." или "admin".
     */
    private AuthResult demoLogin(String email, String password) {
        String e = email.trim();
        String p = password;
        if (e.isEmpty() || p.isEmpty()) {
            return AuthResult.error("Заполните email и пароль");
        }

        UserEntity local = database.userDao().findByEmail(e);
        if (local != null) {
            boolean demoAccount = DemoAccounts.isEmployeeLogin(e, p) || DemoAccounts.isHrLogin(e, p);
            boolean passwordOk = demoAccount || (local.passwordHash != null
                    ? local.passwordHash.equals(p)
                    : verifyFirebaseCredentialsBlocking(e, p));
            if (!passwordOk) {
                return AuthResult.error("Пользователь не найден. Зарегистрируйтесь как сотрудник");
            }
            if (local.isActive) {
                LoginResponse localResp = new LoginResponse();
                localResp.accessToken = "local-token-" + local.id;
                localResp.user = mapUserDto(local);
                return finishSuccessfulLogin(localResp, e, p);
            }
            if (local.dismissalReason != null && !local.dismissalReason.trim().isEmpty()) {
                return AuthResult.error("Аккаунт уволен: " + local.dismissalReason.trim());
            }
            if (local.approvalStatus == ApprovalStatus.PENDING) {
                return AuthResult.error("Аккаунт ожидает подтверждения HR");
            }
            if (local.approvalStatus == ApprovalStatus.REJECTED) {
                return AuthResult.error("Регистрация отклонена HR. Обратитесь в отдел кадров");
            }
        }

        UserDto dto = new UserDto();
        dto.email = e;

        if (DemoAccounts.isEmployeeLogin(e, p)) {
            dto.id = DemoAccounts.SEED_USER_EMPLOYEE_ID;
            dto.fullName = "Иванов Иван (сотрудник)";
            dto.role = "EMPLOYEE";
            dto.departmentId = DemoAccounts.SEED_DEPARTMENT_ID;
        } else if (DemoAccounts.isHrLogin(e, p)) {
            dto.id = DemoAccounts.SEED_USER_HR_ID;
            dto.fullName = "Петрова Мария (HR)";
            dto.role = "HR";
            dto.departmentId = DemoAccounts.SEED_DEPARTMENT_ID;
        } else {
            return AuthResult.error("Пользователь не найден. Зарегистрируйтесь как сотрудник");
        }

        dto.isActive = true;
        dto.updatedAt = System.currentTimeMillis();

        LoginResponse fake = new LoginResponse();
        fake.accessToken = "demo-token-" + dto.id;
        fake.user = dto;
        return finishSuccessfulLogin(fake, e, p);
    }

    /**
     * Вход по данным из Firestore без {@link #mapUser(UserDto)} — сохраняются {@code approvalStatus}
     * и флаги из облака (иначе {@code mapUser} сводит статус только к {@code dto.isActive}).
     */
    private AuthResult finishSuccessfulLoginFromFirestoreProfile(
            @NonNull UserEntity entity,
            @NonNull String email,
            @NonNull String password
    ) {
        AppDatabase.dbExecutor.execute(() -> database.userDao().upsert(entity));

        if (entity.approvalStatus != ApprovalStatus.APPROVED || !entity.isActive) {
            return AuthResult.error("Аккаунт не активирован HR");
        }
        UserRole role = entity.role != null ? entity.role : UserRole.EMPLOYEE;
        String token = "firebase-token-" + entity.id;
        String mail = entity.email != null && !entity.email.isEmpty() ? entity.email : email;
        sessionManager.saveSession(token, entity.id, role, mail);
        FirebaseAuthHelper.signInEmailPasswordSafe(email, password);
        FirebaseAuthHelper.syncRoleFromCustomClaims(sessionManager);

        return AuthResult.success(role);
    }

    private AuthResult finishSuccessfulLogin(@NonNull LoginResponse body, String email, String password) {
        UserDto dto = body.user;
        UserEntity entity = mapUser(dto);
        AppDatabase.dbExecutor.execute(() -> database.userDao().upsert(entity));

        UserRole role = UserRole.fromString(dto.role);
        if (entity.approvalStatus != ApprovalStatus.APPROVED || !entity.isActive) {
            return AuthResult.error("Аккаунт не активирован HR");
        }
        String token = body.accessToken != null ? body.accessToken : "token-" + entity.id;
        sessionManager.saveSession(token, entity.id, role, dto.email);
        FirebaseAuthHelper.signInEmailPasswordSafe(email, password);
        FirebaseAuthHelper.syncRoleFromCustomClaims(sessionManager);

        return AuthResult.success(role);
    }

    private static UserEntity mapUser(UserDto dto) {
        UserEntity u = new UserEntity();
        u.id = dto.id != null ? dto.id : UUID.randomUUID().toString();
        u.email = dto.email != null ? dto.email : "";
        u.fullName = dto.fullName != null ? dto.fullName : "";
        u.phone = dto.phone;
        u.avatarUrl = dto.avatarUrl;
        u.departmentId = dto.departmentId;
        u.position = "";
        u.gender = Gender.MALE;
        u.militaryDocument = null;
        u.role = UserRole.fromString(dto.role);
        u.approvalStatus = dto.isActive ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING;
        u.isMilitaryLiable = false;
        u.isActive = dto.isActive;
        u.updatedAt = dto.updatedAt;
        u.isSynced = true;
        return u;
    }

    private static UserDto mapUserDto(@NonNull UserEntity user) {
        UserDto dto = new UserDto();
        dto.id = user.id;
        dto.email = user.email;
        dto.fullName = user.fullName;
        dto.phone = user.phone;
        dto.avatarUrl = user.avatarUrl;
        dto.departmentId = user.departmentId;
        dto.role = user.role != null ? user.role.name() : UserRole.EMPLOYEE.name();
        dto.isActive = user.isActive;
        dto.updatedAt = user.updatedAt;
        return dto;
    }

    /**
     * Дефолты как в Cloud Function {@code createUserProfile}, если документа ещё нет в Firestore.
     */
    @NonNull
    private UserEntity buildBootstrapProfileFromFirebaseUser(
            @NonNull com.google.firebase.auth.FirebaseUser fbUser) {
        String uid = fbUser.getUid();
        String mail = fbUser.getEmail() != null ? fbUser.getEmail().trim().toLowerCase() : "";
        String displayName = fbUser.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = mail.contains("@") ? buildDisplayNameFromEmail(mail) : "Новый сотрудник";
        }
        long now = System.currentTimeMillis();
        UserEntity u = new UserEntity();
        u.id = uid;
        u.email = mail;
        u.passwordHash = null;
        u.fullName = displayName.trim();
        u.phone = null;
        u.avatarUrl = null;
        u.departmentId = null;
        u.position = "";
        u.gender = Gender.MALE;
        u.militaryDocument = null;
        u.role = UserRole.EMPLOYEE;
        u.approvalStatus = ApprovalStatus.PENDING;
        u.isMilitaryLiable = false;
        u.isActive = false;
        u.updatedAt = now;
        u.isSynced = true;
        return u;
    }

    private static String buildDisplayNameFromEmail(@NonNull String email) {
        int at = email.indexOf('@');
        String left = at > 0 ? email.substring(0, at) : email;
        if (left.isEmpty()) {
            return "Новый сотрудник";
        }
        String normalized = left.replace('.', ' ').replace('_', ' ').trim();
        if (normalized.isEmpty()) {
            return "Новый сотрудник";
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private boolean verifyFirebaseCredentialsBlocking(@NonNull String email, @NonNull String password) {
        Task<com.google.firebase.auth.AuthResult> task = FirebaseAuthHelper.signInEmailPassword(email, password);
        if (task == null) {
            return false;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> successRef = new AtomicReference<>(false);
        task.addOnSuccessListener(result -> {
            successRef.set(result != null && result.getUser() != null);
            latch.countDown();
        }).addOnFailureListener(e -> {
            successRef.set(false);
            latch.countDown();
        });
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return false;
        }
        return Boolean.TRUE.equals(successRef.get());
    }

    @Nullable
    private String createFirebaseUserBlocking(@NonNull String email, @NonNull String password) {
        Task<com.google.firebase.auth.AuthResult> task =
                FirebaseAuthHelper.createUserEmailPassword(email, password);
        if (task == null) {
            return null;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> uidRef = new AtomicReference<>(null);
        AtomicReference<Exception> errRef = new AtomicReference<>(null);
        task.addOnSuccessListener(result -> {
            if (result != null && result.getUser() != null) {
                uidRef.set(result.getUser().getUid());
            }
            latch.countDown();
        }).addOnFailureListener(e -> {
            errRef.set(e);
            latch.countDown();
        });
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return null;
        }
        Exception err = errRef.get();
        if (err instanceof FirebaseAuthUserCollisionException) {
            return null;
        }
        return uidRef.get();
    }

    private void createHrRegistrationNotifications(@NonNull UserEntity newUser) {
        List<UserEntity> hrs = database.userDao().getByRoleSync(UserRole.HR);
        if (hrs == null || hrs.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        for (UserEntity hr : hrs) {
            if (hr == null || hr.id == null || hr.id.isEmpty()) {
                continue;
            }
            NotificationEntity n = new NotificationEntity();
            n.id = "reg-" + UUID.randomUUID();
            n.userId = hr.id;
            n.type = NotificationType.REGISTRATION;
            n.title = "Новая регистрация сотрудника";
            n.message = "Пользователь " + newUser.email + " ожидает подтверждения";
            n.relatedUserId = newUser.id;
            n.isRead = false;
            n.createdAt = now;
            database.notificationDao().upsert(n);
            firestoreNotificationSync.sendRegistrationNotification(hr.id, newUser.id, n.title, n.message);
        }
    }

    public void logout() {
        sessionManager.clear();
        FirebaseAuthHelper.signOut();
    }

    public static final class AuthResult {
        public final boolean success;
        @Nullable
        public final UserRole role;
        @Nullable
        public final String errorMessage;

        private AuthResult(boolean success, @Nullable UserRole role, @Nullable String errorMessage) {
            this.success = success;
            this.role = role;
            this.errorMessage = errorMessage;
        }

        public static AuthResult success(@NonNull UserRole role) {
            return new AuthResult(true, role, null);
        }

        public static AuthResult error(@NonNull String message) {
            return new AuthResult(false, null, message);
        }
    }
}
