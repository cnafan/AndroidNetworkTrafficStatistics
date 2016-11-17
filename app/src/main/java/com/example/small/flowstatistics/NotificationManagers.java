package com.example.small.flowstatistics;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by small on 2016/11/17.
 */

class NotificationManagers {

    void showNotificationRough(){


    }

    void showNotificationPrecise(Context context,long curdayflow){
        SharedPreferences pref_default = getDefaultSharedPreferences(context);
        if (!pref_default.getBoolean("ShowNotification", true)) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(context);
            if (Build.VERSION.SDK_INT < 16) {
                notificationManager.notify(0, builder.getNotification());
            } else {
                notificationManager.notify(0, builder.build());
            }
            notificationManager.cancel(0);
            return;
        }

        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        long remain_liuliang = pref.getLong("remain_liuliang", 0);
        long all_liuliang = pref.getLong("all_liuliang", 0);
        long curmonthflow = pref.getLong("curmonthflow", 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        String notification_string;

        if (Objects.equals(remain_liuliang, "") | Objects.equals(all_liuliang, "")) {
            notification_string = "无流量数据，请启动应用查询";
        } else {
            notification_string = "本月流量还剩 " + new Formatdata().longtostring(remain_liuliang - curmonthflow - curdayflow) + " 今日已用" + new Formatdata().longtostring(curdayflow);
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
