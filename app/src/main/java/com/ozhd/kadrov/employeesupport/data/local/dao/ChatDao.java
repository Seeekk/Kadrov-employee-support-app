package com.ozhd.kadrov.employeesupport.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ozhd.kadrov.employeesupport.data.local.entity.ChatEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.ChatMemberEntity;
import com.ozhd.kadrov.employeesupport.data.local.entity.MessageEntity;
import com.ozhd.kadrov.employeesupport.data.local.pojo.MessageWithSender;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertChat(ChatEntity chat);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertMessages(List<MessageEntity> messages);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertMessage(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMember(ChatMemberEntity member);

    @Query("SELECT * FROM chats WHERE id = :chatId LIMIT 1")
    LiveData<ChatEntity> observeChat(String chatId);

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    LiveData<List<MessageEntity>> observeMessages(String chatId);

    /**
     * Имя отправителя через JOIN users; при отсутствии пользователя senderFullName будет null.
     */
    @Query("SELECT messages.*, users.fullName AS senderFullName FROM messages "
            + "LEFT JOIN users ON users.id = messages.senderId "
            + "WHERE messages.chatId = :chatId ORDER BY messages.timestamp ASC")
    LiveData<List<MessageWithSender>> observeMessagesWithSender(String chatId);

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :limit")
    List<MessageEntity> getRecentSync(String chatId, int limit);
}
