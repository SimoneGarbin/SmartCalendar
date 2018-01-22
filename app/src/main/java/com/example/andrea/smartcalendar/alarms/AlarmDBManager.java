package com.example.andrea.smartcalendar.alarms;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by simone on 08/01/18.
 */

public class AlarmDBManager {
    private AlarmDBAdapter dbAdapter;
    private SQLiteDatabase database;
    private String[] allColumns = { AlarmDBAdapter.ALARM_ID,
            AlarmDBAdapter.ALARM_NAME,
            AlarmDBAdapter.ALARM_TEXT,
            AlarmDBAdapter.ALARM_WHEN
    };

    public AlarmDBManager(Context context) {
        dbAdapter = new AlarmDBAdapter(context);
    }

    public AlarmDBManager() {

    }


    public void open() throws SQLException {
        database = dbAdapter.getWritableDatabase();
    }

    public SQLiteDatabase getDatabase() {
        database = dbAdapter.getWritableDatabase();
        return  database;
    }

    public void close() {
        dbAdapter.close();
    }

    public void saveAlarm(HashMap<String, String> map) {
        SQLiteStatement insert_stm = database.compileStatement(dbAdapter.INSERT_ALARM);
        insert_stm.bindString(1, (String) map.get("id"));
        insert_stm.bindString(2, (String) map.get("name"));
        insert_stm.bindString(3, (String) map.get("text"));
        insert_stm.bindString(4, (String) map.get("when"));
        insert_stm.executeInsert();
    }


    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<Alarm>();
        Cursor cursor = database.query(AlarmDBAdapter.DATABASE_TABLE,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Alarm al = cursorToAlarm(cursor);
            alarms.add(al);
            cursor.moveToNext();
        }
        cursor.close();
        return alarms;
    }

    private Alarm cursorToAlarm(Cursor cursor) {
        Alarm alarm = new Alarm();
        alarm.setId(cursor.getInt(0));
        alarm.setName(cursor.getString(1));
        alarm.setText(cursor.getString(2));
        alarm.setWhen(cursor.getString(3));
        return alarm;
    }
}
