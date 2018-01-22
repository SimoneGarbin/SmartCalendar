package com.example.andrea.smartcalendar.utilites;

import java.util.concurrent.TimeUnit;

/**
 * Created by simone on 16/01/18.
 */

// convert minutes, hours, days and weeks in milliseconds
public class MillisecondsConverter {
    //private long timeNotification = 0;
    public static long convert (int typeOfNotification, int notificationTime){
        long timeNotification;
        switch (typeOfNotification) {
            case 0:
                  timeNotification = TimeUnit.MINUTES.toMillis(notificationTime);
                return timeNotification;
            case 1:
                timeNotification = TimeUnit.HOURS.toMillis(notificationTime);
                return timeNotification;
            case 2:
                timeNotification = TimeUnit.DAYS.toMillis(notificationTime);
                return timeNotification;
            case 3:
                timeNotification = TimeUnit.DAYS.toMillis(notificationTime) * 7;
                return timeNotification;
            default:
                return 0;
        }
    }
}
