package com.example.small.flowstatistics;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by small on 2016/9/30.
 */

public class BootReceiver extends BroadcastReceiver {
    public NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            intent = new Intent(context, AlarmManualStart.class);
            //开启关闭Service1
            context.startService(intent);
            SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
            SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
            long curdayflow = pref.getLong("curdayflow", 0);//4
            new NotificationManagers().showNotificationPrecise(context,curdayflow);
        }
    }



}
