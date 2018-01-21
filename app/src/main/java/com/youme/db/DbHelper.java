package com.youme.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.youme.constant.APPFinal;

/**
 * Created by Thinkpad on 2018/1/21 15:54.
 */
public class DbHelper extends SQLiteOpenHelper {
    final String CREATE_TABLE_SQL = "create table file(_id integer primary key autoincrement,name,length,dir,time,path)";

    public DbHelper(Context context) {
        super(context, APPFinal.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
