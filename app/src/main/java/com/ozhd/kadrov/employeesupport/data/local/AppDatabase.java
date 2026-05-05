package com.ozhd.kadrov.employeesupport.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ozhd.kadrov.employeesupport.data.local.dao.AttachmentDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.ChatDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.DepartmentDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.NotificationDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.RequestCommentDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.RequestDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.RequestFieldDefinitionDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.RequestKindDao;
import com.ozhd.kadrov.employeesupport.data.local.dao.UserDao;
import com.ozhd.kadrov.employeesupport.data.local.entity.AttachmentEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.ChatEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.ChatMemberEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.DepartmentEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.MessageEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.NotificationEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestCommentEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestFieldDefinitionEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.RequestKindEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Локальная БД Room (offline-first). Версию поднимайте при миграциях схемы.
 */
@Database(
        entities = {
                DepartmentEntity.class,
                UserEntity.class,
                RequestKindEntity.class,
                RequestFieldDefinitionEntity.class,
                RequestEntity.class,
                RequestCommentEntity.class,
                ChatEntity.class,
                ChatMemberEntity.class,
                MessageEntity.class,
                NotificationEntity.class,
                AttachmentEntity.class
        },
        version = 5,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final int DB_THREADS = 4;
    public static final ExecutorService dbExecutor = Executors.newFixedThreadPool(DB_THREADS);

    public abstract UserDao userDao();

    public abstract DepartmentDao departmentDao();

    public abstract RequestKindDao requestKindDao();

    public abstract RequestFieldDefinitionDao requestFieldDao();

    public abstract RequestDao requestDao();

    public abstract RequestCommentDao requestCommentDao();

    public abstract ChatDao chatDao();

    public abstract NotificationDao notificationDao();

    public abstract AttachmentDao attachmentDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "kadrov_support.db"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
