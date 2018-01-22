package com.example.andrea.smartcalendar.alarms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.andrea.smartcalendar.alarms.Alarm;
import com.example.andrea.smartcalendar.alarms.AlarmDBManager;
import com.example.andrea.smartcalendar.alarms.AlarmSender;

import java.util.List;
/**
 * Created by simone on 08/01/18.
 */

// get alarms from database and set alarmnotifications

public class ServiceAlarmSetter extends Service {

    AlarmDBManager manager = new AlarmDBManager(this);

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        manager.open();
        List <Alarm> alarms = manager.getAllAlarms();
        AlarmSender sender = new AlarmSender(this);
        for (int i=0; i<alarms.size(); i++){
            long when = Long.parseLong(alarms.get(i).getWhen());
            sender.setOneTimeAlarm(alarms.get(i).getId(), when, alarms.get(i).getName(), alarms.get(i).getText());
        }
        manager.close();
        stopSelf();
    }

}
