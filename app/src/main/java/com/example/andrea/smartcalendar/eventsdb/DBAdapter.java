package com.example.andrea.smartcalendar.eventsdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by andrea on 26/05/17.
 */

public class DBAdapter extends SQLiteOpenHelper {

    //database name
    private static final String DATABASE_NAME = "CalendarDB";

    //table name
    public static final String DATABASE_TABLE = "Events";

    //table fields
    public static final String EVENT_ID = "_id";
    public static final String EVENT_NAME = "name";
    public static final String EVENT_POSITION = "position";
    public static final String EVENT_INITIAL_DATE = "initial_date";
    public static final String EVENT_FINAL_DATE = "final_date";
    public static final String EVENT_INITIAL_HOUR = "initial_hour";
    public static final String EVENT_FINAL_HOUR = "final_hour";
    public static final String EVENT_TRANSPORT = "transport";
    public static final String EVENT_NOTIFICATION = "notification";
    public static final String EVENT_POSITION_NOTIFICATION = "position_notification";
    public static final String EVENT_TIME_NOTIFICATION = "time_notification";
    public static final String EVENT_TYPE_NOTIFICATION = "type_notification";
    public static final String MILLISECONDS_TIME = "milliseconds_time";

    //db version
    private static final int DATABASE_VERSION = 2;

    //create db statement
    private static final String DATABASE_CREATE = "create table "
            + DATABASE_TABLE + "("
            + EVENT_ID + " integer not null primary key autoincrement, "
            + EVENT_NAME + " text not null, "
            + EVENT_POSITION + " text not null, "
            + EVENT_INITIAL_DATE + " text not null, "
            + EVENT_FINAL_DATE + " text not null, "
            + EVENT_INITIAL_HOUR + " text not null, "
            + EVENT_FINAL_HOUR + " text not null, "
            + EVENT_TRANSPORT + " text not null,"
            + EVENT_NOTIFICATION + " text,"
            + EVENT_POSITION_NOTIFICATION + " text,"
            + EVENT_TIME_NOTIFICATION + " text,"
            + EVENT_TYPE_NOTIFICATION + " text, "
            + MILLISECONDS_TIME + " text "
            + ");";

    //drop table
    private String DROP_TABLE = "drop table IF EXISTS " + DATABASE_TABLE;

    //query section
    public static final String INSERT_EVENT = "INSERT INTO "
            + DATABASE_TABLE + " ("
            + EVENT_NAME + ", "
            + EVENT_POSITION + ", "
            + EVENT_INITIAL_DATE + ", "
            + EVENT_FINAL_DATE + ", "
            + EVENT_INITIAL_HOUR + ", "
            + EVENT_FINAL_HOUR + ", "
            + EVENT_TRANSPORT + ", "
            + EVENT_NOTIFICATION + ", "
            + EVENT_POSITION_NOTIFICATION + ", "
            + EVENT_TIME_NOTIFICATION + ", "
            + EVENT_TYPE_NOTIFICATION + ", "
            + MILLISECONDS_TIME + ") "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public DBAdapter(Context context) {
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
