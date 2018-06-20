package com.example.ch3_appwidget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//DBMS를 위한 개발자 Util 클래스..
public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, "widgetdb", null, DATABASE_VERSION);
    }

    //app install 후 최초에 한번 호출.. 보통의 경우 table create
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql= "create table tb_data " +
                "(_id integer primary key autoincrement,"
                + "content not null)";

        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion==DATABASE_VERSION){
            db.execSQL("drop table tb_data");
            onCreate(db);
        }

    }

}