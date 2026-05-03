package com.ozhd.kadrov.employeesupport.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ozhd.kadrov.employeesupport.data.local.AppDatabase;
import com.ozhd.kadrov.employeesupport.data.repository.HrCatalogSyncRepository;

/**
 * Консоль HR: фоновая синхронизация справочников GET {@code hr/catalog} → Room.
 */
public class HrConsoleViewModel extends AndroidViewModel {

    private final HrCatalogSyncRepository syncRepository;
    private final MutableLiveData<SyncOutcome> syncOutcome = new MutableLiveData<>();

    public HrConsoleViewModel(@NonNull Application application) {
        super(application);
        syncRepository = new HrCatalogSyncRepository(application);
    }

    @NonNull
    public LiveData<SyncOutcome> getSyncOutcome() {
        return syncOutcome;
    }

    /** Сброс после показа Toast, чтобы не повторять при повороте экрана. */
    public void clearSyncOutcome() {
        syncOutcome.setValue(null);
    }

    /**
     * Запрос каталога с сервера (Bearer из {@link com.ozhd.kadrov.employeesupport.core.SessionManager}).
     */
    public void syncCatalogFromServer() {
        AppDatabase.dbExecutor.execute(() -> {
            String err = syncRepository.pullAndApplyBlocking();
            if (err == null) {
                syncOutcome.postValue(SyncOutcome.ok());
            } else {
                syncOutcome.postValue(SyncOutcome.error(err));
            }
        });
    }

    /** Результат одной попытки синхронизации для UI. */
    public static final class SyncOutcome {
        public final boolean success;
        @Nullable
        public final String errorDetail;

        private SyncOutcome(boolean success, @Nullable String errorDetail) {
            this.success = success;
            this.errorDetail = errorDetail;
        }

        static SyncOutcome ok() {
            return new SyncOutcome(true, null);
        }

        static SyncOutcome error(@NonNull String detail) {
            return new SyncOutcome(false, detail);
        }
    }
}
