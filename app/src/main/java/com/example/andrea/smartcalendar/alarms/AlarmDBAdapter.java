package com.example.andrea.smartcalendar.alarms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by simone on 08/01/18.
 */

public class AlarmDBAdapter  extends SQLiteOpenHelper {
    //database name
    private static final String DATABASE_NAME = "AlarmDB";

    //table name
    public static final String DATABASE_TABLE = "Alarms";

    //table fields
    public static final String ALARM_AUTOID = "_autoid";
    public static final String ALARM_ID = "_id";
    public static final String ALARM_NAME = "name";
    public static final String ALARM_WHEN = "_when";
    public static final String ALARM_TEXT = "_text";


    //db version
    private static final int DATABASE_VERSION = 3;

    //create db statement
    private static final String DATABASE_CREATE = "create table "
            + DATABASE_TABLE + "("
            + ALARM_AUTOID + " integer not null primary key autoincrement, "
            + ALARM_ID + " text not null, "
            + ALARM_NAME + " text not null, "
            + ALARM_TEXT + " text not null, "
            + ALARM_WHEN + " text not null"
            + ");";

    //drop table
    private String DROP_TABLE = "drop table IF EXISTS " + DATABASE_TABLE;

    //query section
    public static final String INSERT_ALARM = "INSERT INTO "
            + DATABASE_TABLE + " ("
            + ALARM_ID + ", "
            + ALARM_NAME + ", "
            + ALARM_TEXT + ", "
            + ALARM_WHEN + ") "
            + "VALUES (?, ?, ?, ?)";


    public AlarmDBAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //create the db
    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            database.execSQL(DROP_TABLE);
            database.execSQL(DATABASE_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

}
