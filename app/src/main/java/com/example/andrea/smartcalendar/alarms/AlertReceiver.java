package com.example.andrea.smartcalendar.alarms;

import android.content.BroadcastReceiver;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.BitmapFactory;

import com.example.andrea.smartcalendar.R;

/**
 * Created by simone on 19/12/17.
 */

public class AlertReceiver extends BroadcastReceiver {

    NotificationManager nm;
    int color = Color.argb(255, 255, 165, 0);
    long[] pattern = {0, 500};

    @Override
    public void onReceive(Context context, Intent intent) {
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int id = intent.getExtras().getInt("id");
        String text = intent.getExtras().getString("text");
        if(intent.getExtras().getString("delete") != null){
            AlarmSender alarmSender = new AlarmSender(context);
            alarmSender.deleteAlarm(id);
            nm.cancel(id);
        }
        else {
            Intent newIntent = new Intent(context, AlertReceiver.class);
            newIntent.putExtra("id", id);
            newIntent.putExtra("delete", "yes");
            PendingIntent deleteNotification = PendingIntent.getBroadcast(context, id, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            String eventName = intent.getExtras().getString("name");
            Notification notification = new NotificationCompat.Builder(context)
                    .setContentTitle(eventName)
                    .setContentText(text)
                    .setLights(color,1000,1000)
                    .setVibrate(pattern)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_2))
                    .addAction(R.drawable.ic_clear, context.getResources().getString(R.string.delete), deleteNotification)
                    .build();
            nm.notify(id, notification);
        }

    }
}