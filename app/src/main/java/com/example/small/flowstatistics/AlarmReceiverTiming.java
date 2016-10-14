package com.example.small.flowstatistics;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by small on 2016/9/30.
 */

public class AlarmReceiverTiming extends BroadcastReceiver implements Notifications.Interaction_notification {
    public NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("qiang", "定时更新广播收到");

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if (activeInfo == null) {
            Log.d("qiang", "网络没有连接");
            return;
        }
        if (activeInfo.isConnected()) {
            if (Objects.equals(activeInfo.getTypeName(), "MOBILE")) {
                SharedPreferences.Editor editor = context.getSharedPreferences("data", Context.MODE_PRIVATE).edit();
                SharedPreferences pref = context.getSharedPreferences("data", Context.MODE_PRIVATE);

                //boolean isreboot = pref.getBoolean("isreboot", false); //1
                //boolean iscurday = pref.getBoolean("iscurday", true);//2
                long cur_boot_mobiletx = TrafficStats.getMobileTxBytes();
                long cur_boot_mobilerx = TrafficStats.getMobileRxBytes();
                long thisbootflow = cur_boot_mobilerx + cur_boot_mobiletx;//3
                //long curdayflow = pref.getLong("curdayflow", 0);//4
                //long onedaylastbootflow = pref.getLong("onedaylastbootflow", 0);//5
                //long onebootlastdayflow = pref.getLong("onebootlastdayflow", 0);//6


                CalculateTodayFlow calculateTodayFlow = new CalculateTodayFlow();
                long curdayflow = calculateTodayFlow.calculate(context);

                editor.putLong("thisbootflow", thisbootflow);
                editor.putLong("curdayflow", curdayflow);
                editor.commit();

                show_notifiction(context, curdayflow);
                context.startService(new Intent(context, AlarmTimingStart.class));
            }
//            Toast.makeText(context, "mobile:" + mobileInfo.isConnected() + "\n" + "wifi:" + wifiInfo.isConnected()      + "\n" + "active:" + activeInfo.getTypeName(), Toast.LENGTH_SHORT).show();
            Log.d("qiang", "mobile:" + mobileInfo.isConnected() + ",wifi:" + wifiInfo.isConnected() + ",active:" + activeInfo.getTypeName());
        } else {
            Log.d("qiang", "网络没有连接,所以终止定时广播");
        }
    }

    @Override
    public void show_notifiction(Context context, long curdayflow) {
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        String remain_liuliang = pref.getString("remain_liuliang", "");
        String all_liuliang = pref.getString("all_liuliang", "");
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        String notification_string;

        //long today = calculate_today(this);

        if (Objects.equals(remain_liuliang, "") | Objects.equals(all_liuliang, "")) {
            notification_string = "无流量数据，请启动应用查询";
        } else {
            notification_string = "本月流量还剩 " + remain_liuliang + " 今日已用" + show_change(curdayflow);
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

    String show_change(long data) {

        DecimalFormat df = new DecimalFormat("#.##");
        double bytes = data / 1024.0;
        if (bytes > 1048576.0 && bytes / 1048576.0 > 0) {
            return df.format(bytes / 1048576.0) + "G";
        } else if (bytes > 1024.0 && bytes / 1024.0 > 0) {
            return df.format(bytes / 1024.0) + "M";
        } else {
            return df.format(bytes) + "k";
        }
    }
}
