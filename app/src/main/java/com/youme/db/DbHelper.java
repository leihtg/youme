package com.youme.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.anser.model.FileModel;
import com.youme.constant.APPFinal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Thinkpad on 2018/1/21 15:54.
 */
public class DbHelper extends SQLiteOpenHelper {
    final String CREATE_TABLE_SQL = "create table file(_id integer primary key autoincrement,name,length,dir,time,path)";
    final String AUTO_BAK_FILES = "create table auto_bak_file(_id integer primary key autoincrement,path)";

    public DbHelper(Context context) {
        super(context, APPFinal.DB_NAME, null, 1);
    }

    public List<FileModel> queryFileList(String path) {
        SQLiteDatabase db = getReadableDatabase();
        List<FileModel> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from file where path=?", new String[]{path});
        while (cursor.moveToNext()) {
            FileModel fm = new FileModel();
            String name = cursor.getString(1);
            long length = cursor.getLong(2);
            String dir = cursor.getString(3);
            long time = cursor.getLong(4);

            try {
                fm.setName(name);
                fm.setLength(length);
                fm.setDir("true".equals(dir));
                fm.setLastModified(time);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            list.add(fm);
        }
        return list;
    }

    public void saveDb(List<FileModel> list, String parent) {
        if (null == list || list.isEmpty()) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from file where path=?", new String[]{parent});
        String sql = "insert into file values(null,?,?,?,?,?)";
        for (FileModel fm : list) {
            db.execSQL(sql, new Object[]{fm.getName(), fm.getLength(), fm.isDir() + "", fm.getLastModified(), parent});
        }
    }

    public List<String> queryAutoBakPath() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from auto_bak_file", new String[]{});
        List<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            String path = cursor.getString(1);
            list.add(path);
        }
        return list;
    }

    public void saveAutoBakPath(Collection<String> list) {
        if (null == list || list.isEmpty()) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from auto_bak_file");
        String sql = "insert into auto_bak_file values(null,?)";
        for (String fm : list) {
            db.execSQL(sql, new Object[]{fm});
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
        db.execSQL(AUTO_BAK_FILES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}