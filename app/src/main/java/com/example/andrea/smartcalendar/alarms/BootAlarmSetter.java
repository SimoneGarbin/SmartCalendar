package com.example.andrea.smartcalendar.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by simone on 08/01/18.
 */

// start service at the boot of smartphone

public class BootAlarmSetter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("Boot", "completed");
        }
        Intent i = new Intent(context, ServiceAlarmSetter.class);
        context.startService(i);
    }
}
