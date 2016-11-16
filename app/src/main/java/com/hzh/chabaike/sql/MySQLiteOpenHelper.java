package com.hzh.chabaike.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by my on 2016/11/15.
 */
public class MySQLiteOpenHelper extends android.database.sqlite.SQLiteOpenHelper {
    public MySQLiteOpenHelper(Context context) {
        super(context, "data.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table collect(_id varchar primary key,title varchar," +
                "source varchar,nickname varchar,create_time varchar)");
        db.execSQL("create table history(_id varchar primary key,title varchar," +
                "source varchar,nickname varchar,create_time varchar)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
