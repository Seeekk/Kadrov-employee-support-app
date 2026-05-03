package com.ozhd.kadrov.employeesupport.data.local.pojo;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.ozhd.kadrov.employeesupport.data.local.entity.MessageEntity;

/**
 * Результат JOIN messages + users для отображения имени отправителя в чате.
 */
public class MessageWithSender {

    @Embedded
    @NonNull
    public MessageEntity message = new MessageEntity();

    @ColumnInfo(name = "senderFullName")
    public String senderFullName;
}
