package com.example.andrea.smartcalendar.alarms;

import android.app.AlarmManager;
import android.content.Context;

import java.util.Calendar;
import java.util.HashMap;
import android.app.PendingIntent;
import java.util.Date;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.example.andrea.smartcalendar.utilites.MillisecondsConverter;

/**
 * Created by simone on 24/12/17.
 */


public class AlarmSender {
    private AlarmManager am;
    private Context context;
    private AlarmDBManager alarmManager;

    public AlarmSender(Context context) {
        this.context = context;
    }
    public void createAlarm(Date datetime, int typeOfNotification, Integer id, String name, int notificationTime, String notifiationText) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datetime);
        long timeNotification = 0;
        timeNotification = MillisecondsConverter.convert(typeOfNotification, notificationTime);
        Long when = calendar.getTimeInMillis() - timeNotification;
        setOneTimeAlarm(id, when, name, notifiationText);
    }

    // set alarm

    final public void setOneTimeAlarm(Integer id, Long when, String name, String text) {
        alarmManager = new AlarmDBManager(context);
        Intent intent = new Intent(context, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE);
        am =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            am.cancel(pendingIntent);
            pendingIntent.cancel();
            alarmManager.open();
            SQLiteDatabase database;
            database = alarmManager.getDatabase();
            database.delete("Alarms","_id" + " = ?",new String[]{id.toString()});
            alarmManager.close();
        }
        alarmManager.open();
        String whenString = when.toString();
        HashMap<String, String> alarmMap = new HashMap<String, String>();
        alarmMap.put("id", id.toString());
        alarmMap.put("name", name);
        alarmMap.put("text", text);
        alarmMap.put("when", whenString);
        alarmManager.saveAlarm(alarmMap);
        alarmManager.close();
        intent = new Intent(context, AlertReceiver.class);
        intent.putExtra("name", name);
        intent.putExtra("id", id);
        intent.putExtra("text", text);
        pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
    }

    // check if an alarm associated with id exists
    public Boolean checkExisistingAlarm (Integer id) {
        Intent intent = new Intent(context, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            return true;
        }
        else return false;

    }

    // delete the alarm
    public void deleteAlarm(Integer id) {
        Intent intent = new Intent(context, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE);
        am =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            alarmManager = new AlarmDBManager(context);
            am.cancel(pendingIntent);
            pendingIntent.cancel();
            alarmManager.open();
            SQLiteDatabase database;
            database = alarmManager.getDatabase();
            database.delete("Alarms","_id" + " = ?",new String[]{id.toString()});
            alarmManager.close();
        }
    }
}
