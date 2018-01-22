package com.example.andrea.smartcalendar.eventsdb;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andrea on 26/05/17.
 */

public class DBManager {
    private DBAdapter dbAdapter;
    private SQLiteDatabase database;
    private String[] allColumns = { DBAdapter.EVENT_ID,
            DBAdapter.EVENT_NAME,
            DBAdapter.EVENT_POSITION,
            DBAdapter.EVENT_INITIAL_DATE,
            DBAdapter.EVENT_FINAL_DATE,
            DBAdapter.EVENT_INITIAL_HOUR,
            DBAdapter.EVENT_FINAL_HOUR,
            DBAdapter.EVENT_TRANSPORT,
            DBAdapter.EVENT_NOTIFICATION,
            DBAdapter.EVENT_POSITION_NOTIFICATION,
            DBAdapter.EVENT_TIME_NOTIFICATION,
            DBAdapter.EVENT_TYPE_NOTIFICATION,
            DBAdapter.MILLISECONDS_TIME
    };

    public DBManager(Context context) {
        dbAdapter = new DBAdapter(context);
    }

    public DBManager() {

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

    public void saveValues(HashMap<String, String> events) {
        SQLiteStatement insert_stm = database.compileStatement(dbAdapter.INSERT_EVENT);
        insert_stm.bindString(1, (String) events.get("name"));
        insert_stm.bindString(2, (String) events.get("position"));
        insert_stm.bindString(3, (String) events.get("initial_date"));
        insert_stm.bindString(4, (String) events.get("final_date"));
        insert_stm.bindString(5, (String) events.get("initial_hour"));
        insert_stm.bindString(6, (String) events.get("final_hour"));
        insert_stm.bindString(7, (String) events.get("transport"));
        insert_stm.bindString(8, (String) events.get("notification"));
        insert_stm.bindString(9, (String) events.get("position_notification"));
        insert_stm.bindString(10, (String) events.get("time_notification"));
        insert_stm.bindString(11, (String) events.get("type_notification"));
        insert_stm.bindString(12, (String) events.get("milliseconds_time"));
        insert_stm.executeInsert();
    }

    public List<Event> getDayEvents(String date) {
        List<Event> events = new ArrayList<Event>();
        final String TABLE_NAME = DBAdapter.DATABASE_TABLE;
        final String EVENT_INITIAL = DBAdapter.EVENT_INITIAL_DATE;
        Cursor cursor = database.query(
                TABLE_NAME /* table */,
                new String[] { "_id, name, position" } /* columns */,
                "initial_date = ?" /* where or selection */,
                new String[] { date } /* selectionArgs i.e. value to replace ? */,
                null /* groupBy */,
                null /* having */,
                "initial_hour ASC"
        );

        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst())
            {
                do {
                    Event comment = cursorToEvent(cursor);
                    events.add(comment);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        return events;
    }

    public Event getIdEvent(String id) {
        final String TABLE_NAME = DBAdapter.DATABASE_TABLE;
        final String EVENT_INITIAL = DBAdapter.EVENT_INITIAL_DATE;
        Event event = null;

        Cursor cursor = database.query(
                TABLE_NAME /* table */,
                new String[] { "*" } /* columns */,
                "_id = ?" /* where or selection */,
                new String[] { id } /* selectionArgs i.e. value to replace ? */,
                null /* groupBy */,
                null /* having */,
                null /* orderBy */
        );

        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst())
            {
                do {
                    event = cursorToAllEvents(cursor);

                } while (cursor.moveToNext());
            }

        }
        cursor.close();
        return event;
    }

    public List<Event> getNotificationEvents(String milli) {
        //List<Event> events = new ArrayList<Event>();
        List<Event> events = new ArrayList<Event>();
        final String TABLE_NAME = DBAdapter.DATABASE_TABLE;
        final String EVENT_INITIAL = DBAdapter.EVENT_INITIAL_DATE;
        Event event = null;
        Cursor cursor = database.query(
                TABLE_NAME /* table */,
                new String[] { "*" } /* columns */,
                "position_notification = ? AND milliseconds_time > ? " /* where or selection */,
                new String[] { "1", milli } /* selectionArgs i.e. value to replace ? */,
                null /* groupBy */,
                null /* having */,
                null /* orderBy */
        );
        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst())
            {
                do {
                    event = cursorToAllEvents(cursor);
                    events.add(event);
                } while (cursor.moveToNext());
            }

        }
        cursor.close();
        return events;
    }

    public List<Event> getEventsForMap(String date, String milli) {
        List<Event> events = new ArrayList<Event>();
        final String TABLE_NAME = DBAdapter.DATABASE_TABLE;
        Event event = null;
        Cursor cursor = database.query(
                TABLE_NAME /* table */,
                new String[] { "*" } /* columns */,
                "initial_date = ? AND milliseconds_time > ? " /* where or selection */,
                new String[] { date, milli } /* selectionArgs i.e. value to replace ? */,
                null /* groupBy */,
                null /* having */,
                null /* orderBy */
        );
        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst())
            {
                do {
                    event = cursorToAllEvents(cursor);
                    events.add(event);
                } while (cursor.moveToNext());
            }

        }
        cursor.close();
        return events;
    }

    public Event getLastInsertEvent() {

        final String TABLE_NAME = DBAdapter.DATABASE_TABLE;
        Event event = null;
        Cursor cursor = database.query(
                TABLE_NAME /* table */,
                new String[] { "*" } /* columns */,
                null /* where or selection */,
                null /* selectionArgs i.e. value to replace ? */,
                null /* groupBy */,
                null /* having */,
                "_id" +" DESC", "1" /* orderBy */
        );

        if(cursor!=null && cursor.getCount() > 0)
        {
            if (cursor.moveToFirst())
            {
                do {
                    event = cursorToAllEvents(cursor);

                } while (cursor.moveToNext());
            }

        }
        cursor.close();
        return event;
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<Event>();

        Cursor cursor = database.query(DBAdapter.DATABASE_TABLE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Event comment = cursorToAllEvents(cursor);
            events.add(comment);
            cursor.moveToNext();
        }
        cursor.close();
        return events;
    }

    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getLong(0));
        event.setName(cursor.getString(1));
        event.setPosition(cursor.getString(2));
        event.setInitial_date(" ");
        event.setFinal_date("");
        event.setInitial_hour("");
        event.setFinal_hour("");
        event.setTransport("");
        event.setNotification("");
        event.setPosition_notification("");
        event.setTime_notification("");
        event.setType_notification("");

        return event;
    }

    private Event cursorToAllEvents(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getLong(0));
        event.setName(cursor.getString(1));
        event.setPosition(cursor.getString(2));
        event.setInitial_date(cursor.getString(3));
        event.setFinal_date(cursor.getString(4));
        event.setInitial_hour(cursor.getString(5));
        event.setFinal_hour(cursor.getString(6));
        event.setTransport(cursor.getString(7));
        event.setNotification(cursor.getString(8));
        event.setPosition_notification(cursor.getString(9));
        event.setTime_notification(cursor.getString(10));
        event.setType_notification(cursor.getString(11));
        event.setMilliseconds_time(cursor.getString(12));

        return event;
    }

}
