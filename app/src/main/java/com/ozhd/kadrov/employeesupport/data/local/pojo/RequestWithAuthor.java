package com.ozhd.kadrov.employeesupport.data.local.pojo;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.ozhd.kadrov.employeesupport.data.local.entity.RequestEntity;

/**
 * JOIN requests + users для списка заявок с ФИО автора.
 */
public class RequestWithAuthor {

    @Embedded
    @NonNull
    public RequestEntity request = new RequestEntity();

    @ColumnInfo(name = "authorFullName")
    public String authorFullName;
}
