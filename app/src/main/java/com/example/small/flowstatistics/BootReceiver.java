package com.example.small.flowstatistics;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by small on 2016/9/30.
 */

public class BootReceiver extends BroadcastReceiver implements Notifications.Interaction_notification {
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

            show_notifiction(context, curdayflow);
        }
    }
    @Override
    public void show_notifiction(Context context, long curdayflow) {

        SharedPreferences pref_default = getDefaultSharedPreferences(context);
        if (!pref_default.getBoolean("ShowNotification",true)){
            return;
        }

        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        long remain_liuliang = pref.getLong("remain_liuliang", 0);
        long all_liuliang = pref.getLong("all_liuliang", 0);
        long curmonthflow=pref.getLong("curmonthflow",0);
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        String notification_string;

        if (Objects.equals(remain_liuliang, "") | Objects.equals(all_liuliang, "")) {
            notification_string = "无流量数据，请启动应用查询";
        } else {
            notification_string = "本月流量还剩 " + new Formatdata().longtostring(remain_liuliang-curmonthflow-curdayflow) + " 今日已用" + new Formatdata().longtostring(curdayflow);
        }
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_album_black_24dp)
                .setContentTitle("流量计")
                .setAutoCancel(true)
                .setOngoing(true)
                .setContentText(notification_string);
        if (Build.VERSION.SDK_INT < 16) {
            notificationManager.notify(0, builder.getNotification());
        } else {
            notificationManager.notify(0, builder.build());
        }
    }

}
